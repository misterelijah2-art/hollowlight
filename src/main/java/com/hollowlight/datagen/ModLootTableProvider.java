package com.hollowlight.datagen;

import com.hollowlight.entity.ModEntities;
import com.hollowlight.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * Entity loot tables: The Watcher has a rare (1/20) Starshard Fragment drop
 * on death, reinforcing that killing it (hard, since it barely moves but
 * blink-punishes aggression) is a valid alternate route to artifact
 * ingredients versus finding Chapel ritual structures.
 */
public class ModLootTableProvider extends FabricEntityLootTableProvider {

	protected ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(dataOutput, registriesFuture);
	}

	@Override
	public void generate() {
		register(ModEntities.WATCHER, LootTable.builder()
				.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.with(ItemEntry.builder(ModItems.STARSHARD_FRAGMENT).build())
						.conditionally(RandomChanceLootCondition.builder(0.05f))));

		register(ModEntities.STALKER, LootTable.builder()
				.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.with(ItemEntry.builder(net.minecraft.item.Items.BONE).build())));
	}
}
