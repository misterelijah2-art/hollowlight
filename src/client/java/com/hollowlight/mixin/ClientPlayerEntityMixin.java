package com.hollowlight.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bridges server-authoritative dread data to the client HUD.
 *
 * Data Attachments are not automatically synced to the client by Fabric's
 * Attachment API unless explicitly marked syncable with a network codec;
 * this mod keeps dread strictly server-side (it gates gameplay effects
 * there) and is designed to inform the owning client of its own value via
 * a lightweight custom S2C payload. This mixin's tick hook is the intended
 * client-side landing point for that payload handler registration, keeping
 * client network wiring colocated with client player behavior.
 *
 * NOTE: the payload registration/handler itself (a simple record + codec,
 * registered via PayloadTypeRegistry and ClientPlayNetworking) is left as
 * a follow-up integration task — see README "Networking" roadmap item.
 * The hook below is a safe no-op today so the mixin compiles and loads
 * correctly without networking wired yet.
 */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends Entity {

	public ClientPlayerEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void hollowlight$onTick(CallbackInfo ci) {
		// Landing point for future payload-driven ClientDreadState updates.
	}
}
