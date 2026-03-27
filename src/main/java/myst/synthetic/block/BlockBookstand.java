package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftBlockEntities;
import myst.synthetic.MystcraftItemGroups;
import myst.synthetic.block.entity.BlockEntityBookstand;
import myst.synthetic.block.property.WoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class BlockBookstand extends BaseEntityBlock {

    public static final MapCodec<BlockBookstand> CODEC = simpleCodec(BlockBookstand::new);

    public static final IntegerProperty ROTATION_INDEX = IntegerProperty.create("rotindex", 0, 7);
    public static final EnumProperty<WoodType> WOOD = EnumProperty.create("wood", WoodType.class);

    private static final VoxelShape BASE = Block.box(5.6, 0.0, 5.6, 10.4, 3.2, 10.4);
    private static final VoxelShape POST = Block.box(7.2, 1.6, 7.2, 8.8, 8.0, 8.8);
    private static final VoxelShape TOP  = Block.box(2.4, 6.4, 2.4, 13.6, 11.2, 13.6);
    private static final VoxelShape SHAPE = Shapes.or(BASE, POST, TOP);

    public BlockBookstand(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ROTATION_INDEX, 0)
                .setValue(WOOD, WoodType.OAK));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION_INDEX, WOOD);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        float yaw = context.getRotation();
        int rot = ((int) Math.floor(((yaw * 8.0F) / 360.0F) + 0.5D)) & 7;

        return this.defaultBlockState()
                .setValue(ROTATION_INDEX, rot)
                .setValue(WOOD, getWoodType(context.getItemInHand()));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBookstand(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(MystcraftItemGroups.createBookstandVariant(state.getValue(WOOD)));
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return MystcraftItemGroups.createBookstandVariant(state.getValue(WOOD));
    }

    public static ItemStack createVariantStack(ItemStack stack, WoodType wood) {
        CompoundTag tag = new CompoundTag();
        tag.putString("wood", wood.getSerializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
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
}