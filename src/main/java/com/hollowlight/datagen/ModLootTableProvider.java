package com.hollowlight.datagen;

import com.hollowlight.Hollowlight;
import com.hollowlight.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Entity loot tables: The Watcher has a rare (1/20) Starshard Fragment drop
 * on death, reinforcing that killing it (hard, since it barely moves but
 * blink-punishes aggression) is a valid alternate route to artifact
 * ingredients versus finding Chapel ritual structures.
 *
 * Uses SimpleFabricLootTableProvider with LootContextTypes.ENTITY. The
 * abstract method to implement is named accept(...), inherited from
 * LootTableGenerator — not generate(...).
 */
public class ModLootTableProvider extends SimpleFabricLootTableProvider {

	public static final RegistryKey<LootTable> WATCHER_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(Hollowlight.MOD_ID, "entities/watcher"));
	public static final RegistryKey<LootTable> STALKER_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(Hollowlight.MOD_ID, "entities/stalker"));

	public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(dataOutput, registriesFuture, LootContextTypes.ENTITY);
	}

	@Override
	public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
		lootTableBiConsumer.accept(WATCHER_LOOT, LootTable.builder()
				.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.with(ItemEntry.builder(ModItems.STARSHARD_FRAGMENT).build())
						.conditionally(RandomChanceLootCondition.builder(0.05f))));

		lootTableBiConsumer.accept(STALKER_LOOT, LootTable.builder()
				.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.with(ItemEntry.builder(net.minecraft.item.Items.BONE).build())));
	}
}
