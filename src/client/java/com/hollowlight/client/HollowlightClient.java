package com.hollowlight.client;

import com.hollowlight.client.render.StalkerEntityRenderer;
import com.hollowlight.client.render.WatcherEntityRenderer;
import com.hollowlight.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Client-side entrypoint. Registers entity renderers, the model layer the
 * Stalker's placeholder QuadrupedEntityModel needs, and the dread HUD
 * overlay. Screen-space post-processing (vignette at high dread) is
 * implemented via HUD render layers in {@link com.hollowlight.client.gui.DreadHud};
 * a true shader-based post-process pipeline is left as a roadmap TODO
 * requiring a .json shader + .fsh/.vsh asset pair.
 */
public final class HollowlightClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(StalkerEntityRenderer.MODEL_LAYER, StalkerEntityRenderer::getTexturedModelData);

		EntityRendererRegistry.register(ModEntities.WATCHER, WatcherEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.STALKER, StalkerEntityRenderer::new);

		HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
				com.hollowlight.client.gui.DreadHud.render(drawContext, tickCounter));
	}
}
