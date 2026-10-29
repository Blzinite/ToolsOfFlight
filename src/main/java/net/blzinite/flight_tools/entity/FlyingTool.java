package net.blzinite.flight_tools.entity;

import net.blzinite.flight_tools.FlightToolsMod;
import net.blzinite.flight_tools.data.BoostHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Optional;
import java.util.UUID;

public class FlyingTool extends Entity {
    public static final EntityDataAccessor<Integer> BOOST = SynchedEntityData.defineId(FlyingTool.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(FlyingTool.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(FlyingTool.class, EntityDataSerializers.OPTIONAL_UUID);
    private Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    private Player rider = null;

    public FlyingTool(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public FlyingTool(Level level, ItemStack itemStack, Player player) {
        this(FlightToolsMod.FLYING_TOOL.get(), level);
        if (itemStack.has(FlightToolsMod.BOOST_HOLDER)) {
            setItem(itemStack);
            BoostHolder boostHolder = itemStack.get(FlightToolsMod.BOOST_HOLDER);
            setBoost(boostHolder.initalBoost());
        }
        rider = player;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack itemStack) {
        this.getEntityData().set(DATA_ITEM, itemStack);
    }

    public int getBoost() {
        return this.entityData.get(BOOST);
    }

    public void setBoost(int boost) {
        this.entityData.set(BOOST, boost);
    }

    public Player getRider() {
        return this.rider;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BOOST, 0);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(OWNER_ID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(BOOST, compound.getInt("Boost"));

        if (compound.contains("Item")) {
            this.entityData.set(
                    DATA_ITEM,
                    ItemStack.parse(this.registryAccess(), compound.getCompound("Item"))
                            .orElse(ItemStack.EMPTY)
            );
        }

        UUID ownerUUID = compound.hasUUID("Owner") ? compound.getUUID("Owner") : null;

        this.entityData.set(OWNER_ID, Optional.ofNullable(ownerUUID));

        this.rider = null;

        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(ownerUUID);

            if (player != null) {
                this.rider = player;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Boost", getBoost());
        compound.put("Item", this.entityData.get(DATA_ITEM).save(this.registryAccess()));

        this.entityData.get(OWNER_ID).ifPresent(uuid ->
                compound.putUUID("Owner", uuid)
        );
    }

    @Override
    public void tick() {
        this.deltaMovementOnPreviousTick = this.getDeltaMovement();
        super.tick();

        if (this.getFirstPassenger() instanceof Player player) {
            if (rider == null) {
                rider = player;
            }
            travel(player);
            tickRidden(player);
            if (this.onGround()) {
                this.retrieve();
            }
        } else {
            this.retrieve();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    private void retrieve() {
        if (this.rider != null) {
            ItemHandlerHelper.giveItemToPlayer(this.rider, this.getItem());
        }
        this.discard();
    }

    public void travel(Player rider) {
        double gravity = this.getGravity();
        this.checkSlowFallDistance();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 lookAngle = this.getLookAngle();
        float pitchRadians = this.getXRot() * (float) (Math.PI / 180.0);
        double hLookLength = Math.sqrt(lookAngle.x * lookAngle.x + lookAngle.z * lookAngle.z);
        double hSpeed = velocity.horizontalDistance();
        double LookLength = lookAngle.length();
        double pitchCosine = Math.cos(pitchRadians);
        pitchCosine = pitchCosine * pitchCosine * Math.min(1.0, LookLength / 0.4);
        velocity = this.getDeltaMovement().add(0.0, gravity * (-1.0 + pitchCosine * 0.75), 0.0);
        if (velocity.y < 0.0 && hLookLength > 0.0) {
            double downwardLift = velocity.y * -0.1 * pitchCosine;
            velocity = velocity.add(lookAngle.x * downwardLift / hLookLength, downwardLift, lookAngle.z * downwardLift / hLookLength);
        }

        if (pitchRadians < 0.0F && hLookLength > 0.0) {
            double diveAcceleration = hSpeed * (double)(-Mth.sin(pitchRadians)) * 0.04;
            velocity = velocity.add(-lookAngle.x * diveAcceleration / hLookLength, diveAcceleration * 3.2, -lookAngle.z * diveAcceleration / hLookLength);
        }

        if (hLookLength > 0.0) {
            velocity = velocity.add((lookAngle.x / hLookLength * hSpeed - velocity.x) * 0.1, 0.0, (lookAngle.z / hLookLength * hSpeed - velocity.z) * 0.1);
        }

        this.setDeltaMovement(velocity.multiply(0.99F, 0.98F, 0.99F));
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.horizontalCollision && !this.level().isClientSide) {
            double hSpeedPostCollision = this.getDeltaMovement().horizontalDistance();
            double hSpeedLoss = hSpeed - hSpeedPostCollision;
            float f1 = (float)(hSpeedLoss * 10.0 - 3.0);
            if (f1 > 0.0F) {
                rider.hurt(rider.damageSources().flyIntoWall(), f1);
            }
        }
    }

    public Vec3 getDeltaMovementLerped(float partialTicks) {
        return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), partialTicks);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player ? player : null;
    }

    protected void tickRidden(Player player) {
        this.setRot(player.getYRot(), player.getXRot()+5);
        this.yRotO = this.getYRot();
        player.fallDistance = 0;
        if (getBoost() > 0) {
            setBoost(getBoost()-1);
            Vec3 lookAngle = player.getLookAngle();
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.add(
                    lookAngle.x * 0.1 + (lookAngle.x * 1.5 - deltaMovement.x) * 0.5,
                    lookAngle.y * 0.1 + (lookAngle.y * 1.5 - deltaMovement.y) * 0.5,
                    lookAngle.z * 0.1 + (lookAngle.z * 1.5 - deltaMovement.z) * 0.5)
            );
        }
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return this.position().add(0f, 0.125f, 0f);
    }
}
