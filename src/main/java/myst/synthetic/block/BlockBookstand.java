package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.MystcraftItemGroups;
import myst.synthetic.block.entity.BlockEntityBookstand;
import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import myst.synthetic.block.property.WoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class BlockBookstand extends BlockDisplayContainer {

    public static final MapCodec<BlockBookstand> CODEC = simpleCodec(BlockBookstand::new);

    public static final IntegerProperty ROTATION_INDEX = IntegerProperty.create("rotindex", 0, 7);
    public static final EnumProperty<WoodType> WOOD = EnumProperty.create("wood", WoodType.class);

    // Legacy getBoundingBox:
    // new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.75, 0.875)
    private static final VoxelShape OUTLINE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    // Legacy effectively had no meaningful physical collision for movement.
    private static final VoxelShape COLLISION_SHAPE = Shapes.empty();

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

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        int rot = state.getValue(ROTATION_INDEX);

        rot = switch (rotation) {
            case NONE -> rot;
            case CLOCKWISE_90 -> (rot + 2) & 7;
            case CLOCKWISE_180 -> (rot + 4) & 7;
            case COUNTERCLOCKWISE_90 -> (rot + 6) & 7;
        };

        return state.setValue(ROTATION_INDEX, rot);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        int rot = state.getValue(ROTATION_INDEX);

        rot = switch (mirror) {
            case NONE -> rot;
            case LEFT_RIGHT -> (8 - rot) & 7;
            case FRONT_BACK -> (4 - rot) & 7;
        };

        return state.setValue(ROTATION_INDEX, rot);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBookstand(pos, state);
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
        if (!level.isClientSide()) {
            player.openMenu(blockEntity);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
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