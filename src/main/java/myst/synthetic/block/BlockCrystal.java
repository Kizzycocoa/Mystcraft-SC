package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.block.property.CrystalColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BlockCrystal extends Block {

	public static final MapCodec<BlockCrystal> CODEC = simpleCodec(BlockCrystal::new);
	public static final EnumProperty<CrystalColor> COLOR = EnumProperty.create("color", CrystalColor.class);

	public BlockCrystal(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, CrystalColor.BABY_BLUE));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COLOR);
	}
}