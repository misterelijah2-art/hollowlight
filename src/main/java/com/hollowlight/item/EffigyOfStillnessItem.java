package com.hollowlight.item;

import com.hollowlight.dread.DreadManager;
import com.hollowlight.entity.stalker.StalkerEntity;
import com.hollowlight.entity.watcher.WatcherEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Effigy of Stillness — a carved figure of a kneeling player, found only in
 * ritual-chamber structures. Consuming it (right-click, single use) forces
 * every Watcher and Stalker within 24 blocks into a temporary forced
 * DORMANT/RECOVER state for 15 seconds, buying an escape window. Risk/reward:
 * using it also spikes the user's own dread by 20, because the effigy works
 * by making the Understratum briefly mistake the player for something
 * already still and dead — a psychologically costly trick.
 */
public class EffigyOfStillnessItem extends Item {

	public EffigyOfStillnessItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
		if (world.isClient) {
			return TypedActionResult.success(user.getStackInHand(hand), true);
		}
		if (!(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.pass(user.getStackInHand(hand));
		}

		Box area = player.getBoundingBox().expand(24);
		List<Entity> nearby = world.getOtherEntities(player, area);
		int affected = 0;
		for (Entity e : nearby) {
			if (e instanceof WatcherEntity watcher) {
				watcher.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, 9));
				affected++;
			} else if (e instanceof StalkerEntity stalker) {
				stalker.startRecovery(300);
				stalker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, 9));
				affected++;
			}
		}

		DreadManager.addDread(player, 20.0f);
		player.sendMessage(Text.literal(affected > 0
				? "The Understratum forgets you, for a moment."
				: "Nothing was watching, but the toll is paid all the same.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), true);

		ItemStack stack = player.getStackInHand(hand);
		stack.decrement(1);
		return TypedActionResult.success(stack, false);
	}
}
