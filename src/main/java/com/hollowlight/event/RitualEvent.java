package com.hollowlight.event;

import com.hollowlight.dread.DreadManager;
import com.hollowlight.sound.ModSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/**
 * The Binding Ritual: a player-triggered event found at Understratum Chapel
 * structures. Unlike HuntEventManager (ambient, world-driven), this is a
 * deliberate risk a player opts into by lighting all three ritual braziers
 * around a Chapel's altar within a time window — trading a dread spike for
 * a guaranteed Starshard Fragment.
 *
 * Trigger condition: three specific brazier block positions (defined by
 * the structure template) must all be lit within RITUAL_WINDOW_TICKS of
 * the first one being lit.
 *
 * Warning signal: after the first brazier ignites, RITUAL_CHANT begins
 * looping and the player's dread ticks up steadily — a readable cost that
 * scales with hesitation, encouraging decisive play.
 *
 * Success: all three lit in time -> a Starshard Fragment spawns above the
 * altar and dread partially resets.
 * Failure: window expires with fewer than three lit -> RITUAL_FAILURE
 * plays, a Stalker spawns adjacent to the altar as immediate punishment,
 * and no reward is given.
 *
 * This class is a static evaluator rather than a persisted structure,
 * because ritual attempts are meant to be short (under the window) and
 * don't need to survive a server restart; long-lived state belongs in
 * HuntEventManager instead.
 */
public final class RitualEvent {

	public static final int RITUAL_WINDOW_TICKS = 400;

	public record Attempt(BlockPos altarPos, long firstBrazierLitTick, int brazierLitCount) {

		public boolean isExpired(long currentTick) {
			return currentTick - firstBrazierLitTick > RITUAL_WINDOW_TICKS;
		}

		public boolean isComplete() {
			return brazierLitCount >= 3;
		}
	}

	public static void onBrazierLit(ServerWorld world, ServerPlayerEntity player, BlockPos altarPos, boolean isFirst) {
		if (isFirst) {
			world.playSound(null, altarPos, ModSounds.RITUAL_CHANT, SoundCategory.RECORDS, 1.0f, 1.0f);
			player.sendMessage(Text.literal("The braziers demand haste.").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), true);
		}
		DreadManager.addDread(player, 4.0f);
	}

	public static void resolveSuccess(ServerWorld world, ServerPlayerEntity player, BlockPos altarPos) {
		world.playSound(null, altarPos, ModSounds.RITUAL_CHANT, SoundCategory.RECORDS, 1.0f, 1.6f);
		DreadManager.resetOnEscape(player);
		net.minecraft.item.ItemStack reward = new net.minecraft.item.ItemStack(com.hollowlight.item.ModItems.STARSHARD_FRAGMENT, 1);
		net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(world,
				altarPos.getX() + 0.5, altarPos.getY() + 1.2, altarPos.getZ() + 0.5, reward);
		world.spawnEntity(itemEntity);
		player.sendMessage(Text.literal("The ritual holds. Something surfaces.").formatted(Formatting.LIGHT_PURPLE), true);
	}

	public static void resolveFailure(ServerWorld world, ServerPlayerEntity player, BlockPos altarPos) {
		world.playSound(null, altarPos, ModSounds.RITUAL_FAILURE, SoundCategory.HOSTILE, 1.0f, 0.8f);
		DreadManager.addDread(player, 18.0f);

		com.hollowlight.entity.stalker.StalkerEntity punisher = com.hollowlight.entity.ModEntities.STALKER.create(world);
		if (punisher != null) {
			punisher.refreshPositionAndAngles(altarPos.getX() + 1, altarPos.getY(), altarPos.getZ() + 1, 0, 0);
			punisher.setTarget(player);
			punisher.setStalkerState(com.hollowlight.entity.stalker.StalkerEntity.StalkerState.STALK);
			world.spawnEntity(punisher);
		}
		player.sendMessage(Text.literal("The ritual collapses. Something answers instead.").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
	}

	private RitualEvent() {
	}
}
