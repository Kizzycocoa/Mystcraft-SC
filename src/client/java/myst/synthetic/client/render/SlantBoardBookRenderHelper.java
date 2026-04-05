package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class SlantBoardBookRenderHelper {

    private static final Identifier AGEBOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/entity/book/agebook.png");

    private static final Identifier LINKBOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/entity/book/linkbook.png");

    private static final Identifier VANILLA_BOOK_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/enchanting_table_book.png");

    /**
     * Legacy RenderLectern used a fixed open-book render amount of about 1.22F.
     * In this BookModel API, the equivalent is the State record.
     */
    private static final BookModel.State SLANT_BOOK_STATE =
            new BookModel.State(1.22F, 0.0F, 1.0F, 0.0F);

    private final BookModel bookModel;

    public SlantBoardBookRenderHelper() {
        this.bookModel = new BookModel(BookModel.createBodyLayer().bakeRoot());
    }

    public void submitBook(
            DisplayContentType type,
            ItemStack stack,
            PoseStack poseStack,
            SubmitNodeCollector queue,
            int packedLight
    ) {
        Identifier texture = textureFor(type, stack);

        queue.submitCustomGeometry(
                poseStack,
                this.bookModel.renderType(texture),
                (pose, consumer) -> {
                    this.bookModel.root().resetPose();
                    this.bookModel.setupAnim(SLANT_BOOK_STATE);
                    this.bookModel.renderToBuffer(
                            poseStack,
                            consumer,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            -1
                    );
                }
        );
    }

    private static Identifier textureFor(DisplayContentType type, ItemStack stack) {
        return switch (type) {
            case LINKING_BOOK -> LINKBOOK_TEXTURE;
            case DESCRIPTIVE_BOOK -> AGEBOOK_TEXTURE;
            case WRITABLE_BOOK, WRITTEN_BOOK -> VANILLA_BOOK_TEXTURE;
            default -> VANILLA_BOOK_TEXTURE;
        };
    }
}