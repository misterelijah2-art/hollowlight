package com.hollowlight.component;

import com.hollowlight.Hollowlight;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Custom Data Component registrations. Minecraft 1.21.1 stores all
 * per-ItemStack custom data through the Data Component system — there is
 * no ItemStack NBT compound API in this version. Components declared here
 * back the Resonant Lantern's "lit" toggle and lit-duration tracking.
 */
public final class ModComponents {

	public static ComponentType<Boolean> LANTERN_LIT;
	public static ComponentType<Integer> LANTERN_TICKS_LIT;

	public static void register() {
		LANTERN_LIT = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				Identifier.of(Hollowlight.MOD_ID, "lantern_lit"),
				ComponentType.<Boolean>builder().codec(Codec.BOOL).build());

		LANTERN_TICKS_LIT = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				Identifier.of(Hollowlight.MOD_ID, "lantern_ticks_lit"),
				ComponentType.<Integer>builder().codec(Codec.INT).build());
	}

	private ModComponents() {
	}
}
