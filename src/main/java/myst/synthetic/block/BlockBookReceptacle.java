package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.entity.BlockEntityBookReceptacle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.InteractionHand;

public class BlockBookReceptacle extends BaseEntityBlock {

    public static final MapCodec<BlockBookReceptacle> CODEC = simpleCodec(BlockBookReceptacle::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");

    private static final VoxelShape SHAPE_UP = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 10.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 6.0);
    private static final VoxelShape SHAPE_WEST = Block.box(10.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 6.0, 16.0, 16.0);

    public BlockBookReceptacle(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(HAS_BOOK, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos supportPos = context.getClickedPos().relative(facing.getOpposite());

        if (context.getLevel().getBlockState(supportPos).getBlock() != MystcraftBlocks.CRYSTAL) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HAS_BOOK, false);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.relative(state.getValue(FACING).getOpposite());
        return level.getBlockState(supportPos).getBlock() == MystcraftBlocks.CRYSTAL;
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

        if (!this.canSurvive(state, level, pos)) {
            if (!level.isClientSide()) {
                popResource(level, pos, new ItemStack(this));
            }
            level.removeBlock(pos, false);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBookReceptacle(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.SUCCESS;
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
        return InteractionResult.PASS;
    }
}