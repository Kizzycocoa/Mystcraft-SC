package myst.synthetic.block.property;

import net.minecraft.util.StringRepresentable;

public enum CrystalColor implements StringRepresentable {
    MAROON("maroon"),
    RED("red"),
    OLIVE("olive"),
    YELLOW("yellow"),
    DARK_GREEN("dark_green"),
    GREEN("green"),
    TEAL("teal"),
    CYAN("cyan"),
    NAVY("navy"),
    BLUE("blue"),
    PURPLE("purple"),
    MAGENTA("magenta"),
    BLACK("black"),
    GREY("grey"),
    SILVER("silver"),
    WHITE("white"),
    ORANGE("orange"),
    LIME("lime"),
    CITRUS("citrus"),
    PINK("pink"),
    DARK_GREY("dark_grey"),
    BABY_BLUE("baby_blue");

    private final String name;

    CrystalColor(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}