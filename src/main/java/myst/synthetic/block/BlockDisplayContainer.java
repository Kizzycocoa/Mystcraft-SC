package myst.synthetic.block;

import com.mojang.serialization.MapCodec;
import myst.synthetic.block.entity.BlockEntityDisplayContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BlockDisplayContainer extends BaseEntityBlock {

	protected BlockDisplayContainer(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected abstract MapCodec<? extends BaseEntityBlock> codec();

	protected abstract InteractionResult openDisplayUi(
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hit,
			BlockEntityDisplayContainer blockEntity
	);

	protected BlockEntityDisplayContainer getDisplayBlockEntity(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof BlockEntityDisplayContainer blockEntity) {
			return blockEntity;
		}
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntityDisplayContainer blockEntity = getDisplayBlockEntity(level, pos);
		if (blockEntity == null) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide() && player.isShiftKeyDown() && blockEntity.hasStoredItem()) {
			ItemStack removed = blockEntity.takeStoredItem();

			if (!removed.isEmpty()) {
				if (player.getMainHandItem().isEmpty()) {
					player.setItemInHand(InteractionHand.MAIN_HAND, removed);
				} else if (!player.addItem(removed)) {
					popResource(level, pos, removed);
				}
			}

			return InteractionResult.SUCCESS;
		}

		return openDisplayUi(state, level, pos, player, InteractionHand.MAIN_HAND, hit, blockEntity);
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
		BlockEntityDisplayContainer blockEntity = getDisplayBlockEntity(level, pos);
		if (blockEntity == null) {
			return InteractionResult.PASS;
		}

		if (blockEntity.isEmpty() && blockEntity.canAcceptDisplayItem(stack)) {
			if (!level.isClientSide()) {
				blockEntity.setStoredItem(stack.copyWithCount(1));

				if (!player.getAbilities().instabuild) {
					stack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		}

		return openDisplayUi(state, level, pos, player, hand, hit, blockEntity);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) {
			BlockEntityDisplayContainer blockEntity = getDisplayBlockEntity(level, pos);

			if (blockEntity != null && blockEntity.hasStoredItem()) {
				ItemStack stored = blockEntity.takeStoredItem();

				if (!stored.isEmpty()) {
					Block.popResource(level, pos, stored);
				}
			}
		}

		return super.playerWillDestroy(level, pos, state, player);
	}
}