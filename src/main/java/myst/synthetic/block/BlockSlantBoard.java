package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.block.entity.BlockEntitySlantBoard;
import myst.synthetic.block.property.WoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockSlantBoard extends BaseEntityBlock {

    public static final MapCodec<BlockSlantBoard> CODEC = simpleCodec(BlockSlantBoard::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<WoodType> WOOD = EnumProperty.create("wood", WoodType.class);

    private static final VoxelShape SHAPE =
            Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);

    private static final VoxelShape OCCLUSION_SHAPE =
            Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public BlockSlantBoard(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WOOD, WoodType.OAK));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WOOD);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        WoodType wood = getWoodType(context.getItemInHand());

        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WOOD, wood);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntitySlantBoard(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return OCCLUSION_SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    private static WoodType getWoodType(ItemStack stack) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return WoodType.OAK;
        }

        String name = customData.copyTag().getString("wood").orElse("oak");

        for (WoodType type : WoodType.values()) {
            if (type.getSerializedName().equals(name)) {
                return type;
            }
        }

        return WoodType.OAK;
    }

    public static ItemStack createVariantStack(ItemStack stack, WoodType wood) {
        CompoundTag tag = new CompoundTag();
        tag.putString("wood", wood.getSerializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
}