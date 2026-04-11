package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.entity.BlockEntityBookReceptacle;
import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import myst.synthetic.block.entity.DisplayItemRules;
import myst.synthetic.block.property.CrystalColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockBookReceptacle extends BlockDisplayContainer {

    public static final MapCodec<BlockBookReceptacle> CODEC = simpleCodec(BlockBookReceptacle::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final EnumProperty<CrystalColor> COLOR = EnumProperty.create("color", CrystalColor.class);
    public static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");

    private static final VoxelShape SHAPE_FLOOR = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    private static final VoxelShape SHAPE_CEILING = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 10.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 6.0);
    private static final VoxelShape SHAPE_WEST = Block.box(10.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 6.0, 16.0, 16.0);

    public BlockBookReceptacle(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACE, AttachFace.WALL)
                .setValue(FACING, Direction.SOUTH)
                .setValue(COLOR, CrystalColor.BABY_BLUE)
                .setValue(HAS_BOOK, false));
    }

    @Override
    protected MapCodec<? extends BlockDisplayContainer> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, COLOR, HAS_BOOK);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();

        AttachFace mount;
        Direction horizontalFacing;

        if (clickedFace == Direction.UP) {
            mount = AttachFace.FLOOR;
            horizontalFacing = context.getHorizontalDirection().getOpposite();
        } else if (clickedFace == Direction.DOWN) {
            mount = AttachFace.CEILING;
            horizontalFacing = context.getHorizontalDirection().getOpposite();
        } else {
            mount = AttachFace.WALL;
            horizontalFacing = clickedFace;
        }

        BlockPos pos = context.getClickedPos();
        BlockPos supportPos = getSupportPos(pos, mount, horizontalFacing);
        BlockState supportState = context.getLevel().getBlockState(supportPos);

        if (!supportState.is(MystcraftBlocks.CRYSTAL)) {
            return null;
        }

        CrystalColor color = supportState.hasProperty(BlockCrystal.COLOR)
                ? supportState.getValue(BlockCrystal.COLOR)
                : CrystalColor.BABY_BLUE;

        return this.defaultBlockState()
                .setValue(FACE, mount)
                .setValue(FACING, horizontalFacing)
                .setValue(COLOR, color)
                .setValue(HAS_BOOK, false);
    }

    public static BlockPos getSupportPos(BlockPos pos, AttachFace mount, Direction facing) {
        return switch (mount) {
            case FLOOR -> pos.below();
            case CEILING -> pos.above();
            case WALL -> pos.relative(facing.getOpposite());
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = getSupportPos(pos, state.getValue(FACE), state.getValue(FACING));
        BlockState supportState = level.getBlockState(supportPos);

        if (!supportState.is(MystcraftBlocks.CRYSTAL)) {
            return false;
        }

        return !supportState.hasProperty(BlockCrystal.COLOR)
                || supportState.getValue(BlockCrystal.COLOR) == state.getValue(COLOR);
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

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
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
        return switch (state.getValue(FACE)) {
            case FLOOR -> SHAPE_FLOOR;
            case CEILING -> SHAPE_CEILING;
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> SHAPE_NORTH;
                case SOUTH -> SHAPE_SOUTH;
                case WEST -> SHAPE_WEST;
                case EAST -> SHAPE_EAST;
                default -> SHAPE_SOUTH;
            };
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityBookReceptacle receptacle)) {
            return InteractionResult.PASS;
        }

        if (!receptacle.hasBook()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stored = receptacle.getBook();
        if (stored.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        receptacle.clearBook();
        player.setItemInHand(InteractionHand.MAIN_HAND, stored.copy());
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
        if (!(level.getBlockEntity(pos) instanceof BlockEntityBookReceptacle receptacle)) {
            return InteractionResult.PASS;
        }

        boolean validHeldBook = DisplayItemRules.canGoInBookReceptacle(stack);
        boolean hasStoredBook = receptacle.hasBook();

        if (level.isClientSide()) {
            if (validHeldBook || hasStoredBook) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // Empty receptacle: only valid books may be inserted.
        if (!hasStoredBook) {
            if (!validHeldBook || stack.isEmpty()) {
                return InteractionResult.PASS;
            }

            receptacle.setStoredItem(stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // Occupied receptacle + valid book in hand = swap.
        if (validHeldBook && !stack.isEmpty()) {
            ItemStack oldBook = receptacle.getBook().copy();
            ItemStack newBook = stack.copyWithCount(1);

            if (stack.getCount() == 1) {
                receptacle.setStoredItem(newBook);
                player.setItemInHand(hand, oldBook);
                return InteractionResult.SUCCESS;
            }

            if (!player.getInventory().add(oldBook)) {
                return InteractionResult.PASS;
            }

            receptacle.setStoredItem(newBook);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // Occupied receptacle + invalid item in hand = withdraw to inventory if possible.
        ItemStack oldBook = receptacle.getBook().copy();
        if (!player.getInventory().add(oldBook)) {
            return InteractionResult.PASS;
        }

        receptacle.clearBook();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult openDisplayUi(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            BlockEntityDisplayContainer blockEntity
    ) {
        return InteractionResult.PASS;
    }
}