package myst.synthetic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class BookstandBookRenderHelper {

    private static final Identifier AGEBOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/entity/book/agebook.png");

    private static final Identifier LINKBOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/entity/book/linkbook.png");

    private static final Identifier VANILLA_BOOK_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/enchanting_table_book.png");

    /**
     * Legacy RenderBookstand used:
     * book.render(..., 1.05F, ...)
     *
     * Same discovery as slant board: the "open" amount is the 4th float.
     */
    private static final BookModel.State BOOKSTAND_BOOK_STATE =
            new BookModel.State(0.0F, 0.0F, 0.0F, 1.05F);

    private final BookModel bookModel;

    public BookstandBookRenderHelper() {
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
        PoseStack frozenPoseStack = copyTopPose(poseStack);

        queue.submitCustomGeometry(
                poseStack,
                this.bookModel.renderType(texture),
                (pose, consumer) -> {
                    this.bookModel.root().resetPose();
                    this.bookModel.setupAnim(BOOKSTAND_BOOK_STATE);
                    this.bookModel.renderToBuffer(
                            frozenPoseStack,
                            consumer,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            -1
                    );
                }
        );
    }

    private static PoseStack copyTopPose(PoseStack source) {
        PoseStack copy = new PoseStack();
        copy.last().pose().set(source.last().pose());
        copy.last().normal().set(source.last().normal());
        return copy;
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