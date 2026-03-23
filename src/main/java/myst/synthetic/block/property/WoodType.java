package myst.synthetic.block.property;

import net.minecraft.util.StringRepresentable;

public enum WoodType implements StringRepresentable {

    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    MANGROVE("mangrove"),
    CHERRY("cherry"),
    BAMBOO("bamboo"),
    PALE_OAK("pale_oak"),
    CRIMSON("crimson"),
    WARPED("warped");

    private final String name;

    WoodType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}