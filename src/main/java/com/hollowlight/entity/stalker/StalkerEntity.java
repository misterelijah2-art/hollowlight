package com.hollowlight.entity.stalker;

import com.hollowlight.dread.DreadManager;
import com.hollowlight.entity.stalker.goal.StalkerLungeGoal;
import com.hollowlight.entity.stalker.goal.StalkerStalkGoal;
import com.hollowlight.sound.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.List;

/**
 * The Stalker: a fast, low-to-the-ground quadruped predator that patrols the
 * Understratum's tunnels. Unlike The Watcher, it is drawn by sound and light
 * rather than sightline alone, and once it acquires a target it commits to a
 * pursuit with a distinct three-phase attack pattern rather than a simple
 * melee loop.
 *
 * AI states:
 *   PATROL  -> default WanderAroundGoal-driven roaming; listens for
 *              qualifying noise events within HEARING_RANGE.
 *   STALK   -> a noise or light source was detected: paths toward the
 *              *last known location*, not the live player position, and
 *              deliberately stays out of the player's direct line of sight
 *              when possible.
 *   LUNGE   -> once within LUNGE_RANGE and the player is detected, commits
 *              to a fast scripted charge with a short wind-up snarl,
 *              becoming briefly uninterruptible.
 *   RECOVER -> short cooldown after a lunge (hit or miss) before it can
 *              lunge again, giving the player a real window to counter or flee.
 */
public class StalkerEntity extends HostileEntity {

	private static final TrackedData<Byte> STATE = DataTracker.registerData(StalkerEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Boolean> LUNGING = DataTracker.registerData(StalkerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public enum StalkerState {
		PATROL, STALK, LUNGE, RECOVER
	}

	public static final int HEARING_RANGE = 20;
	public static final int LUNGE_RANGE = 6;

	private int recoverTimer = 0;

	public StalkerEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 18;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.MAX_HEALTH, 26.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.32)
				.add(EntityAttributes.FOLLOW_RANGE, HEARING_RANGE + 8)
				.add(EntityAttributes.ATTACK_DAMAGE, 9.0)
				.add(EntityAttributes.ATTACK_KNOCKBACK, 0.6);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(STATE, (byte) StalkerState.PATROL.ordinal());
		builder.add(LUNGING, false);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new StalkerLungeGoal(this));
		this.goalSelector.add(1, new StalkerStalkGoal(this));
		this.goalSelector.add(2, new WanderAroundGoal(this, 0.8));
		this.goalSelector.add(3, new LookAroundGoal(this));
	}

	public StalkerState getState() {
		return StalkerState.values()[this.dataTracker.get(STATE)];
	}

	public void setStalkerState(StalkerState state) {
		this.dataTracker.set(STATE, (byte) state.ordinal());
	}

	public boolean isLunging() {
		return this.dataTracker.get(LUNGING);
	}

	public void setLunging(boolean lunging) {
		this.dataTracker.set(LUNGING, lunging);
	}

	public boolean isRecovering() {
		return recoverTimer > 0;
	}

	public void startRecovery(int ticks) {
		recoverTimer = ticks;
		setStalkerState(StalkerState.RECOVER);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient()) return;

		if (recoverTimer > 0) {
			recoverTimer--;
			if (recoverTimer == 0 && getState() == StalkerState.RECOVER) {
				setStalkerState(StalkerState.PATROL);
			}
		}

		ServerWorld world = (ServerWorld) this.getWorld();
		if (this.age % 40 == 0) {
			applyProximityDread(world);
		}
	}

	private void applyProximityDread(ServerWorld world) {
		if (this.getTarget() instanceof ServerPlayerEntity player && (getState() == StalkerState.STALK || getState() == StalkerState.LUNGE)) {
			double dist = this.distanceTo(player);
			if (dist <= HEARING_RANGE) {
				DreadManager.markPerceived(player, world.getTime());
				DreadManager.addDread(player, 0.12f);
			}
		}
	}

	public ServerPlayerEntity findNoiseSource() {
		if (!(this.getWorld() instanceof ServerWorld world)) return null;
		List<ServerPlayerEntity> players = world.getPlayers();
		ServerPlayerEntity best = null;
		double bestScore = -1;
		for (ServerPlayerEntity player : players) {
			if (player.isSpectator() || player.isCreative()) continue;
			double dist = this.distanceTo(player);
			if (dist > HEARING_RANGE) continue;
			double noise = 0;
			if (player.isSprinting()) noise += 3;
			if (!player.isSneaky()) noise += 1;
			if (!world.isDay()) noise += 1;
			double lightLevel = world.getLightLevel(player.getBlockPos());
			if (lightLevel >= 8) noise += 2;
			double score = noise - (dist / HEARING_RANGE);
			if (noise > 0 && score > bestScore) {
				bestScore = score;
				best = player;
			}
		}
		return best;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.STALKER_SNARL;
	}

	@Override
	protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
		return SoundEvents.ENTITY_WOLF_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WOLF_DEATH;
	}

	@Override
	protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state) {
		this.playSound(ModSounds.STALKER_FOOTSTEP, 0.4f, 1.0f);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("RecoverTimer", recoverTimer);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		recoverTimer = nbt.getInt("RecoverTimer");
	}
}
