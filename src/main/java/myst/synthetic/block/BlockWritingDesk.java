package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.BlockEntityDesk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class BlockWritingDesk extends BaseEntityBlock {

	public static final MapCodec<BlockWritingDesk> CODEC = simpleCodec(BlockWritingDesk::new);

	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty IS_TOP = BooleanProperty.create("istop");
	public static final BooleanProperty IS_FOOT = BooleanProperty.create("isfoot");

	public BlockWritingDesk(BlockBehaviour.Properties properties) {
		super(properties);

		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(IS_TOP, false)
				.setValue(IS_FOOT, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, IS_TOP, IS_FOOT);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	public static boolean isTop(BlockState state) {
		return state.getBlock() instanceof BlockWritingDesk && state.getValue(IS_TOP);
	}

	public static boolean isFoot(BlockState state) {
		return state.getBlock() instanceof BlockWritingDesk && state.getValue(IS_FOOT);
	}

	public static Direction getDeskFacing(BlockState state) {
		return state.getValue(FACING);
	}

	public static BlockPos getFootOffset(Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(0, 0, -1);
			case WEST -> new BlockPos(-1, 0, 0);
			case SOUTH -> new BlockPos(0, 0, 1);
			case EAST -> new BlockPos(1, 0, 0);
			default -> BlockPos.ZERO;
		};
	}

	public static BlockPos getAnchorPos(BlockState state, BlockPos pos) {
		if (isTop(state)) {
			pos = pos.below();
		}

		if (isFoot(state)) {
			Direction facing = getDeskFacing(state);
			BlockPos footOffset = getFootOffset(facing);
			return pos.offset(-footOffset.getX(), 0, -footOffset.getZ());
		}

		return pos;
	}

	public static boolean isAnchor(BlockState state) {
		return !state.getValue(IS_TOP) && !state.getValue(IS_FOOT);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return isAnchor(state) ? new BlockEntityDesk(pos, state) : null;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (isTop(state)) {
			return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);
		}
		return Shapes.block();
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);

		if (!isStructureStillValid(level, pos, state)) {
			level.removeBlock(pos, false);
		}
	}

	private boolean isStructureStillValid(Level level, BlockPos pos, BlockState state) {
		Direction facing = getDeskFacing(state);
		BlockPos anchor = getAnchorPos(state, pos);
		BlockPos footOffset = getFootOffset(facing);

		BlockPos bottomHead = anchor;
		BlockPos bottomFoot = anchor.offset(footOffset);
		BlockPos topHead = bottomHead.above();
		BlockPos topFoot = bottomFoot.above();

		BlockState bh = level.getBlockState(bottomHead);
		BlockState bf = level.getBlockState(bottomFoot);
		BlockState th = level.getBlockState(topHead);
		BlockState tf = level.getBlockState(topFoot);

		boolean bottomOk =
				bh.getBlock() instanceof BlockWritingDesk &&
						!bh.getValue(IS_TOP) &&
						!bh.getValue(IS_FOOT) &&
						bf.getBlock() instanceof BlockWritingDesk &&
						!bf.getValue(IS_TOP) &&
						bf.getValue(IS_FOOT);

		boolean topHeadOk =
				th.getBlock() instanceof BlockWritingDesk &&
						th.getValue(IS_TOP) &&
						!th.getValue(IS_FOOT);

		boolean topFootOk =
				tf.getBlock() instanceof BlockWritingDesk &&
						tf.getValue(IS_TOP) &&
						tf.getValue(IS_FOOT);

		boolean topHeadEmpty = th.isAir();
		boolean topFootEmpty = tf.isAir();

		// Allow:
		// - no top at all
		// - full top
		// - partial top during placement/removal
		boolean topStateOk =
				(topHeadEmpty && topFootEmpty) ||
						(topHeadOk && topFootOk) ||
						(topHeadOk && topFootEmpty) ||
						(topHeadEmpty && topFootOk);

		return bottomOk && topStateOk;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		BlockPos anchor = getAnchorPos(state, pos);
		removeWholeDesk(level, anchor);

		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}

	public static void removeWholeDesk(Level level, BlockPos anchor) {
		BlockState anchorState = level.getBlockState(anchor);
		if (!(anchorState.getBlock() instanceof BlockWritingDesk)) {
			return;
		}

		Direction facing = getDeskFacing(anchorState);
		BlockPos footOffset = getFootOffset(facing);

		BlockPos bottomHead = anchor;
		BlockPos bottomFoot = anchor.offset(footOffset);
		BlockPos topHead = bottomHead.above();
		BlockPos topFoot = bottomFoot.above();

		if (level.getBlockState(topFoot).getBlock() instanceof BlockWritingDesk) {
			level.removeBlock(topFoot, false);
		}
		if (level.getBlockState(topHead).getBlock() instanceof BlockWritingDesk) {
			level.removeBlock(topHead, false);
		}
		if (level.getBlockState(bottomFoot).getBlock() instanceof BlockWritingDesk) {
			level.removeBlock(bottomFoot, false);
		}
		if (level.getBlockState(bottomHead).getBlock() instanceof BlockWritingDesk) {
			level.removeBlock(bottomHead, false);
		}
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return isTop(state)
				? new ItemStack(MystcraftItems.WRITING_DESK_TOP)
				: new ItemStack(this.asItem());
	}
}