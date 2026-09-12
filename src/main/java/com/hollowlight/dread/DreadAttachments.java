package com.hollowlight.dread;

import com.hollowlight.Hollowlight;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

/**
 * Registers the Data Attachment used to persist per-player dread state.
 * Uses Fabric's Attachment API (1.21.1+) instead of NBT capability hacks:
 * the attachment is automatically (de)serialized with the player entity
 * via the supplied codec, and persists across death/relog because the
 * attachment is stored on the player entity itself and marked persistent.
 */
public final class DreadAttachments {

	private static final Codec<DreadData> DREAD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("dread").forGetter(DreadData::dread),
			Codec.INT.fieldOf("exposure").forGetter(DreadData::exposure),
			Codec.LONG.fieldOf("last_perceived_tick").forGetter(DreadData::lastPerceivedTick),
			Codec.INT.fieldOf("corruption_stage").forGetter(DreadData::corruptionStage)
	).apply(instance, DreadData::new));

	public static AttachmentType<DreadData> DREAD;

	public static void register() {
		DREAD = AttachmentRegistry.<DreadData>builder()
				.persistent(DREAD_CODEC)
				.initializer(() -> DreadData.DEFAULT)
				.buildAndRegister(Identifier.of(Hollowlight.MOD_ID, "dread"));
	}

	private DreadAttachments() {
	}
}
