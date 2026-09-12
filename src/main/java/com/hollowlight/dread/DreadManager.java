package com.hollowlight.dread;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Central API for reading/mutating a player's dread. All other systems
 * (entity AI perception, ritual events, item effects) should go through
 * this class rather than touching the attachment directly, so gain/decay
 * curves and gameplay-effect thresholds stay in one place.
 */
public final class DreadManager {

	public static final float THRESHOLD_UNEASE = 15f;
	public static final float THRESHOLD_TREMORS = 45f;
	public static final float THRESHOLD_BREAK = 75f;
	public static final float THRESHOLD_COLLAPSE = 85f;

	private DreadManager() {
	}

	public static DreadData get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(DreadAttachments.DREAD);
	}

	public static void set(ServerPlayerEntity player, DreadData data) {
		player.setAttached(DreadAttachments.DREAD, data);
	}

	public static void addDread(ServerPlayerEntity player, float amount) {
		if (amount <= 0f) return;
		float scaled = amount * com.hollowlight.config.HollowlightConfig.get().dreadGainMultiplier();
		DreadData current = get(player);
		float before = current.dread();
		DreadData updated = current.withDread(current.dread() + scaled);
		set(player, updated);
		applyThresholdEffects(player, before, updated.dread());
	}

	public static void decay(ServerPlayerEntity player, long worldTime) {
		DreadData current = get(player);
		if (worldTime - current.lastPerceivedTick() < 100) {
			return;
		}
		if (current.dread() <= 0f) return;
		float decayRate = 0.5f * com.hollowlight.config.HollowlightConfig.get().dreadDecayMultiplier();
		set(player, current.withDread(current.dread() - decayRate));
	}

	public static void markPerceived(ServerPlayerEntity player, long worldTime) {
		DreadData current = get(player);
		set(player, current.withExposure(current.exposure() + 1, worldTime));
	}

	public static void resetOnEscape(ServerPlayerEntity player) {
		DreadData current = get(player);
		set(player, new DreadData(MathHelper.clamp(current.dread() - 40f, 0f, 100f), 0, current.lastPerceivedTick(), current.corruptionStage()));
	}

	private static void applyThresholdEffects(ServerPlayerEntity player, float before, float after) {
		if (after >= THRESHOLD_COLLAPSE && before < THRESHOLD_COLLAPSE) {
			player.sendMessage(net.minecraft.text.Text.literal("Something has fully noticed you.").formatted(net.minecraft.util.Formatting.DARK_RED, net.minecraft.util.Formatting.ITALIC), true);
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
		} else if (after >= THRESHOLD_BREAK && before < THRESHOLD_BREAK) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
		} else if (after >= THRESHOLD_TREMORS && before < THRESHOLD_TREMORS) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 0));
		} else if (after >= THRESHOLD_UNEASE && before < THRESHOLD_UNEASE) {
			player.sendMessage(net.minecraft.text.Text.literal("You feel watched.").formatted(net.minecraft.util.Formatting.GRAY, net.minecraft.util.Formatting.ITALIC), true);
		}

		if (after >= THRESHOLD_TREMORS && player.getRandom().nextFloat() < 0.02f) {
			player.getWorld().playSound(null, player.getBlockPos(), com.hollowlight.sound.ModSounds.WHISPER_STINGER,
					net.minecraft.sound.SoundCategory.AMBIENT, 0.6f, 0.8f + player.getRandom().nextFloat() * 0.4f);
		}
	}
}
