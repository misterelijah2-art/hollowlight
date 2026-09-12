package com.hollowlight.datagen;

import com.hollowlight.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * Data-generated crafting recipes for the mod's three artifacts, each
 * requiring a Starshard Fragment (obtained only from Understratum risk
 * sources) so acquisition stays tied to danger rather than pure grinding.
 */
public class ModRecipeProvider extends FabricRecipeProvider {

	public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generate(RecipeExporter exporter) {
		ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.RESONANT_LANTERN)
				.pattern("GLG")
				.pattern("LSL")
				.pattern("GLG")
				.input('G', Items.GLOWSTONE)
				.input('L', Items.LANTERN)
				.input('S', ModItems.STARSHARD_FRAGMENT)
				.criterion(hasItem(ModItems.STARSHARD_FRAGMENT), conditionsFromItem(ModItems.STARSHARD_FRAGMENT))
				.offerTo(exporter);

		ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.TUNING_FORK_OF_THE_HOLLOW)
				.pattern(" I ")
				.pattern(" S ")
				.pattern(" I ")
				.input('I', Items.IRON_NUGGET)
				.input('S', ModItems.STARSHARD_FRAGMENT)
				.criterion(hasItem(ModItems.STARSHARD_FRAGMENT), conditionsFromItem(ModItems.STARSHARD_FRAGMENT))
				.offerTo(exporter);

		ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.EFFIGY_OF_STILLNESS)
				.pattern(" C ")
				.pattern("CSC")
				.pattern(" C ")
				.input('C', Items.CLAY_BALL)
				.input('S', ModItems.STARSHARD_FRAGMENT)
				.criterion(hasItem(ModItems.STARSHARD_FRAGMENT), conditionsFromItem(ModItems.STARSHARD_FRAGMENT))
				.offerTo(exporter);
	}
}
