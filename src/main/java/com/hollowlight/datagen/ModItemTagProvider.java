package com.hollowlight.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

/**
 * Item tags grouping the mod's artifacts, useful for other datapacks/mods
 * to reference (e.g. curse-immunity checks) without hard dependencies.
 */
public class ModItemTagProvider extends FabricTagProvider<Item> {

	public static final TagKey<Item> HOLLOWLIGHT_ARTIFACTS = TagKey.of(RegistryKeys.ITEM, Identifier.of("hollowlight", "artifacts"));

	public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, RegistryKeys.ITEM, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
		getOrCreateTagBuilder(HOLLOWLIGHT_ARTIFACTS)
				.add(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("hollowlight", "resonant_lantern")))
				.add(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("hollowlight", "effigy_of_stillness")))
				.add(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("hollowlight", "tuning_fork_of_the_hollow")));
	}
}
