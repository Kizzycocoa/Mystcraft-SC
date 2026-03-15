package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BlockDecay extends Block {

	public static final MapCodec<BlockDecay> CODEC = simpleCodec(BlockDecay::new);

	public static final EnumProperty<DecayType> DECAY =
			EnumProperty.create("decay", DecayType.class);

	public BlockDecay(BlockBehaviour.Properties properties) {
		super(properties);

		this.registerDefaultState(
				this.stateDefinition.any().setValue(DECAY, DecayType.BLACK)
		);
	}

	@Override
	public MapCodec<BlockDecay> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DECAY);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		ItemStack stack = new ItemStack(MystcraftBlocks.BLOCKDECAY);

		stack.set(
				DataComponents.BLOCK_STATE,
				BlockItemStateProperties.EMPTY.with(DECAY, state.getValue(DECAY))
		);

		return stack;
	}
}