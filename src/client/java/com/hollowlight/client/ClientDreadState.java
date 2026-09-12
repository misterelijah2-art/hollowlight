package com.hollowlight.client;

/**
 * Client-side cache of the local player's last known dread value. Intended
 * to be populated by a future S2C payload handler (see mixin package
 * Networking TODO) whenever the server informs the owning client of an
 * updated dread value. Kept as a tiny static holder since only the HUD and,
 * eventually, screen-shader code need to read it, both on the render thread.
 */
public final class ClientDreadState {

	private static volatile float lastKnownDread = 0f;

	public static float getLastKnownDread() {
		return lastKnownDread;
	}

	public static void setLastKnownDread(float value) {
		lastKnownDread = value;
	}

	private ClientDreadState() {
	}
}
