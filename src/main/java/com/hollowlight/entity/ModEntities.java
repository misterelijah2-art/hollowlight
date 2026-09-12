package com.hollowlight.entity;

import com.hollowlight.Hollowlight;
import com.hollowlight.entity.stalker.StalkerEntity;
import com.hollowlight.entity.watcher.WatcherEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Entity type registrations. Both entities use SpawnGroup.MONSTER so they
 * respect standard hostile-mob difficulty/spawn toggles, but neither is
 * added to any vanilla biome spawn list — they are placed exclusively by
 * ModWorldGen structure/biome spawn rules and the hunt event manager, which
 * keeps their appearances deliberate and tension-authored rather than ambient.
 */
public final class ModEntities {

	public static EntityType<WatcherEntity> WATCHER;
	public static EntityType<StalkerEntity> STALKER;

	public static void register() {
		WATCHER = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Hollowlight.MOD_ID, "watcher"),
				EntityType.Builder.create(WatcherEntity::new, SpawnGroup.MONSTER)
						.dimensions(EntityDimensions.fixed(0.7f, 2.4f))
						.maxTrackingRange(48)
						.build());

		STALKER = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Hollowlight.MOD_ID, "stalker"),
				EntityType.Builder.create(StalkerEntity::new, SpawnGroup.MONSTER)
						.dimensions(EntityDimensions.fixed(1.1f, 1.0f))
						.maxTrackingRange(40)
						.build());

		FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createAttributes());
	}

	private ModEntities() {
	}
}
