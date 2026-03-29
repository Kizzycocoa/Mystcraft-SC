package myst.synthetic.entity;

import myst.synthetic.LinkBookClientBridge;
import myst.synthetic.item.ItemLinkbook;
import myst.synthetic.linking.LinkOptions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EntityLinkbook extends Mob {

    private static final EntityDataAccessor<ItemStack> BOOK =
            SynchedEntityData.defineId(EntityLinkbook.class, EntityDataSerializers.ITEM_STACK);

    private static final EntityDataAccessor<String> AGE_NAME =
            SynchedEntityData.defineId(EntityLinkbook.class, EntityDataSerializers.STRING);

    private int decayTimer;

    public EntityLinkbook(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setNoAi(true);
        this.noPhysics = false;
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BOOK, ItemStack.EMPTY);
        builder.define(AGE_NAME, "");
    }

    @NotNull
    public ItemStack getBook() {
        return this.entityData.get(BOOK);
    }

    public void setBook(@NotNull ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.entityData.set(BOOK, copy);
        this.entityData.set(AGE_NAME, getDisplayNameFromBook(copy));
    }

    @NotNull
    public String getAgeName() {
        return this.entityData.get(AGE_NAME);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            this.setHealth(this.getBookHealth());
        }

        super.tick();

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ++this.decayTimer;

        if (this.decayTimer % 10000 == 0) {
            this.hurtServer(serverLevel, this.damageSources().starve(), 1.0F);
        }

        if (this.isInWaterOrRain()) {
            this.hurtServer(serverLevel, this.damageSources().drown(), 1.0F);
        }

        if (this.getBook().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }

        if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)) {
            amount *= 2.0F;
            this.igniteForSeconds(Math.max(1, (int)Math.ceil(amount)));
        }

        return super.hurtServer(level, source, amount);
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        this.updateBookHealth();
    }

    private void updateBookHealth() {
        ItemStack book = this.getBook();
        if (book.isEmpty()) {
            return;
        }

        ItemLinkbook.setHealth(book, this.getHealth());
        this.entityData.set(BOOK, book);
    }

    private float getBookHealth() {
        ItemStack book = this.getBook();
        if (book.isEmpty()) {
            return this.getMaxHealth();
        }

        return ItemLinkbook.getHealth(book);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air - 2;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected void registerGoals() {
        // Intentionally empty.
    }

    @Override
    public void travel(Vec3 travelVector) {
        // Intentionally no movement.
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Intentionally no knockback.
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (!this.level().isClientSide()) {
                player.setItemInHand(hand, this.getBook().copy());
                this.discard();
            }
            return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        if (this.level().isClientSide()) {
            LinkBookClientBridge.open(this.getBook().copy());
        }

        return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void push(Entity entity) {
        if (!this.level().isClientSide() && entity instanceof MinecartHopper hopper) {
            ItemStack book = this.getBook();
            if (book.isEmpty()) {
                return;
            }

            ItemStack remainder = HopperBlockEntity.addItem(null, hopper, book.copy(), null);

            if (remainder.isEmpty()) {
                this.discard();
            } else {
                this.setBook(remainder);
            }
            return;
        }

        super.push(entity);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putInt("DecayTimer", this.decayTimer);

        if (!this.getBook().isEmpty()) {
            output.storeNullable("Book", ItemStack.CODEC, this.getBook());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.decayTimer = input.getIntOr("DecayTimer", 0);

        ItemStack book = input.read("Book", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        if (book.isEmpty()) {
            this.discard();
            return;
        }

        this.setBook(book);
    }

    private static String getDisplayNameFromBook(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        net.minecraft.nbt.CompoundTag tag = customData != null ? customData.copyTag() : null;
        return LinkOptions.getDisplayName(tag);
    }
}