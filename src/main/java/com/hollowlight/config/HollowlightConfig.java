package com.hollowlight.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hollowlight.Hollowlight;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-backed config (no external config-API dependency, so the mod
 * stays self-contained). Loaded once on first access and cached; call
 * {@link #reload()} if you need to pick up manual edits without restarting.
 */
public record HollowlightConfig(
		float dreadGainMultiplier,
		float dreadDecayMultiplier,
		float huntEventFrequencyMultiplier,
		boolean allowDaytimeHunts,
		int minPerceptionDistanceStalker,
		int minPerceptionDistanceWatcher,
		boolean disableScreenDistortion
) {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hollowlight.json");

	private static HollowlightConfig instance;

	public static HollowlightConfig defaults() {
		return new HollowlightConfig(1.0f, 1.0f, 1.0f, false, 24, 32, false);
	}

	public static HollowlightConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	public static void reload() {
		instance = load();
	}

	private static HollowlightConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				HollowlightConfig loaded = GSON.fromJson(reader, HollowlightConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException e) {
				Hollowlight.LOGGER.warn("[Hollowlight] Failed to read config, using defaults.", e);
			}
		}
		HollowlightConfig defaults = defaults();
		save(defaults);
		return defaults;
	}

	public static void save(HollowlightConfig config) {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			Hollowlight.LOGGER.warn("[Hollowlight] Failed to save config.", e);
		}
	}
}
