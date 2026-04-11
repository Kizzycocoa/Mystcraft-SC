package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.property.CrystalColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class BlockCrystal extends Block {

	public static final MapCodec<BlockCrystal> CODEC = simpleCodec(BlockCrystal::new);

	public static final EnumProperty<CrystalColor> COLOR = EnumProperty.create("color", CrystalColor.class);

	// Legacy-style portal conductor state.
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final EnumProperty<Direction> SOURCE_DIRECTION = EnumProperty.create("source", Direction.class);

	public BlockCrystal(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(COLOR, CrystalColor.BABY_BLUE)
				.setValue(ACTIVE, false)
				.setValue(SOURCE_DIRECTION, Direction.DOWN));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COLOR, ACTIVE, SOURCE_DIRECTION);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		if (placed == null) {
			return null;
		}

		// Freshly placed crystals are always inactive until a receptacle pulse paths them.
		return placed
				.setValue(ACTIVE, false)
				.setValue(SOURCE_DIRECTION, Direction.DOWN);
	}

	@Override
	protected void neighborChanged(
			BlockState state,
			Level level,
			BlockPos pos,
			Block neighborBlock,
			@Nullable Orientation orientation,
			boolean movedByPiston
	) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);

		if (level.isClientSide() || myst.synthetic.util.PortalUtils.isMutating(level)) {
			return;
		}

		if (!state.getValue(ACTIVE)) {
			return;
		}

		CrystalColor requiredColor = state.getValue(COLOR);

		if (!myst.synthetic.util.PortalUtils.isCrystalStillValid(level, pos, requiredColor)) {
			level.setBlock(pos, getDisabledState(state), Block.UPDATE_ALL);
			myst.synthetic.util.PortalUtils.validateAround(level, pos, requiredColor);
			myst.synthetic.util.PortalUtils.refreshNearbyReceptacles(level, pos, requiredColor);
		}
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return state.getValue(ACTIVE) ? 15 : 0;
	}

	public static boolean isCrystal(BlockState state) {
		return state.is(MystcraftBlocks.CRYSTAL);
	}

	public static boolean isActiveCrystal(BlockState state) {
		return state.is(MystcraftBlocks.CRYSTAL) && state.getValue(ACTIVE);
	}

	public static BlockState getDisabledState(BlockState state) {
		return state
				.setValue(ACTIVE, false)
				.setValue(SOURCE_DIRECTION, Direction.DOWN);
	}

	public static BlockState getDirectedState(BlockState state, Direction sourceDirection) {
		return state
				.setValue(ACTIVE, true)
				.setValue(SOURCE_DIRECTION, sourceDirection);
	}

	public static boolean isSameColor(BlockState a, BlockState b) {
		if (!a.is(MystcraftBlocks.CRYSTAL) || !b.is(MystcraftBlocks.CRYSTAL)) {
			return false;
		}
		return a.getValue(COLOR) == b.getValue(COLOR);
	}

	public static boolean isSameColor(BlockState state, CrystalColor color) {
		return state.is(MystcraftBlocks.CRYSTAL) && state.getValue(COLOR) == color;
	}

	public static CrystalColor getColor(BlockState state) {
		return state.getValue(COLOR);
	}

	public static Direction getSourceDirection(BlockState state) {
		return state.getValue(SOURCE_DIRECTION);
	}

	public static boolean isSourcePointingToward(BlockState state, Direction toward) {
		return state.is(MystcraftBlocks.CRYSTAL)
				&& state.getValue(ACTIVE)
				&& state.getValue(SOURCE_DIRECTION) == toward;
	}

	public static boolean canConductFrom(BlockState state, CrystalColor requiredColor) {
		return state.is(MystcraftBlocks.CRYSTAL)
				&& state.getValue(COLOR) == requiredColor;
	}
}