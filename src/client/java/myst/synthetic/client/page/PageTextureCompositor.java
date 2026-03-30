package myst.synthetic.client.page;

import myst.synthetic.page.emblem.PageGlyphSlot;
import myst.synthetic.page.emblem.ResolvedGlyphComponent;
import myst.synthetic.page.emblem.ResolvedPageEmblem;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class PageTextureCompositor {

    private static final String PAGE_BACKGROUND_PATH = "assets/mystcraft-sc/textures/item/page_background.png";
    private static final String SYMBOL_COMPONENTS_PATH = "assets/mystcraft-sc/textures/page/symbolcomponents.png";

    private static final int WORKING_SIZE = 160;
    private static final int FINAL_SIZE = 128;
    private static final int COMPONENT_SIZE = 64;
    private static final int COMPONENTS_PER_ROW = 8;

    private PageTextureCompositor() {
    }

    public static BufferedImage composeBlankPage() {
        return downscaleToFinal(buildWorkingBackground());
    }

    public static BufferedImage composeLinkPanelPage() {
        BufferedImage page = buildWorkingBackground();

        Graphics2D graphics = page.createGraphics();
        try {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(25, 30, 110, 45);
        } finally {
            graphics.dispose();
        }

        return downscaleToFinal(page);
    }

    public static BufferedImage composeSymbolPage(ResolvedPageEmblem emblem, List<ResolvedGlyphComponent> glyphs) {
        BufferedImage page = buildWorkingBackground();
        BufferedImage atlas = loadImage(SYMBOL_COMPONENTS_PATH);

        for (ResolvedGlyphComponent glyph : glyphs) {
            drawGlyphComponent(page, atlas, glyph);
        }

        return downscaleToFinal(page);
    }

    private static void drawGlyphComponent(
            BufferedImage target,
            BufferedImage atlas,
            ResolvedGlyphComponent glyph
    ) {
        int componentIndex = glyph.component().index();
        int srcX = (componentIndex % COMPONENTS_PER_ROW) * COMPONENT_SIZE;
        int srcY = (componentIndex / COMPONENTS_PER_ROW) * COMPONENT_SIZE;

        BufferedImage sourcePiece = atlas.getSubimage(srcX, srcY, COMPONENT_SIZE, COMPONENT_SIZE);
        BufferedImage rotated = rotateImage(sourcePiece, glyph.component().rotation(), glyph.component().scale());

        Rectangle targetRect = getTargetRect(glyph);

        blendImage(target, rotated, targetRect.x, targetRect.y, 0x000000);
    }

    private static Rectangle getTargetRect(ResolvedGlyphComponent glyph) {
        Rectangle slotRect = switch (glyph.slot()) {
            case TOP -> new Rectangle(48, 0, 64, 64);
            case RIGHT -> new Rectangle(96, 48, 64, 64);
            case BOTTOM -> new Rectangle(48, 96, 64, 64);
            case LEFT -> new Rectangle(0, 48, 64, 64);
        };

        int offsetX = switch (glyph.componentOrder()) {
            case 0 -> 0;
            case 1 -> 8;
            case 2 -> -8;
            case 3 -> 4;
            default -> 0;
        };

        int offsetY = switch (glyph.componentOrder()) {
            case 0 -> 0;
            case 1 -> 4;
            case 2 -> -4;
            case 3 -> 8;
            default -> 0;
        };

        return new Rectangle(slotRect.x + offsetX, slotRect.y + offsetY, 64, 64);
    }

    private static BufferedImage rotateImage(BufferedImage source, float rotationDegrees, float scale) {
        BufferedImage working = new BufferedImage(COMPONENT_SIZE, COMPONENT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = working.createGraphics();

        try {
            AffineTransform transform = new AffineTransform();
            transform.translate(COMPONENT_SIZE / 2.0, COMPONENT_SIZE / 2.0);
            transform.rotate(Math.toRadians(rotationDegrees));
            transform.scale(scale, scale);
            transform.translate(-COMPONENT_SIZE / 2.0, -COMPONENT_SIZE / 2.0);

            graphics.drawImage(source, transform, null);
        } finally {
            graphics.dispose();
        }

        return working;
    }

    private static void blendImage(BufferedImage target, BufferedImage source, int targetX, int targetY, int targetColor) {
        for (int x = 0; x < source.getWidth(); x++) {
            int tx = targetX + x;
            if (tx < 0 || tx >= target.getWidth()) {
                continue;
            }

            for (int y = 0; y < source.getHeight(); y++) {
                int ty = targetY + y;
                if (ty < 0 || ty >= target.getHeight()) {
                    continue;
                }

                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }

                int existing = target.getRGB(tx, ty);
                int blended = blend(targetColor, argb, existing);
                target.setRGB(tx, ty, blended);
            }
        }
    }

    private static int blend(int intendedColor, int currentColor, int targetColor) {
        float srcAlpha = ((currentColor >>> 24) & 0xFF) / 255.0F;

        float srcRed = ((intendedColor >>> 16) & 0xFF) / 255.0F;
        float srcGreen = ((intendedColor >>> 8) & 0xFF) / 255.0F;
        float srcBlue = (intendedColor & 0xFF) / 255.0F;

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

    private static BufferedImage buildWorkingBackground() {
        BufferedImage base = loadImage(PAGE_BACKGROUND_PATH);
        BufferedImage scaled = scaleImage(base, 5.0);
        ColorModel colorModel = scaled.getColorModel();
        return new BufferedImage(colorModel, scaled.copyData(null), colorModel.isAlphaPremultiplied(), null);
    }

    private static BufferedImage downscaleToFinal(BufferedImage source) {
        return scaleImage(source, 0.8);
    }

    private static BufferedImage scaleImage(BufferedImage source, double scale) {
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp op = new AffineTransformOp(transform, scale > 1.0
                ? AffineTransformOp.TYPE_NEAREST_NEIGHBOR
                : AffineTransformOp.TYPE_BILINEAR);

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