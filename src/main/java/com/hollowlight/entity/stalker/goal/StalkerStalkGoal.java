package com.hollowlight.entity.stalker.goal;

import com.hollowlight.entity.stalker.StalkerEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Implements the STALK state. Deliberately paths to a remembered
 * noise/sight location rather than the player's live position, and
 * periodically "loses" the trail if the player goes quiet and stays out
 * of range, dropping back to PATROL.
 */
public class StalkerStalkGoal extends Goal {

	private final StalkerEntity stalker;
	private BlockPos lastKnownPos;
	private ServerPlayerEntity trackedPlayer;
	private int reacquireTimer = 0;
	private int loseTrailTimer = 0;

	public StalkerStalkGoal(StalkerEntity stalker) {
		this.stalker = stalker;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		if (stalker.isRecovering() || stalker.isLunging()) return false;
		ServerPlayerEntity found = stalker.findNoiseSource();
		if (found != null) {
			trackedPlayer = found;
			lastKnownPos = found.getBlockPos();
			return true;
		}
		return trackedPlayer != null && loseTrailTimer < 300;
	}

	@Override
	public boolean shouldContinue() {
		return trackedPlayer != null && trackedPlayer.isAlive() && loseTrailTimer < 300 && !stalker.isLunging();
	}

	@Override
	public void start() {
		stalker.setStalkerState(StalkerEntity.StalkerState.STALK);
		loseTrailTimer = 0;
	}

	@Override
	public void stop() {
		stalker.setStalkerState(StalkerEntity.StalkerState.PATROL);
		trackedPlayer = null;
		lastKnownPos = null;
	}

	@Override
	public void tick() {
		reacquireTimer++;
		if (reacquireTimer >= 20) {
			reacquireTimer = 0;
			ServerPlayerEntity reacquired = stalker.findNoiseSource();
			if (reacquired != null && reacquired == trackedPlayer) {
				lastKnownPos = reacquired.getBlockPos();
				loseTrailTimer = 0;
			} else {
				loseTrailTimer += 20;
			}
		}

		if (lastKnownPos == null) return;

		Vec3d targetVec = Vec3d.ofCenter(lastKnownPos);
		double distToTarget = stalker.getPos().distanceTo(targetVec);
		if (distToTarget > 2.0) {
			stalker.getNavigation().startMovingTo(targetVec.x, targetVec.y, targetVec.z, 1.15);
		} else {
			double angle = (stalker.age % 60) / 60.0 * Math.PI * 2;
			double cx = targetVec.x + Math.cos(angle) * 3.0;
			double cz = targetVec.z + Math.sin(angle) * 3.0;
			stalker.getNavigation().startMovingTo(cx, targetVec.y, cz, 0.9);
		}

		if (trackedPlayer != null) {
			stalker.getLookControl().lookAt(trackedPlayer, 30f, 30f);
			if (stalker.getVisibilityCache().canSee(trackedPlayer)) {
				lastKnownPos = trackedPlayer.getBlockPos();
				loseTrailTimer = 0;
				stalker.setTarget(trackedPlayer);
			}
		}
	}
}
