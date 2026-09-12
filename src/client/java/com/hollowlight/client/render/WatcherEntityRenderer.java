package com.hollowlight.client.render;

import com.hollowlight.Hollowlight;
import com.hollowlight.entity.watcher.WatcherEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/**
 * Renderer stub for The Watcher. Reuses the vanilla player model skeleton
 * as a placeholder silhouette (tall, thin humanoid, arms held straight
 * down reads correctly even before a bespoke model exists) so the entity
 * is fully visible and animated immediately.
 *
 * TODO(art): supply src/client/resources/assets/hollowlight/textures/entity/watcher.png
 * TODO(art): implement a bespoke WatcherEntityModel (featureless face, no
 *            visible mouth/eyes) and swap the model reference below.
 */
public class WatcherEntityRenderer extends MobEntityRenderer<WatcherEntity, PlayerEntityModel<WatcherEntity>> {

	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(Identifier.of(Hollowlight.MOD_ID, "watcher"), "main");
	private static final Identifier TEXTURE = Identifier.of(Hollowlight.MOD_ID, "textures/entity/watcher.png");

	public WatcherEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
	}

	@Override
	public Identifier getTexture(WatcherEntity entity) {
		return TEXTURE;
	}
}
