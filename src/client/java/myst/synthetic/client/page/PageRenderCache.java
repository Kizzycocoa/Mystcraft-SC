package myst.synthetic.client.page;

import com.mojang.blaze3d.platform.NativeImage;
import myst.synthetic.client.render.PageRenderKey;
import myst.synthetic.page.emblem.PageEmblemResolver;
import myst.synthetic.page.emblem.ResolvedPageEmblem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PageRenderCache {

    private static boolean prewarmed = false;
    private static final Map<PageRenderKey, PageRenderAsset> ASSETS = new ConcurrentHashMap<>();

    private PageRenderCache() {
    }

    public static PageRenderAsset getAsset(PageRenderKey key) {
        return ASSETS.computeIfAbsent(key, PageRenderCache::createAsset);
    }

    public static Identifier getTexture(PageRenderKey key) {
        return getAsset(key).textureId();
    }

    public static void clear() {
        ASSETS.clear();
        prewarmed = false;
    }

    private static PageRenderAsset createAsset(PageRenderKey key) {
        BufferedImage image = switch (key.kind()) {
            case BLANK -> PageTextureCompositor.composeBlankPage();
            case LINK_PANEL -> PageTextureCompositor.composeLinkPanelPage();
            case SYMBOL -> {
                ResolvedPageEmblem emblem = key.symbolId() == null ? null : PageEmblemResolver.resolve(key.symbolId());
                yield PageTextureCompositor.composeSymbolPage(emblem);
            }
            case BLANK_CONTENT -> PageTextureCompositor.composeBlankContent();
            case LINK_PANEL_CONTENT -> PageTextureCompositor.composeLinkPanelContent();
            case SYMBOL_CONTENT -> {
                ResolvedPageEmblem emblem = key.symbolId() == null ? null : PageEmblemResolver.resolve(key.symbolId());
                yield PageTextureCompositor.composeSymbolContent(emblem);
            }
        };

        Identifier textureId = Identifier.fromNamespaceAndPath(
                "mystcraft-sc",
                "dynamic/page/" + key.cacheKey()
        );

        DynamicTexture dynamicTexture = new DynamicTexture(
                () -> "mystcraft_page_" + key.cacheKey(),
                toNativeImage(image)
        );

        dynamicTexture.setFilter(false, false);

        Minecraft.getInstance().getTextureManager().register(textureId, dynamicTexture);
        return new PageRenderAsset(textureId, image);
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                nativeImage.setPixel(x, y, argb);
            }
        }

        return nativeImage;
    }

    public static void prewarmAll() {
        if (prewarmed) {
            return;
        }
        prewarmed = true;

        getAsset(new PageRenderKey(PageRenderKey.Kind.BLANK, null));
        getAsset(new PageRenderKey(PageRenderKey.Kind.LINK_PANEL, null));
        getAsset(new PageRenderKey(PageRenderKey.Kind.BLANK_CONTENT, null));
        getAsset(new PageRenderKey(PageRenderKey.Kind.LINK_PANEL_CONTENT, null));

        for (myst.synthetic.page.symbol.PageSymbol symbol : myst.synthetic.page.symbol.PageSymbolRegistry.values()) {
            getAsset(new PageRenderKey(PageRenderKey.Kind.SYMBOL, symbol.id()));
            getAsset(new PageRenderKey(PageRenderKey.Kind.SYMBOL_CONTENT, symbol.id()));
        }
    }
}