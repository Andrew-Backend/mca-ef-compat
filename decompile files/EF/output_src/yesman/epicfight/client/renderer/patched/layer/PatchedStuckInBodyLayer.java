package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class PatchedStuckInBodyLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends PlayerModel<E>, R extends StuckInBodyLayer<E, M>>
   extends PatchedLayer<E, T, M, R> {
   private static final Vec3f VECTOR = new Vec3f();

   protected void renderLayer(
      T entitypatch,
      E entityliving,
      R vanillaLayer,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      OpenMatrix4f[] poses,
      float bob,
      float yRot,
      float xRot,
      float partialTicks
   ) {
      int i = Math.min(ClientConfig.maxStuckProjectiles, this.numStuck(entityliving));
      RandomSource randomsource = RandomSource.m_216335_(entityliving.m_19879_());
      if (i > 0) {
         for (int j = 0; j < i; j++) {
            poseStack.m_85836_();
            int randomJoint = Math.abs(randomsource.m_188502_()) % entitypatch.getArmature().getJointNumber();
            MathUtils.mulStack(poseStack, poses[randomJoint]);
            entitypatch.getArmature().searchJointById(randomJoint).getLocalTransform().toTranslationVector(VECTOR);
            float f = randomsource.m_188501_();
            float f1 = randomsource.m_188501_();
            float f2 = randomsource.m_188501_();
            float f3 = Mth.m_14179_(f, -VECTOR.x * 0.5F, VECTOR.x * 0.5F);
            float f4 = Mth.m_14179_(f1, -VECTOR.y * 0.5F, VECTOR.y * 0.5F);
            float f5 = Mth.m_14179_(f2, -VECTOR.z * 0.5F, VECTOR.z * 0.5F);
            poseStack.m_252880_(f3, f4, f5);
            f = -1.0F * (f * 2.0F - 1.0F);
            f1 = -1.0F * (f1 * 2.0F - 1.0F);
            f2 = -1.0F * (f2 * 2.0F - 1.0F);
            this.renderStuckItem(poseStack, buffer, packedLight, entityliving, f, f1, f2, partialTicks);
            poseStack.m_85849_();
         }
      }
   }

   protected abstract int numStuck(E var1);

   protected abstract void renderStuckItem(PoseStack var1, MultiBufferSource var2, int var3, Entity var4, float var5, float var6, float var7, float var8);
}
