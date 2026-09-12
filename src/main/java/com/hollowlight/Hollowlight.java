package com.hollowlight;

import com.hollowlight.dread.DreadAttachments;
import com.hollowlight.entity.ModEntities;
import com.hollowlight.event.HuntEventManager;
import com.hollowlight.item.ModItems;
import com.hollowlight.sound.ModSounds;
import com.hollowlight.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hollowlight — a cosmic-dread horror mod.
 *
 * The Understratum is a corrupted rock layer where an ancient buried
 * intelligence, the Hollow Star, still watches. Being seen is the danger.
 *
 * This class wires up all server-side registries and systems in a fixed,
 * dependency-safe order: attachments -> sounds -> items -> entities ->
 * world generation -> the hunt/ritual event manager -> tick hooks.
 */
public final class Hollowlight implements ModInitializer {

	public static final String MOD_ID = "hollowlight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[Hollowlight] The Understratum stirs. Initializing systems...");

		DreadAttachments.register();
		ModSounds.register();
		ModItems.register();
		ModEntities.register();
		ModWorldGen.register();

		ServerTickEvents.START_SERVER_TICK.register(server -> HuntEventManager.get(server).tick(server));

		LOGGER.info("[Hollowlight] Initialization complete. It has noticed you.");
	}
}
