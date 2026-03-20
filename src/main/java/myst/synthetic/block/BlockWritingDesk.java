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
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.InteractionHand;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;

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

		boolean topHeadIsDeskTop =
				th.getBlock() instanceof BlockWritingDesk &&
						th.getValue(IS_TOP);

		boolean topFootIsDeskTop =
				tf.getBlock() instanceof BlockWritingDesk &&
						tf.getValue(IS_TOP);

		boolean topHeadOk =
				topHeadIsDeskTop &&
						!th.getValue(IS_FOOT);

		boolean topFootOk =
				topFootIsDeskTop &&
						tf.getValue(IS_FOOT);

		boolean topHeadEmpty = th.isAir();
		boolean topFootEmpty = tf.isAir();

		// Valid cases:
		// - no desk-top attached (including another desk stacked above)
		// - full valid attached top
		// - partial attached top during placement/removal
		boolean topStateOk =
				(!topHeadIsDeskTop && !topFootIsDeskTop) ||
						(topHeadOk && topFootOk) ||
						(topHeadOk && topFootEmpty) ||
						(topHeadEmpty && topFootOk);

		return bottomOk && topStateOk;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		return Collections.emptyList();
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) {
			BlockPos anchor = getAnchorPos(state, pos);
			BlockState anchorState = level.getBlockState(anchor);

			if (anchorState.getBlock() instanceof BlockWritingDesk) {
				Direction facing = getDeskFacing(anchorState);
				BlockPos footOffset = getFootOffset(facing);

				BlockPos bottomHead = anchor;
				BlockPos bottomFoot = anchor.offset(footOffset);
				BlockPos topHead = bottomHead.above();
				BlockPos topFoot = bottomFoot.above();

				boolean hasTop =
						level.getBlockState(topHead).getBlock() instanceof BlockWritingDesk &&
								level.getBlockState(topFoot).getBlock() instanceof BlockWritingDesk &&
								level.getBlockState(topHead).getValue(IS_TOP) &&
								level.getBlockState(topFoot).getValue(IS_TOP);

				if (isTop(state)) {
					// Remove only the top.
					if (!pos.equals(topHead) && level.getBlockState(topHead).getBlock() instanceof BlockWritingDesk) {
						level.setBlock(topHead, Blocks.AIR.defaultBlockState(), 35);
					}
					if (!pos.equals(topFoot) && level.getBlockState(topFoot).getBlock() instanceof BlockWritingDesk) {
						level.setBlock(topFoot, Blocks.AIR.defaultBlockState(), 35);
					}

					if (!player.isCreative()) {
						Block.popResource(level, pos, new ItemStack(MystcraftItems.WRITING_DESK_TOP));
					}
				} else {
					// Remove whole structure.
					if (!pos.equals(bottomHead) && level.getBlockState(bottomHead).getBlock() instanceof BlockWritingDesk) {
						level.setBlock(bottomHead, Blocks.AIR.defaultBlockState(), 35);
					}
					if (!pos.equals(bottomFoot) && level.getBlockState(bottomFoot).getBlock() instanceof BlockWritingDesk) {
						level.setBlock(bottomFoot, Blocks.AIR.defaultBlockState(), 35);
					}
					if (level.getBlockState(topHead).getBlock() instanceof BlockWritingDesk) {
						level.setBlock(topHead, Blocks.AIR.defaultBlockState(), 35);
					}
					if (level.getBlockState(topFoot).getBlock() instanceof BlockWritingDesk) {
						level.setBlock(topFoot, Blocks.AIR.defaultBlockState(), 35);
					}

					if (!player.isCreative()) {
						Block.popResource(level, pos, new ItemStack(this.asItem()));

						if (hasTop) {
							Block.popResource(level, pos.above(), new ItemStack(MystcraftItems.WRITING_DESK_TOP));
						}
					}
				}
			}
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return isTop(state)
				? new ItemStack(MystcraftItems.WRITING_DESK_TOP)
				: new ItemStack(this.asItem());
	}
	@Override
	protected InteractionResult useItemOn(
			ItemStack stack,
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hit
	) {
		if (!stack.is(MystcraftItems.WRITING_DESK_TOP)) {
			return InteractionResult.PASS;
		}

		if (isTop(state)) {
			return InteractionResult.FAIL;
		}

		BlockPos anchor = getAnchorPos(state, pos);
		BlockState anchorState = level.getBlockState(anchor);

		if (!(anchorState.getBlock() instanceof BlockWritingDesk)) {
			return InteractionResult.FAIL;
		}

		Direction facing = getDeskFacing(anchorState);
		BlockPos footOffset = getFootOffset(facing);

		BlockPos topHead = anchor.above();
		BlockPos topFoot = topHead.offset(footOffset);

		if (!level.getBlockState(topHead).isAir()) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(topFoot).isAir()) {
			return InteractionResult.FAIL;
		}

		BlockState topState = defaultBlockState()
				.setValue(FACING, facing)
				.setValue(IS_TOP, true)
				.setValue(IS_FOOT, false);

		if (!level.isClientSide()) {
			level.setBlock(topHead, topState, 3);
			level.setBlock(topFoot, topState.setValue(IS_FOOT, true), 3);

			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}

		return InteractionResult.SUCCESS;
	}
}