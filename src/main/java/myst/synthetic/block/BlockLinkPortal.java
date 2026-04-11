package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockLinkPortal extends TransparentBlock {

    public static final MapCodec<BlockLinkPortal> CODEC = simpleCodec(BlockLinkPortal::new);

    public static final DirectionProperty SOURCE_DIRECTION = DirectionProperty.create("source");
    public static final BooleanProperty IS_PART_OF_PORTAL = BooleanProperty.create("active");

    public static final EnumProperty<Direction.Axis> RENDER_ROTATION = EnumProperty.create("renderface", Direction.Axis.class);
    public static final BooleanProperty HAS_ROTATION = BooleanProperty.create("hasface");

    private static final VoxelShape SHAPE_EW = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_UD = Block.box(0.0, 6.0, 0.0, 16.0, 10.0, 16.0);
    private static final VoxelShape SHAPE_NS = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape SHAPE_FULL = Shapes.block();

    public BlockLinkPortal(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HAS_ROTATION, false)
                .setValue(RENDER_ROTATION, Direction.Axis.X)
                .setValue(IS_PART_OF_PORTAL, false)
                .setValue(SOURCE_DIRECTION, Direction.DOWN));
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOURCE_DIRECTION, IS_PART_OF_PORTAL, HAS_ROTATION, RENDER_ROTATION);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(HAS_ROTATION)) {
            return SHAPE_FULL;
        }

        return switch (state.getValue(RENDER_ROTATION)) {
            case X -> SHAPE_EW;
            case Y -> SHAPE_UD;
            case Z -> SHAPE_NS;
        };
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

        if (level.isClientSide()) {
            return;
        }

        // Legacy calls PortalUtils.validatePortal(world, pos) here.
        // Wire that in once the crystal/source-direction network logic is ported.
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (level.isClientSide()) {
            return;
        }

        // Legacy:
        // 1. trace back through source-direction to the Book Receptacle
        // 2. get the inserted portal activator / linking book
        // 3. call its onPortalCollision logic
    }

    @Override
    protected boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext useContext) {
        return false;
    }

    public static boolean isPortalBlock(BlockState state) {
        return state.is(MystcraftBlocks.LINK_PORTAL);
    }

    public static boolean isPortalOrCrystal(BlockState state) {
        return state.is(MystcraftBlocks.LINK_PORTAL) || state.is(MystcraftBlocks.CRYSTAL);
    }
}