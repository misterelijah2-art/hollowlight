package com.hollowlight.client.render;

import com.hollowlight.Hollowlight;
import com.hollowlight.entity.stalker.StalkerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.util.Identifier;

/**
 * Renderer stub for The Stalker. Reuses the vanilla wolf skeleton as a
 * geometry placeholder (low, quadruped, four legs + tail bone structure
 * closely matches the intended silhouette) so the entity is visible and
 * animated (walk/attack) immediately without a custom model class.
 *
 * TODO(art): supply src/client/resources/assets/hollowlight/textures/entity/stalker.png
 * TODO(art): once a bespoke model is ready, replace WolfEntityModel with a
 *            custom StalkerEntityModel bound to MODEL_LAYER.
 */
public class StalkerEntityRenderer extends MobEntityRenderer<StalkerEntity, WolfEntityModel<StalkerEntity>> {

	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(Identifier.of(Hollowlight.MOD_ID, "stalker"), "main");
	private static final Identifier TEXTURE = Identifier.of(Hollowlight.MOD_ID, "textures/entity/stalker.png");

	public StalkerEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new WolfEntityModel<>(context.getPart(EntityModelLayers.WOLF)), 0.4f);
	}

	@Override
	public Identifier getTexture(StalkerEntity entity) {
		return TEXTURE;
	}
}
