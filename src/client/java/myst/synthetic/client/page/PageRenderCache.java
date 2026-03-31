package myst.synthetic.client.page;

import com.mojang.blaze3d.platform.NativeImage;
import myst.synthetic.client.render.PageRenderKey;
import myst.synthetic.page.emblem.PageEmblemResolver;
import myst.synthetic.page.emblem.ResolvedPageEmblem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PageRenderCache {

    private static final Map<PageRenderKey, Identifier> TEXTURE_IDS = new ConcurrentHashMap<>();

    private PageRenderCache() {
    }

    public static Identifier getTexture(PageRenderKey key) {
        return TEXTURE_IDS.computeIfAbsent(key, PageRenderCache::createTexture);
    }

    public static void clear() {
        TEXTURE_IDS.clear();
    }

    private static Identifier createTexture(PageRenderKey key) {
        BufferedImage image = switch (key.kind()) {
            case BLANK -> PageTextureCompositor.composeBlankPage();
            case LINK_PANEL -> PageTextureCompositor.composeLinkPanelPage();
            case SYMBOL -> {
                ResolvedPageEmblem emblem = key.symbolId() == null ? null : PageEmblemResolver.resolve(key.symbolId());
                yield PageTextureCompositor.composeSymbolPage(emblem);
            }
        };

        Identifier textureId = Identifier.fromNamespaceAndPath(
                "mystcraft-sc",
                "dynamic/page/" + key.cacheKey()
        );

        DynamicTexture dynamicTexture = new DynamicTexture(() -> "mystcraft_page_" + key.cacheKey(), toNativeImage(image));
        Minecraft.getInstance().getTextureManager().register(textureId, dynamicTexture);

        return textureId;
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output);
            output.flush();

            byte[] bytes = output.toByteArray();
            output.close();

            NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(bytes));
            if (nativeImage == null) {
                throw new IllegalStateException("Failed to convert BufferedImage to NativeImage");
            }

            return nativeImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create NativeImage for page render", e);
        }
    }
}