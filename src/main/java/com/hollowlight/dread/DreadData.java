package com.hollowlight.dread;

/**
 * Immutable record representing a player's dread state.
 *
 * dread: 0-100. 0 = perfectly calm, 100 = total psychic collapse.
 * exposure: rolling counter of how many ticks the player has been actively
 *           "perceived" by Understratum entities/geometry this session; used
 *           to compute dread gain rate and to gate hunt-event escalation.
 * lastPerceivedTick: world time of the last perception tick, used to allow
 *           natural dread decay once perception stops.
 * corruptionStage: 0-3, a coarse phase indicator (Omens/Stalking/Confrontation/
 *           Aftermath) derived from peak dread reached, used by HUD, music,
 *           and event manager to gate escalation without recomputing history.
 */
public record DreadData(float dread, int exposure, long lastPerceivedTick, int corruptionStage) {

	public static final DreadData DEFAULT = new DreadData(0f, 0, 0L, 0);
	public static final float MAX_DREAD = 100f;

	public DreadData withDread(float newDread) {
		float clamped = Math.max(0f, Math.min(MAX_DREAD, newDread));
		int stage = computeStage(clamped);
		return new DreadData(clamped, exposure, lastPerceivedTick, Math.max(corruptionStage, stage));
	}

	public DreadData withExposure(int newExposure, long tick) {
		return new DreadData(dread, newExposure, tick, corruptionStage);
	}

	public DreadData reset() {
		return DEFAULT;
	}

	private static int computeStage(float dread) {
		if (dread >= 85f) return 3;
		if (dread >= 45f) return 2;
		if (dread >= 15f) return 1;
		return 0;
	}

	public boolean isCritical() {
		return dread >= 85f;
	}

	public boolean isHigh() {
		return dread >= 45f;
	}
}
