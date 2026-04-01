package myst.synthetic.fluid;

import java.util.Optional;
import myst.synthetic.MystcraftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class InkFluid extends FlowingFluid {

    @Override
    public Fluid getFlowing() {
        return MystcraftFluids.FLOWING_BLACK_INK;
    }

    @Override
    public Fluid getSource() {
        return MystcraftFluids.BLACK_INK;
    }

    @Override
    public Item getBucket() {
        return MystcraftFluids.BLACK_INK_BUCKET;
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return 2;
    }

    @Override
    public int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 20;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return MystcraftFluids.BLACK_INK_BLOCK.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == MystcraftFluids.BLACK_INK || fluid == MystcraftFluids.FLOWING_BLACK_INK;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    public static final class Flowing extends InkFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static final class Still extends InkFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}