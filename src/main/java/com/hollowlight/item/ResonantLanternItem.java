package com.hollowlight.item;

import com.hollowlight.dread.DreadManager;
import com.hollowlight.sound.ModSounds;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Resonant Lantern — risk/reward light source.
 *
 * Acquisition: crafted from a Starshard Fragment (dropped rarely by The
 * Watcher on death, or found in Understratum structure loot) plus a normal
 * lantern and glowstone; see recipe JSON.
 *
 * Mechanical effect: while lit and held, it grants Night Vision. BUT every
 * 30 seconds of active use adds a flat dread tick, because the Lantern's
 * light is drawn from something the Understratum considers its own.
 * Right-click toggles it on/off so players can choose when to pay the cost.
 */
public class ResonantLanternItem extends Item {

	private static final int DREAD_INTERVAL_TICKS = 600;

	public ResonantLanternItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			boolean lit = isLit(stack);
			setLit(stack, !lit);
			world.playSound(null, user.getBlockPos(), ModSounds.WATCHER_BLINK, SoundCategory.PLAYERS, 0.4f, lit ? 0.7f : 1.3f);
			user.sendMessage(Text.literal(lit ? "The lantern dims." : "The lantern wakes.").formatted(Formatting.GOLD), true);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
		if (world.isClient || !selected || !isLit(stack)) return;
		if (!(entity instanceof ServerPlayerEntity player)) return;

		if (world.getTime() % 20 == 0) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 30, 0, true, false));
		}

		int ticksLit = stack.getOrCreateNbt().getInt("TicksLit") + 1;
		stack.getOrCreateNbt().putInt("TicksLit", ticksLit);
		if (ticksLit >= DREAD_INTERVAL_TICKS) {
			stack.getOrCreateNbt().putInt("TicksLit", 0);
			DreadManager.addDread(player, 3.0f);
			player.sendMessage(Text.literal("The lantern's light feels borrowed, not given.").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), true);
		}
	}

	public static boolean isLit(ItemStack stack) {
		return stack.getOrCreateNbt().getBoolean("Lit");
	}

	public static void setLit(ItemStack stack, boolean lit) {
		stack.getOrCreateNbt().putBoolean("Lit", lit);
	}
}
