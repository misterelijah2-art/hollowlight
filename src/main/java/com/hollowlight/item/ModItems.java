package com.hollowlight.item;

import com.hollowlight.Hollowlight;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Custom item registrations. Each item has a unique behavioral override
 * rather than being a pure stat-stick — see individual classes and the
 * design doc "Custom Items" section for lore/acquisition of each.
 */
public final class ModItems {

	public static Item RESONANT_LANTERN;
	public static Item EFFIGY_OF_STILLNESS;
	public static Item TUNING_FORK_OF_THE_HOLLOW;
	public static Item STARSHARD_FRAGMENT;

	public static void register() {
		RESONANT_LANTERN = register("resonant_lantern", new ResonantLanternItem(new Item.Settings().maxCount(1)));
		EFFIGY_OF_STILLNESS = register("effigy_of_stillness", new EffigyOfStillnessItem(new Item.Settings().maxCount(1)));
		TUNING_FORK_OF_THE_HOLLOW = register("tuning_fork_of_the_hollow", new TuningForkItem(new Item.Settings().maxCount(1)));
		STARSHARD_FRAGMENT = register("starshard_fragment", new Item(new Item.Settings().maxCount(16)));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(RESONANT_LANTERN);
			entries.add(EFFIGY_OF_STILLNESS);
			entries.add(TUNING_FORK_OF_THE_HOLLOW);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(STARSHARD_FRAGMENT));
	}

	private static Item register(String path, Item item) {
		Identifier id = Identifier.of(Hollowlight.MOD_ID, path);
		return Registry.register(Registries.ITEM, id, item);
	}

	private ModItems() {
	}
}
