package com.hollowlight.sound;

import com.hollowlight.Hollowlight;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Sound event registrations. Actual .ogg assets are not included (art/audio
 * asset stub); see README "Audio Plan" for exactly when each should fire.
 */
public final class ModSounds {

	public static SoundEvent AMBIENT_DRONE_LOW;
	public static SoundEvent AMBIENT_DRONE_HIGH;
	public static SoundEvent WHISPER_STINGER;
	public static SoundEvent HEARTBEAT_STINGER;
	public static SoundEvent WATCHER_DETECT;
	public static SoundEvent WATCHER_BLINK;
	public static SoundEvent STALKER_FOOTSTEP;
	public static SoundEvent STALKER_SNARL;
	public static SoundEvent STALKER_LUNGE;
	public static SoundEvent RITUAL_CHANT;
	public static SoundEvent RITUAL_FAILURE;
	public static SoundEvent HUNT_HORN;

	public static void register() {
		AMBIENT_DRONE_LOW = register("ambient.drone_low");
		AMBIENT_DRONE_HIGH = register("ambient.drone_high");
		WHISPER_STINGER = register("ambient.whisper_stinger");
		HEARTBEAT_STINGER = register("ambient.heartbeat_stinger");
		WATCHER_DETECT = register("entity.watcher.detect");
		WATCHER_BLINK = register("entity.watcher.blink");
		STALKER_FOOTSTEP = register("entity.stalker.footstep");
		STALKER_SNARL = register("entity.stalker.snarl");
		STALKER_LUNGE = register("entity.stalker.lunge");
		RITUAL_CHANT = register("event.ritual_chant");
		RITUAL_FAILURE = register("event.ritual_failure");
		HUNT_HORN = register("event.hunt_horn");
	}

	private static SoundEvent register(String path) {
		Identifier id = Identifier.of(Hollowlight.MOD_ID, path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	private ModSounds() {
	}
}
