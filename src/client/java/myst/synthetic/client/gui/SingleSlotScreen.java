package myst.synthetic.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import myst.synthetic.block.entity.DisplayContentType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SingleSlotScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/single_slot.png");

    private final ItemStack stack;
    private final DisplayContentType type;

    private int leftPos;
    private int topPos;

    public SingleSlotScreen(ItemStack stack, DisplayContentType type) {
        super(Component.empty());
        this.stack = stack;
        this.type = type;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - 176) / 2;
        this.topPos = (this.height - 166) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                176,
                166,
                256,
                256
        );

        int slotX = this.leftPos + 80;
        int slotY = this.topPos + 35;

        if (!this.stack.isEmpty()) {
            guiGraphics.renderItem(this.stack, slotX, slotY);
            guiGraphics.renderItemDecorations(this.font, this.stack, slotX, slotY);
        }

        String label = switch (this.type) {
            case EMPTY -> "Empty";
            case PAPER -> "Paper";
            case PAGE -> "Page";
            case MAP -> "Map";
            case WRITABLE_BOOK -> "Book and Quill";
            case WRITTEN_BOOK -> "Written Book";
            case LINKING_BOOK -> "Linking Book";
            case DESCRIPTIVE_BOOK -> "Descriptive Book";
        };

        guiGraphics.drawString(this.font, label, this.leftPos + 8, this.topPos + 8, 0x404040, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}