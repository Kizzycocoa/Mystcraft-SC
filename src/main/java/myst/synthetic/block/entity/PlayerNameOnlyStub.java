package myst.synthetic.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Tiny helper so the binder can make a preview book even when there is no real
 * player involved. The actual crafted result still uses the real player.
 */
public abstract class PlayerNameOnlyStub {

	private final String name;

	protected PlayerNameOnlyStub(String name) {
		this.name = name;
	}

	public String getAuthorName() {
		return this.name;
	}

	public Component getNameComponent() {
		return Component.literal(this.name);
	}
}