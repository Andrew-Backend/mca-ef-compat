package yesman.epicfight.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetIndicator extends EntityUI {
   @Override
   public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks) {
      if (!ClientConfig.showTargetIndicator) {
         return false;
      }

      if (playerpatch != null && entity != playerpatch.getTarget()) {
         return false;
      }

      if (!entity.m_20177_((Player)playerpatch.getOriginal()) && entity.m_6084_() && entity != playerpatch.getOriginal()) {
         if (entity.m_20280_(Minecraft.m_91087_().m_91288_()) >= 400.0) {
            return false;
         } else {
            return entity instanceof Player player ? !player.m_5833_() : true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void draw(
      LivingEntity entity,
      @Nullable LivingEntityPatch<?> entitypatch,
      LocalPlayerPatch playerpatch,
      PoseStack poseStack,
      MultiBufferSource buffers,
      float partialTicks
   ) {
      Matrix4f modelViewMatrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, entity.m_20206_() + 0.45F, 0.0F, true, partialTicks);
      if (entitypatch == null) {
         drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
      } else if (entity.f_19797_ % 2 == 0 && !entitypatch.flashTargetIndicator(playerpatch)) {
         drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 132, 0, 167, 36, 256);
      } else {
         drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
      }
   }
}
