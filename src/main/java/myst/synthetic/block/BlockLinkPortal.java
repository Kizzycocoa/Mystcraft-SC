package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlocks;
import myst.synthetic.block.entity.BlockEntityBookReceptacle;
import myst.synthetic.block.property.CrystalColor;
import myst.synthetic.linking.LinkController;
import myst.synthetic.linking.LinkOptions;
import myst.synthetic.util.PortalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

        if (level.isClientSide() || PortalUtils.isMutating(level)) {
            return;
        }

        if (!state.getValue(ACTIVE)) {
            return;
        }

        PortalUtils.validatePortal(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        if (random.nextInt(4) != 0) {
            return;
        }

        int color = getClientPortalColor(level, pos);

        for (int i = 0; i < 2; i++) {
            spawnPortalParticle(level, pos, state, random, color);
        }
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

        if (!state.getValue(ACTIVE)) {
            return;
        }

        if (!entity.isAlive() || entity.isPassenger() || entity.isVehicle()) {
            return;
        }

        BlockEntityBookReceptacle receptacle = PortalUtils.getOwningReceptacle(level, pos);
        if (receptacle == null) {
            level.removeBlock(pos, false);
            return;
        }

        if (!receptacle.hasValidPortalBook() || !receptacle.hasValidSupportCrystal()) {
            level.removeBlock(pos, false);
            return;
        }

        ItemStack book = receptacle.getBook();
        if (book.isEmpty()) {
            level.removeBlock(pos, false);
            return;
        }

        CustomData customData = book.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();
        LinkOptions info = new LinkOptions(tag);

        String targetDimension = info.getDimensionUID();
        if (targetDimension == null || targetDimension.isBlank()) {
            return;
        }

        String currentDimension = extractDimensionId(level.dimension().toString());
        if (currentDimension.equals(targetDimension)) {
            return;
        }

        LinkController.travelEntity(level, entity, info);
    }

    private static String extractDimensionId(String raw) {
        int slash = raw.lastIndexOf('/');
        int end = raw.lastIndexOf(']');

        if (slash >= 0 && end > slash) {
            return raw.substring(slash + 1, end).trim();
        }

        return raw;
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

    private static int getClientPortalColor(Level level, BlockPos pos) {
        BlockEntityBookReceptacle receptacle = PortalUtils.getOwningReceptacle(level, pos);
        if (receptacle == null) {
            return 0xFFFFFF;
        }
        return receptacle.getPortalColor();
    }

    private static void spawnPortalParticle(
            Level level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            int color
    ) {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        switch (state.getValue(RENDER_ROTATION)) {
            case X -> {
                x = centerX + (random.nextBoolean() ? 0.30D : -0.30D);
                y = pos.getY() + random.nextDouble();
                z = pos.getZ() + random.nextDouble();
                vx = (random.nextDouble() - 0.5D) * 0.04D;
                vy = (random.nextDouble() - 0.5D) * 0.04D;
                vz = (random.nextDouble() - 0.5D) * 0.04D;
            }
            case Y -> {
                x = pos.getX() + random.nextDouble();
                y = centerY + (random.nextBoolean() ? 0.30D : -0.30D);
                z = pos.getZ() + random.nextDouble();
                vx = (random.nextDouble() - 0.5D) * 0.04D;
                vy = (random.nextDouble() - 0.5D) * 0.04D;
                vz = (random.nextDouble() - 0.5D) * 0.04D;
            }
            case Z -> {
                x = pos.getX() + random.nextDouble();
                y = pos.getY() + random.nextDouble();
                z = centerZ + (random.nextBoolean() ? 0.30D : -0.30D);
                vx = (random.nextDouble() - 0.5D) * 0.04D;
                vy = (random.nextDouble() - 0.5D) * 0.04D;
                vz = (random.nextDouble() - 0.5D) * 0.04D;
            }
            default -> {
                x = pos.getX() + random.nextDouble();
                y = pos.getY() + random.nextDouble();
                z = pos.getZ() + random.nextDouble();
                vx = 0.0D;
                vy = 0.0D;
                vz = 0.0D;
            }
        }

        level.addParticle(
                new DustParticleOptions(color, 1.0F),
                x, y, z,
                vx, vy, vz
        );
    }
}