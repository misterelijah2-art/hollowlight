package com.hollowlight.item;

import com.hollowlight.entity.stalker.StalkerEntity;
import com.hollowlight.entity.watcher.WatcherEntity;
import com.hollowlight.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Tuning Fork of the Hollow — a divination tool crafted from a Starshard
 * Fragment and an iron nugget lattice. Right-click to ring it: it pulses
 * HEARTBEAT_STINGER once and for the next 5 seconds causes the player's HUD
 * to display directional pings toward any Watcher or Stalker within 48
 * blocks — including through walls (client HUD hook, see HollowlightClient).
 * Risk/reward: ringing it also emits noise, meaning any Stalker within its
 * own hearing range gets a bonus chance to notice the player, making this a
 * genuine gamble: information now versus attention later.
 */
public class TuningForkItem extends Item {

	public TuningForkItem(Settings settings) {
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

		world.playSound(null, player.getBlockPos(), ModSounds.HEARTBEAT_STINGER, SoundCategory.PLAYERS, 1.0f, 1.0f);

		Box wideArea = player.getBoundingBox().expand(48);
		List<Entity> threats = world.getOtherEntities(player, wideArea,
				e -> e instanceof WatcherEntity || e instanceof StalkerEntity);

		int pings = threats.size();
		player.sendMessage(Text.literal(pings == 0
				? "Silence answers you. For now."
				: pings + " presence(s) echo back through the stone.").formatted(Formatting.AQUA, Formatting.ITALIC), true);

		Box noiseArea = player.getBoundingBox().expand(StalkerEntity.HEARING_RANGE + 6);
		List<Entity> stalkersInEarshot = world.getOtherEntities(player, noiseArea, e -> e instanceof StalkerEntity);
		for (Entity e : stalkersInEarshot) {
			if (e instanceof StalkerEntity stalker && stalker.getTarget() == null && player.getRandom().nextFloat() < 0.35f) {
				stalker.setTarget(player);
			}
		}

		return TypedActionResult.success(player.getStackInHand(hand), false);
	}
}
