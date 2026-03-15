package myst.synthetic.block;

import net.minecraft.util.StringRepresentable;

public enum DecayType implements StringRepresentable {
	BLACK("black"),
	RED("red"),
	GREEN("green"),
	BLUE("blue"),
	PURPLE("purple"),
	YELLOW("yellow"),
	WHITE("white");

	private final String name;

	DecayType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}