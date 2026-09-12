package com.hollowlight.entity.watcher;

import com.hollowlight.dread.DreadManager;
import com.hollowlight.sound.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Watcher: a near-motionless, humanoid-silhouette entity that stands in
 * lit or open sightlines within the Understratum. It never chases. Its
 * entire threat model is line-of-sight based dread infliction and, at close
 * range while unbroken eye contact is maintained, a "blink-step" teleport
 * that closes distance instantly when the player looks away.
 *
 * AI states:
 *   IDLE      -> no player within range or no line of sight: stands still,
 *                slow idle sway only.
 *   OBSERVING -> a player is within range AND has line of sight to it:
 *                inflicts dread per tick, plays WATCHER_DETECT once on
 *                entry, faces the player.
 *   BLINKING  -> observed player breaks line of sight for >= 1 tick while
 *                within BLINK_RANGE: entity vanishes (WATCHER_BLINK) and
 *                reappears closer to the player (never adjacent — always
 *                leaves a "startle gap" of 3-5 blocks), then returns to
 *                OBSERVING.
 *   DORMANT   -> after inflicting a large dread spike via blink, goes
 *                inert for a cooldown so it doesn't chain-blink the player
 *                to death; this is a deliberate mercy window.
 */
public class WatcherEntity extends HostileEntity {

	private static final TrackedData<Byte> STATE = DataTracker.registerData(WatcherEntity.class, TrackedDataHandlerRegistry.BYTE);

	public enum WatcherState {
		IDLE, OBSERVING, BLINKING, DORMANT
	}

	private static final int PERCEPTION_RANGE = 28;
	private static final int BLINK_RANGE = 20;
	private static final int DORMANT_COOLDOWN_TICKS = 200;

	private int dormantTimer = 0;
	private int losBrokenTicks = 0;

	public WatcherEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 12;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.MAX_HEALTH, 20.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.0)
				.add(EntityAttributes.FOLLOW_RANGE, PERCEPTION_RANGE + 4)
				.add(EntityAttributes.ATTACK_DAMAGE, 6.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(STATE, (byte) WatcherState.IDLE.ordinal());
	}

	@Override
	protected void initGoals() {
		this.goalSelector = new GoalSelector();
		this.targetSelector = new GoalSelector();
	}

	public WatcherState getState() {
		return WatcherState.values()[this.dataTracker.get(STATE)];
	}

	private void setState(WatcherState state) {
		this.dataTracker.set(STATE, (byte) state.ordinal());
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient()) return;
		ServerWorld world = (ServerWorld) this.getWorld();

		if (getState() == WatcherState.DORMANT) {
			dormantTimer--;
			if (dormantTimer <= 0) {
				setState(WatcherState.IDLE);
			}
			return;
		}

		ServerPlayerEntity target = findObservablePlayer(world);

		if (target == null) {
			if (getState() != WatcherState.IDLE) setState(WatcherState.IDLE);
			losBrokenTicks = 0;
			return;
		}

		boolean hasLineOfSight = canSee(target);
		this.getLookControl().lookAt(target, 90f, 90f);

		if (hasLineOfSight) {
			losBrokenTicks = 0;
			if (getState() != WatcherState.OBSERVING) {
				setState(WatcherState.OBSERVING);
				world.playSound(null, this.getBlockPos(), ModSounds.WATCHER_DETECT, SoundCategory.HOSTILE, 1.0f, 1.0f);
			}
			DreadManager.markPerceived(target, world.getTime());
			DreadManager.addDread(target, 0.18f);
		} else {
			losBrokenTicks++;
			double distance = this.distanceTo(target);
			if (getState() == WatcherState.OBSERVING && losBrokenTicks >= 1 && distance <= BLINK_RANGE) {
				performBlink(world, target);
			} else if (losBrokenTicks > 60) {
				setState(WatcherState.IDLE);
			}
		}
	}

	private void performBlink(ServerWorld world, ServerPlayerEntity target) {
		setState(WatcherState.BLINKING);
		world.playSound(null, this.getBlockPos(), ModSounds.WATCHER_BLINK, SoundCategory.HOSTILE, 1.0f, 0.9f);

		Random random = this.getRandom();
		double gap = 3.0 + random.nextDouble() * 2.0;
		double angle = random.nextDouble() * Math.PI * 2;
		double newX = target.getX() + Math.cos(angle) * gap;
		double newZ = target.getZ() + Math.sin(angle) * gap;

		for (int dy = 3; dy >= -3; dy--) {
			double newY = target.getY() + dy;
			if (this.teleport(newX, newY, newZ, false)) {
				break;
			}
		}

		world.playSound(null, this.getBlockPos(), ModSounds.WATCHER_BLINK, SoundCategory.HOSTILE, 1.0f, 1.3f);
		DreadManager.addDread(target, 12.0f);
		dormantTimer = DORMANT_COOLDOWN_TICKS;
		setState(WatcherState.DORMANT);
	}

	@Nullable
	private ServerPlayerEntity findObservablePlayer(ServerWorld world) {
		List<ServerPlayerEntity> players = world.getPlayers();
		ServerPlayerEntity closest = null;
		double closestDist = Double.MAX_VALUE;
		for (ServerPlayerEntity player : players) {
			if (player.isSpectator() || player.isCreative()) continue;
			double dist = this.distanceTo(player);
			if (dist <= PERCEPTION_RANGE && dist < closestDist) {
				closest = player;
				closestDist = dist;
			}
		}
		return closest;
	}

	private boolean canSee(ServerPlayerEntity player) {
		return this.getVisibilityCache().canSee(player);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("DormantTimer", dormantTimer);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		dormantTimer = nbt.getInt("DormantTimer");
	}
}
