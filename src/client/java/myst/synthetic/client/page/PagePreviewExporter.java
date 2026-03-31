package myst.synthetic.client.page;

import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.page.emblem.PageEmblemResolver;
import myst.synthetic.page.emblem.ResolvedPageEmblem;
import myst.synthetic.page.symbol.PageSymbol;
import myst.synthetic.page.symbol.PageSymbolRegistry;
import net.fabricmc.loader.api.FabricLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class PagePreviewExporter {

    private static boolean exported = false;

    private PagePreviewExporter() {
    }

    public static void exportAllOnce() {
        if (exported) {
            return;
        }
        exported = true;

        Path outputDir = FabricLoader.getInstance().getGameDir()
                .resolve("mystcraft_debug_pages");

        try {
            if (Files.exists(outputDir)) {
                try (var stream = Files.list(outputDir)) {
                    stream.sorted(Comparator.reverseOrder())
                            .filter(path -> !path.equals(outputDir))
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }

            Files.createDirectories(outputDir);

            writeImage(outputDir.resolve("blank_page.png"), PageTextureCompositor.composeBlankPage());
            writeImage(outputDir.resolve("link_panel_page.png"), PageTextureCompositor.composeLinkPanelPage());

            for (PageSymbol symbol : PageSymbolRegistry.values()) {
                ResolvedPageEmblem emblem = PageEmblemResolver.resolve(symbol);
                BufferedImage image = PageTextureCompositor.composeSymbolPage(emblem);

                String filename = symbol.id().getPath().replace('/', '_') + ".png";
                writeImage(outputDir.resolve(filename), image);
            }

            MystcraftSyntheticCodex.LOGGER.info("Exported Mystcraft page previews to {}", outputDir.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export page previews", e);
        }
    }

    private static void writeImage(Path path, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", path.toFile());
    }
}