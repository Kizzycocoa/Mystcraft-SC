package myst.synthetic.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

public class LegacyTinyToggleButton extends Button {

    private final Font font;
    private final String label;
    private final BooleanSupplier selectedSupplier;

    public LegacyTinyToggleButton(
            int x,
            int y,
            int width,
            int height,
            String label,
            Font font,
            BooleanSupplier selectedSupplier,
            OnPress onPress
    ) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.label = label;
        this.font = font;
        this.selectedSupplier = selectedSupplier;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = this.selectedSupplier.getAsBoolean();
        boolean hovered = this.isHovered();
        boolean enabled = this.active;

        int x1 = this.getX();
        int y1 = this.getY();
        int x2 = x1 + this.width;
        int y2 = y1 + this.height;

        int outerTopLeft = selected ? 0xFF3A3A3A : 0xFFA0A0A0;
        int outerBottomRight = selected ? 0xFFA0A0A0 : 0xFF3A3A3A;
        int inner = selected ? 0xFF6A6A6A : 0xFF8A8A8A;
        int textColor = enabled ? 0xFF000000 : 0xFF333333;

        guiGraphics.fill(x1, y1, x2, y2, outerBottomRight);
        guiGraphics.fill(x1, y1, x2 - 1, y2 - 1, outerTopLeft);
        guiGraphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, inner);

        if (hovered) {
            guiGraphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0x33FFFFFF);
        }

        this.drawScaledCenteredText(guiGraphics, textColor, selected ? 1 : 0);
    }

    private void drawScaledCenteredText(GuiGraphics guiGraphics, int color, int pressedOffset) {
        int availableWidth = this.width - 4;
        int textWidth = this.font.width(this.label);
        float scale = textWidth > availableWidth ? (float) availableWidth / (float) textWidth : 1.0F;

        float scaledWidth = textWidth * scale;
        float drawX = this.getX() + ((this.width - scaledWidth) / 2.0F) + pressedOffset;
        float drawY = this.getY() + ((this.height - (8.0F * scale)) / 2.0F) + pressedOffset;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(drawX, drawY);
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.drawString(this.font, this.label, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }
}