package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedArrowLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends PlayerModel<E>>
   extends PatchedStuckInBodyLayer<E, T, M, ArrowLayer<E, M>> {
   private final EntityRenderDispatcher dispatcher;

   public PatchedArrowLayer(Context context) {
      this.dispatcher = context.m_174022_();
   }

   @Override
   protected void renderStuckItem(
      PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float f1, float f2, float f3, float partialTick
   ) {
      float f = Mth.m_14116_(f1 * f1 + f3 * f3);
      Arrow arrow = new Arrow(entity.m_9236_(), entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
      arrow.m_146922_((float)(Math.atan2(f1, f3) * 180.0F / (float)Math.PI));
      arrow.m_146926_((float)(Math.atan2(f2, f) * 180.0F / (float)Math.PI));
      arrow.f_19859_ = arrow.m_146908_();
      arrow.f_19860_ = arrow.m_146909_();
      this.dispatcher.m_114384_(arrow, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);
   }

   @Override
   protected int numStuck(E entity) {
      return entity.m_21234_();
   }
}
