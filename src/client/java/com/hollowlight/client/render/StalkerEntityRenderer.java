package com.hollowlight.client.render;

import com.hollowlight.Hollowlight;
import com.hollowlight.entity.stalker.StalkerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.util.Identifier;

/**
 * Renderer stub for The Stalker. WolfEntityModel is generically restricted
 * to WolfEntity subclasses (T extends WolfEntity), so it cannot be reused
 * directly for a HostileEntity like StalkerEntity. QuadrupedEntityModel is
 * the correct generic placeholder here: it is parameterized over the
 * entity type itself in 1.21.1 (the render-state-based model split happens
 * in later versions) and models exactly the four-legged silhouette this
 * entity needs — the same base class vanilla uses for cows, pigs, etc.
 *
 * TODO(art): supply src/client/resources/assets/hollowlight/textures/entity/stalker.png
 * TODO(art): once a bespoke model is ready, replace QuadrupedEntityModel
 *            with a custom StalkerEntityModel bound to MODEL_LAYER.
 */
public class StalkerEntityRenderer extends MobEntityRenderer<StalkerEntity, QuadrupedEntityModel<StalkerEntity>> {

	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(Identifier.of(Hollowlight.MOD_ID, "stalker"), "main");
	private static final Identifier TEXTURE = Identifier.of(Hollowlight.MOD_ID, "textures/entity/stalker.png");

	public StalkerEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new QuadrupedEntityModel<>(context.getPart(MODEL_LAYER)), 0.4f);
	}

	@Override
	public Identifier getTexture(StalkerEntity entity) {
		return TEXTURE;
	}

	/**
	 * Provides the ModelPart tree QuadrupedEntityModel expects (head, body,
	 * four legs). Registered as a model layer definition; see
	 * HollowlightClient for where this would be wired to
	 * EntityModelLayerRegistry once that registration is added.
	 */
	public static net.minecraft.client.model.TexturedModelData getTexturedModelData() {
		net.minecraft.client.model.ModelData modelData = QuadrupedEntityModel.getModelData(net.minecraft.client.model.Dilation.NONE, 4);
		return net.minecraft.client.model.TexturedModelData.of(modelData, 64, 32);
	}
}
