package com.hollowlight.client.gui;

import com.hollowlight.Hollowlight;
import com.hollowlight.config.HollowlightConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Renders the dread HUD: a small meter (bottom-right, mirroring vanilla's
 * air/hunger bar placement conventions) plus a full-screen vignette whose
 * opacity scales with dread once above DreadManager.THRESHOLD_UNEASE.
 * Reads dread client-side via {@link com.hollowlight.client.ClientDreadState},
 * a cache populated by the client player mixin/network hook (see mixin
 * package Networking TODO), since attachments are server-authoritative and
 * must be explicitly synced to the client.
 *
 * TODO(art): replace the flat-color meter fill with a real sprite at
 * assets/hollowlight/textures/gui/dread_icon.png; texture path is already
 * wired below for when the asset exists.
 */
public final class DreadHud {

	private static final Identifier DREAD_ICON = Identifier.of(Hollowlight.MOD_ID, "textures/gui/dread_icon.png");

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) return;
		if (HollowlightConfig.get().disableScreenDistortion()) {
			renderMeterOnly(context);
			return;
		}

		float dread = com.hollowlight.client.ClientDreadState.getLastKnownDread();
		if (dread <= com.hollowlight.dread.DreadManager.THRESHOLD_UNEASE) {
			renderMeterOnly(context);
			return;
		}

		float intensity = MathHelper.clamp((dread - com.hollowlight.dread.DreadManager.THRESHOLD_UNEASE) / 70f, 0f, 1f);
		int alpha = (int) (intensity * 140);
		int color = (alpha << 24) | 0x100005;

		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();
		int edge = Math.max(20, (int) (40 * intensity));

		context.fill(0, 0, width, edge, color);
		context.fill(0, height - edge, width, height, color);
		context.fill(0, 0, edge, height, color);
		context.fill(width - edge, 0, width, height, color);

		renderMeterOnly(context);
	}

	private static void renderMeterOnly(DrawContext context) {
		float dread = com.hollowlight.client.ClientDreadState.getLastKnownDread();
		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();
		int barX = width - 110;
		int barY = height - 39;
		int barWidth = 100;
		int filled = (int) (barWidth * (dread / 100f));

		context.fill(barX, barY, barX + barWidth, barY + 4, 0x66000000);
		int fillColor = dread >= com.hollowlight.dread.DreadManager.THRESHOLD_BREAK ? 0xFFAA0000
				: dread >= com.hollowlight.dread.DreadManager.THRESHOLD_TREMORS ? 0xFFCC6600
				: 0xFF446644;
		context.fill(barX, barY, barX + filled, barY + 4, fillColor);
	}

	private DreadHud() {
	}
}
