package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.property.CrystalColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockLinkPortal extends TransparentBlock {

    public static final MapCodec<BlockLinkPortal> CODEC = simpleCodec(BlockLinkPortal::new);

    public static final EnumProperty<CrystalColor> COLOR = EnumProperty.create("color", CrystalColor.class);
    public static final EnumProperty<Direction> SOURCE_DIRECTION = EnumProperty.create("source", Direction.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<Direction.Axis> RENDER_ROTATION =
            EnumProperty.create("renderface", Direction.Axis.class);
    public static final BooleanProperty HAS_ROTATION = BooleanProperty.create("hasface");

    private static final VoxelShape SHAPE_EW = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_UD = Block.box(0.0, 6.0, 0.0, 16.0, 10.0, 16.0);
    private static final VoxelShape SHAPE_NS = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape SHAPE_FULL = Shapes.block();

    public BlockLinkPortal(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COLOR, CrystalColor.BABY_BLUE)
                .setValue(SOURCE_DIRECTION, Direction.DOWN)
                .setValue(ACTIVE, false)
                .setValue(HAS_ROTATION, false)
                .setValue(RENDER_ROTATION, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR, SOURCE_DIRECTION, ACTIVE, HAS_ROTATION, RENDER_ROTATION);
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

        if (!state.getValue(ACTIVE)) {
            return;
        }

        myst.synthetic.util.PortalUtils.validatePortal(level, pos);
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier applier,
            boolean pastEdges
    ) {
        if (level.isClientSide()) {
            return;
        }

        // Next step hooks in here:
        // 1. Trace back to the receptacle via source directions
        // 2. Get the stored link/descriptive book
        // 3. Trigger the book's portal collision logic
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    public static boolean isPortalBlock(BlockState state) {
        return state.is(MystcraftBlocks.LINK_PORTAL);
    }

    public static boolean isActivePortal(BlockState state) {
        return state.is(MystcraftBlocks.LINK_PORTAL) && state.getValue(ACTIVE);
    }

    public static BlockState getDisabledState(BlockState state) {
        return state
                .setValue(ACTIVE, false)
                .setValue(SOURCE_DIRECTION, Direction.DOWN)
                .setValue(HAS_ROTATION, false)
                .setValue(RENDER_ROTATION, Direction.Axis.X);
    }

    public static BlockState getDirectedState(
            BlockState state,
            CrystalColor color,
            Direction sourceDirection,
            Direction.Axis renderAxis,
            boolean hasRotation
    ) {
        return state
                .setValue(COLOR, color)
                .setValue(ACTIVE, true)
                .setValue(SOURCE_DIRECTION, sourceDirection)
                .setValue(RENDER_ROTATION, renderAxis)
                .setValue(HAS_ROTATION, hasRotation);
    }

    public static boolean canConductFrom(BlockState state, CrystalColor requiredColor) {
        return state.is(MystcraftBlocks.LINK_PORTAL)
                && state.getValue(COLOR) == requiredColor;
    }

    public static CrystalColor getColor(BlockState state) {
        return state.getValue(COLOR);
    }

    public static Direction getSourceDirection(BlockState state) {
        return state.getValue(SOURCE_DIRECTION);
    }
}