package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class PVindicatorRenderer extends PIllagerRenderer<AbstractIllager, MobPatch<AbstractIllager>> {
   public PVindicatorRenderer(Context context, EntityType<?> entityType) {
      super(context, entityType);
      this.addPatchedLayerAlways(
         ItemInHandLayer.class,
         new PatchedItemInHandLayer<AbstractIllager, MobPatch<AbstractIllager>, IllagerModel<AbstractIllager>>() {
            public void renderLayer(
               MobPatch<AbstractIllager> entitypatch,
               AbstractIllager entityliving,
               RenderLayer<AbstractIllager, IllagerModel<AbstractIllager>> originalRenderer,
               PoseStack matrixStackIn,
               MultiBufferSource buffer,
               int packedLightIn,
               OpenMatrix4f[] poses,
               float bob,
               float yRot,
               float xRot,
               float partialTicks
            ) {
               if (entityliving.m_5912_()) {
                  super.renderLayer(entitypatch, entityliving, originalRenderer, matrixStackIn, buffer, packedLightIn, poses, bob, yRot, xRot, partialTicks);
               }
            }
         }
      );
   }
}
