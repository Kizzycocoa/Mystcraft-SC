package myst.synthetic.client.page;

import myst.synthetic.page.emblem.ResolvedPageEmblem;
import myst.synthetic.page.emblem.ResolvedPageWord;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public final class PageTextureCompositor {

    private static final String PAGE_BACKGROUND_PATH = "assets/mystcraft-sc/textures/item/page_background.png";
    private static final String SYMBOL_COMPONENTS_PATH = "assets/mystcraft-sc/textures/page/symbolcomponents.png";

    private static final int COMPONENT_SIZE = 64;
    private static final int COMPONENTS_PER_ROW = 8;
    private static final float POEM_SCALE = 0.9F;

    private static final int FINAL_PAGE_SIZE = 64;

    // Render the content face at 2x the old size so the page card can downsample it.
    private static final int CONTENT_SIZE = 58;

    private PageTextureCompositor() {
    }

    public static BufferedImage composeBlankPage() {
        return downscaleToFinal(buildWorkingBackground());
    }

    public static BufferedImage composeLinkPanelPage() {
        BufferedImage page = buildWorkingBackground();

        int width = 110;
        int height = 45;
        int startX = 25;
        int startY = 30;

        for (int x = startX; x <= startX + width; x++) {
            for (int y = startY; y <= startY + height; y++) {
                page.setRGB(x, y, 0xFF000000);
            }
        }

        return downscaleToFinal(page);
    }

    public static BufferedImage composeSymbolPage(ResolvedPageEmblem emblem) {
        BufferedImage page = buildWorkingBackground();
        BufferedImage source = loadImage(SYMBOL_COMPONENTS_PATH);

        stitchEmblem(page, emblem, source);
        return downscaleToFinal(page);
    }

    public static BufferedImage composeBlankContent() {
        return new BufferedImage(CONTENT_SIZE, CONTENT_SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    public static BufferedImage composeLinkPanelContent() {
        BufferedImage working = createTransparentWorkingCanvas();

        int width = 110;
        int height = 45;
        int startX = 25;
        int startY = 30;

        for (int x = startX; x <= startX + width; x++) {
            for (int y = startY; y <= startY + height; y++) {
                working.setRGB(x, y, 0xFF000000);
            }
        }

        BufferedImage finalSized = downscaleToFinal(working);
        return scaleFinalPageToContent(finalSized);
    }

    public static BufferedImage composeSymbolContent(ResolvedPageEmblem emblem) {
        BufferedImage working = createTransparentWorkingCanvas();
        BufferedImage source = loadImage(SYMBOL_COMPONENTS_PATH);

        stitchEmblem(working, emblem, source);

        BufferedImage finalSized = downscaleToFinal(working);
        return scaleFinalPageToContent(finalSized);
    }

    private static void stitchEmblem(BufferedImage target, ResolvedPageEmblem emblem, BufferedImage source) {
        if (emblem == null || emblem.isEmpty()) {
            stitchWord(target, null, getPageTarget(-1), source);
            return;
        }

        List<ResolvedPageWord> words = emblem.words();
        if (words.size() > 0) {
            stitchWord(target, words.get(0), getPageTarget(0), source);
        }
        if (words.size() > 1) {
            stitchWord(target, words.get(1), getPageTarget(1), source);
        }
        if (words.size() > 2) {
            stitchWord(target, words.get(2), getPageTarget(2), source);
        }
        if (words.size() > 3) {
            stitchWord(target, words.get(3), getPageTarget(3), source);
        }
    }

    private static void stitchWord(BufferedImage targetImage, ResolvedPageWord word, Rectangle targetRect, BufferedImage source) {
        List<Integer> components = null;
        List<Integer> colors = null;

        if (word != null) {
            components = word.resolvedWord().componentIndices();
            colors = word.resolvedWord().colors();
        }

        if (components == null || components.isEmpty()) {
            components = new LinkedList<>();
            components.add(0);
            colors = List.of(0x000000);
        }

        int scaledSize = Math.max(1, Math.round(COMPONENT_SIZE * POEM_SCALE));
        int inset = (COMPONENT_SIZE - scaledSize) / 2;

        for (int i = 0; i < components.size(); i++) {
            int color = 0x000000;
            if (colors != null && !colors.isEmpty()) {
                if (i < colors.size()) {
                    color = colors.get(i);
                } else {
                    color = colors.get(0);
                }
            }

            int component = components.get(i);
            int iconX = (component % COMPONENTS_PER_ROW) * COMPONENT_SIZE;
            int iconY = (component / COMPONENTS_PER_ROW) * COMPONENT_SIZE;

            for (int x = 0; x < scaledSize; x++) {
                int srcX = (x * COMPONENT_SIZE) / scaledSize;
                int tx = targetRect.x + inset + x;
                if (tx < 0 || tx >= targetImage.getWidth()) {
                    continue;
                }

                for (int y = 0; y < scaledSize; y++) {
                    int srcY = (y * COMPONENT_SIZE) / scaledSize;
                    int ty = targetRect.y + inset + y;
                    if (ty < 0 || ty >= targetImage.getHeight()) {
                        continue;
                    }

                    int argb = source.getRGB(iconX + srcX, iconY + srcY);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (alpha == 0) {
                        continue;
                    }

                    int currentArgb = targetImage.getRGB(tx, ty);
                    int targetColor = blend(color, argb, currentArgb);
                    targetImage.setRGB(tx, ty, targetColor);
                }
            }
        }
    }

    private static int blend(int targetInitialColor, int currentColor, int targetColor) {
        float srcAlpha = ((currentColor >>> 24) & 0xFF) / 255.0F;

        float srcRed = ((targetInitialColor >>> 16) & 0xFF) / 255.0F;
        float srcGreen = ((targetInitialColor >>> 8) & 0xFF) / 255.0F;
        float srcBlue = (targetInitialColor & 0xFF) / 255.0F;

        float dstRed = ((targetColor >>> 16) & 0xFF) / 255.0F;
        float dstGreen = ((targetColor >>> 8) & 0xFF) / 255.0F;
        float dstBlue = (targetColor & 0xFF) / 255.0F;

        int outRed = clamp255((srcRed * srcAlpha + dstRed * (1.0F - srcAlpha)) * 255.0F);
        int outGreen = clamp255((srcGreen * srcAlpha + dstGreen * (1.0F - srcAlpha)) * 255.0F);
        int outBlue = clamp255((srcBlue * srcAlpha + dstBlue * (1.0F - srcAlpha)) * 255.0F);

        return (255 << 24) | (outRed << 16) | (outGreen << 8) | outBlue;
    }

    private static int clamp255(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    private static Rectangle getPageTarget(int index) {
        return switch (index) {
            case 3 -> new Rectangle(8, 48, 64, 64);
            case 2 -> new Rectangle(48, 88, 64, 64);
            case 1 -> new Rectangle(88, 48, 64, 64);
            case 0 -> new Rectangle(48, 8, 64, 64);
            default -> new Rectangle(48, 48, 64, 64);
        };
    }

    private static BufferedImage buildWorkingBackground() {
        BufferedImage base = loadImage(PAGE_BACKGROUND_PATH);
        BufferedImage scaled = scaleImage(base, 5.0);
        ColorModel colorModel = scaled.getColorModel();
        return new BufferedImage(colorModel, scaled.copyData(null), colorModel.isAlphaPremultiplied(), null);
    }

    private static BufferedImage createTransparentWorkingCanvas() {
        BufferedImage reference = buildWorkingBackground();
        return new BufferedImage(reference.getWidth(), reference.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    private static BufferedImage scaleFinalPageToContent(BufferedImage finalSized) {
        double scale = (double) CONTENT_SIZE / (double) FINAL_PAGE_SIZE;
        return scaleImage(finalSized, scale);
    }

    private static BufferedImage downscaleToFinal(BufferedImage source) {
        return scaleImage(source, 0.8);
    }

    private static BufferedImage scaleImage(BufferedImage source, double scale) {
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.geom.AffineTransform transform = java.awt.geom.AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp op = new AffineTransformOp(
                transform,
                scale > 1.0 ? AffineTransformOp.TYPE_NEAREST_NEIGHBOR : AffineTransformOp.TYPE_BILINEAR
        );

        op.filter(source, output);
        return output;
    }

    private static BufferedImage loadImage(String path) {
        try (InputStream stream = PageTextureCompositor.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing page render resource: " + path);
            }

            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Failed to decode page render resource: " + path);
            }

            return image;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load page render resource: " + path, e);
        }
    }
}