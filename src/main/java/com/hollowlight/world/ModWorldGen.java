package com.hollowlight.world;

import com.hollowlight.Hollowlight;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;

/**
 * Registers the Understratum dimension key/tags and hooks its exclusive
 * hostile spawns into the mod's custom "understratum" biome (defined via
 * datapack-style JSON under data/hollowlight/worldgen and dimension). This
 * class only wires biome-modification hooks in code; the dimension geometry
 * itself (a squashed, low-ceiling, jagged stone dimension lit only by faint
 * bioluminescent fungus) is data-driven per Fabric convention.
 *
 * Generation rules (implemented via the dimension/noise JSON, summarized
 * here for maintainers):
 *  - Sea level 32, world height 0-128 (compressed, claustrophobic).
 *  - Base terrain uses a heavily eroded noise router so ceilings sag low
 *    and corridors are tight — reinforces "no safe vantage point."
 *  - Ambient light level capped at 0 (no skylight; dimension has no sky).
 *  - Structures (Understratum Chapel, Bonewell Shaft) are placed via
 *    structure_set JSON with wide spacing (avg 1 per ~24 chunks) so finding
 *    one feels like a rare, dreadful discovery, not a farmable resource.
 */
public final class ModWorldGen {

	public static final Identifier UNDERSTRATUM_BIOME_ID = Identifier.of(Hollowlight.MOD_ID, "understratum");

	public static void register() {
		BiomeModifications.create(Identifier.of(Hollowlight.MOD_ID, "clear_vanilla_spawns"))
				.add(ModificationPhase.REMOVALS,
						biomeSelectionContext -> biomeSelectionContext.getBiomeKey().getValue().equals(UNDERSTRATUM_BIOME_ID),
						context -> context.getSpawnSettings().clearSpawns());

		Hollowlight.LOGGER.info("[Hollowlight] Understratum biome spawn rules applied.");
	}

	private ModWorldGen() {
	}
}
