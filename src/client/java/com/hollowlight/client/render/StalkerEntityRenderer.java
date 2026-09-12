package com.hollowlight.client.render;

import com.hollowlight.Hollowlight;
import com.hollowlight.entity.stalker.StalkerEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.util.Identifier;

/**
 * Renderer stub for The Stalker. QuadrupedEntityModel's constructor is
 * protected, so it cannot be instantiated directly outside its own
 * package/subclasses. PigEntityModel is a public, ready-to-use
 * QuadrupedEntityModel subclass (same four-legged body/head/leg part
 * layout) with a public single-arg constructor and a working
 * getTexturedModelData(Dilation) helper, making it the correct concrete
 * placeholder here rather than the abstract quadruped base.
 *
 * TODO(art): supply src/client/resources/assets/hollowlight/textures/entity/stalker.png
 * TODO(art): once a bespoke model is ready, replace PigEntityModel with a
 *            custom StalkerEntityModel bound to MODEL_LAYER.
 */
public class StalkerEntityRenderer extends MobEntityRenderer<StalkerEntity, PigEntityModel<StalkerEntity>> {

	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(Identifier.of(Hollowlight.MOD_ID, "stalker"), "main");
	private static final Identifier TEXTURE = Identifier.of(Hollowlight.MOD_ID, "textures/entity/stalker.png");

	public StalkerEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new PigEntityModel<>(context.getPart(MODEL_LAYER)), 0.4f);
	}

	@Override
	public Identifier getTexture(StalkerEntity entity) {
		return TEXTURE;
	}

	/**
	 * Supplies the ModelPart tree PigEntityModel expects (head, body, four
	 * legs). Wired to EntityModelLayerRegistry in HollowlightClient.
	 */
	public static TexturedModelData getTexturedModelData() {
		return PigEntityModel.getTexturedModelData(Dilation.NONE);
	}
}
