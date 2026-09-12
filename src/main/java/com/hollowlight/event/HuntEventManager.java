package com.hollowlight.event;

import com.hollowlight.Hollowlight;
import com.hollowlight.config.HollowlightConfig;
import com.hollowlight.dread.DreadManager;
import com.hollowlight.entity.ModEntities;
import com.hollowlight.entity.stalker.StalkerEntity;
import com.hollowlight.sound.ModSounds;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;

import java.util.UUID;

/**
 * Persistent world-level state machine driving "The Convergence" — the
 * mod's dynamic hunt/ritual event.
 *
 * States:
 *   DORMANT   -> default. Rolls a chance each check interval to escalate to
 *                OMEN once any player crosses the Stalking dread threshold.
 *                This is the trigger condition.
 *   OMEN      -> warning phase (20s): HUNT_HORN sound plays once, distant
 *                and muffled. No new threat yet — this is the player's
 *                readable signal to prepare or flee.
 *   ACTIVE    -> a bonus Stalker is spawned near the highest-dread player
 *                and given an aggressive target lock immediately (skips
 *                PATROL, spawns straight into STALK). Lasts until the
 *                spawned hunter dies, the player escapes, or a hard
 *                timeout elapses.
 *   RESOLVING -> either outcome triggers this 1-tick cleanup state:
 *                SUCCESS (hunter defeated or timeout survived) grants dread
 *                relief via DreadManager#resetOnEscape; FAILURE (player
 *                caught while dread already critical) spikes dread hard.
 *
 * Uses Minecraft's PersistentState so the event's phase and timers survive
 * server restarts, and drives everything from ServerTickEvents rather than
 * any busy-wait loop — a single world-attached state object advances at
 * most once per tick, cheaply.
 */
public class HuntEventManager extends PersistentState {

	private static final int OMEN_DURATION_TICKS = 400;
	private static final int ACTIVE_TIMEOUT_TICKS = 2400;
	private static final int CHECK_INTERVAL_TICKS = 100;

	private static final PersistentState.Type<HuntEventManager> TYPE = new PersistentState.Type<>(
			HuntEventManager::new,
			HuntEventManager::fromNbt,
			null
	);

	public enum Phase {
		DORMANT, OMEN, ACTIVE, RESOLVING
	}

	private Phase phase = Phase.DORMANT;
	private int timer = 0;
	private UUID huntTargetPlayer;
	private UUID spawnedHunterUuid;

	public HuntEventManager() {
	}

	public static HuntEventManager get(MinecraftServer server) {
		ServerWorld overworld = server.getOverworld();
		return overworld.getPersistentStateManager().getOrCreate(TYPE, Identifier.of(Hollowlight.MOD_ID, "hunt_event").toString());
	}

	public static HuntEventManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		HuntEventManager state = new HuntEventManager();
		state.phase = Phase.values()[nbt.getInt("Phase")];
		state.timer = nbt.getInt("Timer");
		if (nbt.containsUuid("HuntTarget")) state.huntTargetPlayer = nbt.getUuid("HuntTarget");
		if (nbt.containsUuid("SpawnedHunter")) state.spawnedHunterUuid = nbt.getUuid("SpawnedHunter");
		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		nbt.putInt("Phase", phase.ordinal());
		nbt.putInt("Timer", timer);
		if (huntTargetPlayer != null) nbt.putUuid("HuntTarget", huntTargetPlayer);
		if (spawnedHunterUuid != null) nbt.putUuid("SpawnedHunter", spawnedHunterUuid);
		return nbt;
	}

	public void tick(MinecraftServer server) {
		timer--;
		switch (phase) {
			case DORMANT -> tickDormant(server);
			case OMEN -> tickOmen(server);
			case ACTIVE -> tickActive(server);
			case RESOLVING -> tickResolving();
		}

		if (server.getTicks() % 20 == 0) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				DreadManager.decay(player, player.getWorld().getTime());
			}
		}
	}

	private void tickDormant(MinecraftServer server) {
		if (timer > 0) return;
		timer = CHECK_INTERVAL_TICKS;

		ServerPlayerEntity candidate = findHighestDreadEligiblePlayer(server);
		if (candidate == null) return;

		float chance = 0.15f * HollowlightConfig.get().huntEventFrequencyMultiplier();
		if (candidate.getRandom().nextFloat() < chance) {
			beginOmen(candidate);
		}
	}

	private void beginOmen(ServerPlayerEntity target) {
		phase = Phase.OMEN;
		timer = OMEN_DURATION_TICKS;
		huntTargetPlayer = target.getUuid();
		target.getWorld().playSound(null, target.getBlockPos(), ModSounds.HUNT_HORN, SoundCategory.HOSTILE, 0.7f, 0.6f);
		target.sendMessage(Text.literal("A horn sounds somewhere below. Something has been called.").formatted(Formatting.DARK_RED), true);
		markDirty();
	}

	private void tickOmen(MinecraftServer server) {
		if (timer > 0) return;
		ServerPlayerEntity target = resolvePlayer(server, huntTargetPlayer);
		if (target == null) {
			resetToDormant();
			return;
		}
		spawnHunter(target);
	}

	private void spawnHunter(ServerPlayerEntity target) {
		ServerWorld world = (ServerWorld) target.getWorld();
		Random random = world.getRandom();
		double angle = random.nextDouble() * Math.PI * 2;
		BlockPos spawnPos = target.getBlockPos().add(
				(int) (Math.cos(angle) * 10), 0, (int) (Math.sin(angle) * 10));

		StalkerEntity hunter = ModEntities.STALKER.create(world);
		if (hunter == null) {
			resetToDormant();
			return;
		}
		hunter.refreshPositionAndAngles(spawnPos.getX(), target.getY(), spawnPos.getZ(), 0, 0);
		hunter.setTarget(target);
		hunter.setStalkerState(StalkerEntity.StalkerState.STALK);
		hunter.setCustomName(Text.literal("The Convergence").formatted(Formatting.DARK_RED));
		world.spawnEntity(hunter);
		spawnedHunterUuid = hunter.getUuid();

		phase = Phase.ACTIVE;
		timer = ACTIVE_TIMEOUT_TICKS;
		target.sendMessage(Text.literal("It has found you.").formatted(Formatting.RED, Formatting.BOLD), true);
		markDirty();
	}

	private void tickActive(MinecraftServer server) {
		ServerPlayerEntity target = resolvePlayer(server, huntTargetPlayer);
		if (target == null) {
			resolveOutcome(server, false);
			return;
		}

		ServerWorld world = (ServerWorld) target.getWorld();
		var hunterEntity = spawnedHunterUuid != null ? world.getEntity(spawnedHunterUuid) : null;

		boolean hunterDead = hunterEntity == null || !hunterEntity.isAlive();
		boolean playerCaught = target.hurtTime > 0 && hunterEntity != null
				&& target.getBlockPos().isWithinDistance(hunterEntity.getBlockPos(), 2)
				&& DreadManager.get(target).isCritical();

		if (hunterDead) {
			resolveOutcome(server, true);
		} else if (playerCaught) {
			resolveOutcome(server, false);
		} else if (timer <= 0) {
			resolveOutcome(server, true);
		}
	}

	private void resolveOutcome(MinecraftServer server, boolean success) {
		ServerPlayerEntity target = resolvePlayer(server, huntTargetPlayer);
		if (target != null) {
			if (success) {
				DreadManager.resetOnEscape(target);
				target.sendMessage(Text.literal("The Convergence passes. For now.").formatted(Formatting.GRAY, Formatting.ITALIC), true);
			} else {
				DreadManager.addDread(target, 25.0f);
				target.getWorld().playSound(null, target.getBlockPos(), ModSounds.RITUAL_FAILURE, SoundCategory.HOSTILE, 1.0f, 1.0f);
				target.sendMessage(Text.literal("It caught up to you.").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
			}
		}
		phase = Phase.RESOLVING;
		timer = 1;
		markDirty();
	}

	private void tickResolving() {
		resetToDormant();
	}

	private void resetToDormant() {
		phase = Phase.DORMANT;
		timer = CHECK_INTERVAL_TICKS;
		huntTargetPlayer = null;
		spawnedHunterUuid = null;
		markDirty();
	}

	private ServerPlayerEntity findHighestDreadEligiblePlayer(MinecraftServer server) {
		ServerPlayerEntity best = null;
		float bestDread = -1;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			var data = DreadManager.get(player);
			if (data.isHigh() && data.dread() > bestDread) {
				best = player;
				bestDread = data.dread();
			}
		}
		return best;
	}

	private ServerPlayerEntity resolvePlayer(MinecraftServer server, UUID uuid) {
		if (uuid == null) return null;
		return server.getPlayerManager().getPlayer(uuid);
	}

	public Phase getPhase() {
		return phase;
	}
}
