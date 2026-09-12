package com.hollowlight.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Registers all datagen providers. Run via `./gradlew runDatagen`.
 * Generates recipes, loot tables, advancements, and tags so the JSON in
 * src/main/resources/data stays derived from typed Java rather than
 * hand-maintained where the schema allows it.
 */
public class HollowlightDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModLootTableProvider::new);
		pack.addProvider(ModAdvancementProvider::new);
		pack.addProvider(ModItemTagProvider::new);
	}
}
