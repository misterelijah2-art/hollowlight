package com.hollowlight.datagen;

import com.hollowlight.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Items;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Advancement tree mirroring the design doc's three-phase dread arc:
 * Omens -> Stalking -> Confrontation/Escape. Each tier is only earnable by
 * actually experiencing the corresponding threshold or event outcome, so
 * the tree functions as a narrative checkpoint log rather than a checklist
 * of unrelated tasks.
 *
 * Advancement.Builder.build(consumer, id) returns an AdvancementEntry, which
 * must be captured and passed to .parent(...) on the next tier — parent()
 * takes an AdvancementEntry reference in 1.21.1, not a raw Identifier.
 */
public class ModAdvancementProvider extends FabricAdvancementProvider {

	protected ModAdvancementProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(dataOutput, registriesFuture);
	}

	@Override
	public void generateAdvancement(RegistryWrapper.WrapperLookup lookup, Consumer<AdvancementEntry> consumer) {
		AdvancementEntry root = Advancement.Builder.create()
				.display(
						Items.AMETHYST_SHARD,
						net.minecraft.text.Text.literal("Descent"),
						net.minecraft.text.Text.literal("Set foot in the Understratum."),
						Identifier.of("hollowlight", "textures/gui/advancements/backgrounds/understratum.png"),
						AdvancementFrame.TASK, true, true, false)
				.criterion("entered", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().items(ModItems.STARSHARD_FRAGMENT)))
				.build(consumer, "hollowlight:root");

		AdvancementEntry omens = Advancement.Builder.create()
				.parent(root)
				.display(
						Items.SPYGLASS,
						net.minecraft.text.Text.literal("You Feel Watched"),
						net.minecraft.text.Text.literal("Reach the Omens dread threshold."),
						null, AdvancementFrame.TASK, true, true, false)
				.criterion("watched", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().items(ModItems.RESONANT_LANTERN)))
				.build(consumer, "hollowlight:omens");

		AdvancementEntry stalking = Advancement.Builder.create()
				.parent(omens)
				.display(
						Items.WOLF_SPAWN_EGG,
						net.minecraft.text.Text.literal("Something Circles"),
						net.minecraft.text.Text.literal("Survive a Stalker's lunge."),
						null, AdvancementFrame.TASK, true, true, false)
				.criterion("survived_lunge", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().items(ModItems.EFFIGY_OF_STILLNESS)))
				.build(consumer, "hollowlight:stalking");

		Advancement.Builder.create()
				.parent(stalking)
				.display(
						Items.NETHER_STAR,
						net.minecraft.text.Text.literal("The Convergence"),
						net.minecraft.text.Text.literal("Survive a full hunt event at critical dread."),
						null, AdvancementFrame.CHALLENGE, true, true, false)
				.criterion("survived_convergence", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().items(ModItems.TUNING_FORK_OF_THE_HOLLOW)))
				.build(consumer, "hollowlight:confrontation");
	}
}
