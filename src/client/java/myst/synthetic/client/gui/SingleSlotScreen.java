package myst.synthetic.client.gui;

import myst.synthetic.block.entity.DisplayContentType;
import myst.synthetic.network.DisplayContainerExtractPayload;
import myst.synthetic.network.DisplayContainerInsertPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class SingleSlotScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("mystcraft-sc", "textures/gui/single_slot.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private static final int CONTAINER_SLOT_X = 80;
    private static final int CONTAINER_SLOT_Y = 35;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;

    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 142;

    private final DisplayContentType type;
    @Nullable
    private final BlockPos containerPos;

    private ItemStack displayedStack;

    private int leftPos;
    private int topPos;

    public SingleSlotScreen(ItemStack stack, DisplayContentType type) {
        this(stack, type, null);
    }

    public SingleSlotScreen(ItemStack stack, DisplayContentType type, @Nullable BlockPos containerPos) {
        super(Component.empty());
        this.displayedStack = stack.copy();
        this.type = type;
        this.containerPos = containerPos;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || stack.isEmpty()) {
            return;
        }

        List<ClientTooltipComponent> tooltip = new ArrayList<>();

        for (var line : stack.getTooltipLines(
                net.minecraft.world.item.Item.TooltipContext.EMPTY,
                minecraft.player,
                TooltipFlag.Default.NORMAL
        )) {
            tooltip.add(new ClientTextTooltip(line.getVisualOrderText()));
        }

        guiGraphics.renderTooltip(
                this.font,
                tooltip,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Inventory inventory = minecraft.player != null ? minecraft.player.getInventory() : null;

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

        int containerSlotX = this.leftPos + CONTAINER_SLOT_X;
        int containerSlotY = this.topPos + CONTAINER_SLOT_Y;

        boolean hoveringContainer = isPointInSlot(mouseX, mouseY, containerSlotX, containerSlotY);

        if (!this.displayedStack.isEmpty()) {
            guiGraphics.renderItem(this.displayedStack, containerSlotX, containerSlotY);
            guiGraphics.renderItemDecorations(this.font, this.displayedStack, containerSlotX, containerSlotY);
        }

        if (hoveringContainer) {
            guiGraphics.fill(containerSlotX, containerSlotY, containerSlotX + 16, containerSlotY + 16, 0x80FFFFFF);
        }

        if (inventory != null) {
            renderPlayerInventory(guiGraphics, inventory, mouseX, mouseY);
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

        if (hoveringContainer && !this.displayedStack.isEmpty()) {
            renderItemTooltip(guiGraphics, this.displayedStack, mouseX, mouseY);
            return;
        }

        if (inventory != null) {
            int hoveredPlayerSlot = getHoveredPlayerSlot(mouseX, mouseY);
            if (hoveredPlayerSlot >= 0) {
                ItemStack stack = inventory.getItem(hoveredPlayerSlot);
                if (!stack.isEmpty()) {
                    renderItemTooltip(guiGraphics, stack, mouseX, mouseY);
                }
            }
        }
    }

    private void renderPlayerInventory(GuiGraphics guiGraphics, Inventory inventory, int mouseX, int mouseY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + 9;
                int x = this.leftPos + PLAYER_INV_X + column * 18;
                int y = this.topPos + PLAYER_INV_Y + row * 18;

                renderInventorySlot(guiGraphics, inventory.getItem(slot), x, y, mouseX, mouseY);
            }
        }

        for (int column = 0; column < 9; column++) {
            int slot = column;
            int x = this.leftPos + HOTBAR_X + column * 18;
            int y = this.topPos + HOTBAR_Y;

            renderInventorySlot(guiGraphics, inventory.getItem(slot), x, y, mouseX, mouseY);
        }
    }

    private void renderInventorySlot(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);
        }

        if (isPointInSlot(mouseX, mouseY, x, y)) {
            guiGraphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
        }
    }

    private boolean isPointInSlot(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private int getHoveredPlayerSlot(double mouseX, double mouseY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + 9;
                int x = this.leftPos + PLAYER_INV_X + column * 18;
                int y = this.topPos + PLAYER_INV_Y + row * 18;

                if (isPointInSlot(mouseX, mouseY, x, y)) {
                    return slot;
                }
            }
        }

        for (int column = 0; column < 9; column++) {
            int slot = column;
            int x = this.leftPos + HOTBAR_X + column * 18;
            int y = this.topPos + HOTBAR_Y;

            if (isPointInSlot(mouseX, mouseY, x, y)) {
                return slot;
            }
        }

        return -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();

        int containerSlotX = this.leftPos + CONTAINER_SLOT_X;
        int containerSlotY = this.topPos + CONTAINER_SLOT_Y;

        if (this.containerPos != null && isPointInSlot(mouseX, mouseY, containerSlotX, containerSlotY)) {
            if (!this.displayedStack.isEmpty()) {
                ClientPlayNetworking.send(new DisplayContainerExtractPayload(this.containerPos));
                this.displayedStack = ItemStack.EMPTY;
            }
            return true;
        }

        if (this.containerPos != null) {
            int playerSlot = getHoveredPlayerSlot(mouseX, mouseY);
            if (playerSlot >= 0) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    ItemStack clicked = minecraft.player.getInventory().getItem(playerSlot);
                    if (!clicked.isEmpty()) {
                        ClientPlayNetworking.send(new DisplayContainerInsertPayload(this.containerPos, playerSlot));
                        this.displayedStack = clicked.copyWithCount(1);
                    }
                }
                return true;
            }
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }
}