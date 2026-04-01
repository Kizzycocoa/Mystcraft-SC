package myst.synthetic.ink;

import myst.synthetic.MystcraftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class InkExposure {

    private InkExposure() {
    }

    public static boolean isEyeInInk(Entity entity) {
        Level level = entity.level();
        Vec3 eyePos = entity.getEyePosition();
        FluidState fluidState = level.getFluidState(BlockPos.containing(eyePos));
        return fluidState.is(MystcraftFluids.BLACK_INK);
    }

    public static boolean isBodyInInk(Entity entity) {
        Level level = entity.level();
        AABB box = entity.getBoundingBox().deflate(0.001D);

        double minX = box.minX;
        double midX = (box.minX + box.maxX) * 0.5D;
        double maxX = box.maxX;

        double minY = box.minY;
        double midY = (box.minY + box.maxY) * 0.5D;
        double maxY = box.maxY;

        double minZ = box.minZ;
        double midZ = (box.minZ + box.maxZ) * 0.5D;
        double maxZ = box.maxZ;

        return isInkAt(level, minX, minY, minZ)
                || isInkAt(level, midX, minY, midZ)
                || isInkAt(level, maxX, minY, maxZ)
                || isInkAt(level, minX, midY, minZ)
                || isInkAt(level, midX, midY, midZ)
                || isInkAt(level, maxX, midY, maxZ)
                || isInkAt(level, minX, maxY, minZ)
                || isInkAt(level, midX, maxY, midZ)
                || isInkAt(level, maxX, maxY, maxZ);
    }

    public static boolean isSubmergedInInk(Entity entity) {
        return isEyeInInk(entity) || isBodyInInk(entity);
    }

    private static boolean isInkAt(Level level, double x, double y, double z) {
        FluidState fluidState = level.getFluidState(BlockPos.containing(x, y, z));
        return fluidState.is(MystcraftFluids.BLACK_INK);
    }
}