package com.hollowlight.entity.stalker.goal;

import com.hollowlight.dread.DreadManager;
import com.hollowlight.entity.stalker.StalkerEntity;
import com.hollowlight.sound.ModSounds;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Implements the LUNGE state: a short, committed, high-priority charge.
 *   1. Wind-up (12 ticks): entity locks onto the target, plays STALKER_SNARL,
 *      stops moving — a readable tell the player can react to.
 *   2. Charge (up to 20 ticks or until contact): burst of velocity toward
 *      the target's position captured at wind-up end (not re-aimed
 *      mid-charge, so a sidestep can dodge it).
 *   3. Recovery: on hit or charge expiry, entity is briefly vulnerable.
 */
public class StalkerLungeGoal extends Goal {

	private static final int WINDUP_TICKS = 12;
	private static final int CHARGE_TICKS = 20;
	private static final int RECOVER_TICKS = 40;

	private final StalkerEntity stalker;
	private ServerPlayerEntity target;
	private int phaseTimer;
	private boolean charging;
	private Vec3d chargeDirection;

	public StalkerLungeGoal(StalkerEntity stalker) {
		this.stalker = stalker;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
	}

	@Override
	public boolean canStart() {
		if (stalker.isRecovering()) return false;
		ServerPlayerEntity candidate = stalker.getTarget() instanceof ServerPlayerEntity p ? p : null;
		if (candidate == null) return false;
		double dist = stalker.distanceTo(candidate);
		return dist <= StalkerEntity.LUNGE_RANGE && stalker.getVisibilityCache().canSee(candidate);
	}

	@Override
	public boolean shouldContinue() {
		return charging || (target != null && target.isAlive() && phaseTimer < WINDUP_TICKS);
	}

	@Override
	public void start() {
		target = (ServerPlayerEntity) stalker.getTarget();
		phaseTimer = 0;
		charging = false;
		stalker.setLunging(true);
		stalker.setStalkerState(StalkerEntity.StalkerState.LUNGE);
		stalker.getNavigation().stop();
		stalker.playSound(ModSounds.STALKER_SNARL, 1.4f, 0.8f);
	}

	@Override
	public void tick() {
		if (target == null) return;
		stalker.getLookControl().lookAt(target, 60f, 60f);

		if (!charging) {
			phaseTimer++;
			if (phaseTimer >= WINDUP_TICKS) {
				beginCharge();
			}
			return;
		}

		phaseTimer++;
		if (chargeDirection != null) {
			stalker.setVelocity(chargeDirection.x, stalker.getVelocity().y, chargeDirection.z);
		}

		if (stalker.getBoundingBox().expand(0.4).intersects(target.getBoundingBox())) {
			landHit();
			return;
		}

		if (phaseTimer >= WINDUP_TICKS + CHARGE_TICKS) {
			missLunge();
		}
	}

	private void beginCharge() {
		charging = true;
		stalker.playSound(ModSounds.STALKER_LUNGE, 1.5f, 1.0f);
		Vec3d toTarget = target.getPos().subtract(stalker.getPos()).normalize();
		this.chargeDirection = toTarget.multiply(1.35, 0.0, 1.35).add(0, 0.12, 0);
	}

	private void landHit() {
		target.damage(stalker.getDamageSources().mobAttack(stalker), 9.0f);
		DreadManager.addDread(target, 8.0f);
		stalker.getWorld().playSound(null, stalker.getBlockPos(), ModSounds.STALKER_LUNGE, SoundCategory.HOSTILE, 1.0f, 1.4f);
		endLunge();
	}

	private void missLunge() {
		endLunge();
	}

	private void endLunge() {
		charging = false;
		chargeDirection = null;
		stalker.setLunging(false);
		stalker.startRecovery(RECOVER_TICKS);
	}

	@Override
	public void stop() {
		stalker.setLunging(false);
	}
}
