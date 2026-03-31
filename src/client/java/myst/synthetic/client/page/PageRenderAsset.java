package myst.synthetic.client.page;

import java.awt.image.BufferedImage;
import net.minecraft.resources.Identifier;

public record PageRenderAsset(
        Identifier textureId,
        BufferedImage image
) {
}