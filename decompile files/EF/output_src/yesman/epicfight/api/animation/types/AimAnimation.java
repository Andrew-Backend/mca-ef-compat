package yesman.epicfight.api.animation.types;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AimAnimation extends StaticAnimation {
   public DirectStaticAnimation lookForward;
   public DirectStaticAnimation lookUp;
   public DirectStaticAnimation lookDown;
   public DirectStaticAnimation lying;

   public AimAnimation(
      boolean repeatPlay,
      AnimationManager.AnimationAccessor<? extends AimAnimation> accessor,
      String path1,
      String path2,
      String path3,
      String path4,
      AssetAccessor<? extends Armature> armature
   ) {
      this(0.15F, repeatPlay, accessor, path1, path2, path3, path4, armature);
   }

   public AimAnimation(
      float transitionTime,
      boolean repeatPlay,
      AnimationManager.AnimationAccessor<? extends AimAnimation> accessor,
      String path1,
      String path2,
      String path3,
      String path4,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, repeatPlay, accessor, armature);
      this.lookForward = new DirectStaticAnimation(
         transitionTime, repeatPlay, ResourceLocation.fromNamespaceAndPath(accessor.registryName().m_135827_(), path1), armature
      );
      this.lookUp = new DirectStaticAnimation(
         transitionTime, repeatPlay, ResourceLocation.fromNamespaceAndPath(accessor.registryName().m_135827_(), path2), armature
      );
      this.lookDown = new DirectStaticAnimation(
         transitionTime, repeatPlay, ResourceLocation.fromNamespaceAndPath(accessor.registryName().m_135827_(), path3), armature
      );
      this.lying = new DirectStaticAnimation(
         transitionTime, repeatPlay, ResourceLocation.fromNamespaceAndPath(accessor.registryName().m_135827_(), path4), armature
      );
      this.addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (animation, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
         if (animation.isLinkAnimation()) {
            return 1.0F;
         } else {
            return entitypatch.getOriginal().m_6117_() ? (this.getTotalTime() - elapsedTime) / this.getTotalTime() : 1.0F;
         }
      });
      this.addProperty(
         AnimationProperty.StaticAnimationProperty.POSE_MODIFIER,
         (animation, pose, entitypatch, elapsedTime, partialTicks) -> {
            if (!entitypatch.isFirstPerson() && !animation.isLinkAnimation()) {
               JointTransform chest = pose.orElseEmpty("Chest");
               JointTransform head = pose.orElseEmpty("Head");
               float f = 90.0F;
               float ratio = (f - Math.abs(entitypatch.getOriginal().m_146909_())) / f;
               float yRotHead = Mth.m_14179_(partialTicks, entitypatch.getOriginal().f_20886_, entitypatch.getOriginal().f_20885_);
               float yRot = entitypatch.getOriginal().m_20202_() != null
                  ? yRotHead
                  : Mth.m_14179_(partialTicks, entitypatch.getOriginal().f_20884_, entitypatch.getOriginal().f_20883_);
               MathUtils.mulQuaternion(QuaternionUtils.YP.rotationDegrees(Mth.m_14177_(yRot - yRotHead) * ratio), head.rotation(), head.rotation());
               chest.frontResult(
                  JointTransform.rotation(QuaternionUtils.YP.rotationDegrees(Mth.m_14177_(yRotHead - yRot) * ratio)), OpenMatrix4f::mulAsOriginInverse
               );
            }
         }
      );
   }

   @Override
   public void loadAnimation() {
      this.lookForward.loadAnimation();
      this.lookUp.loadAnimation();
      this.lookDown.loadAnimation();
      this.lying.loadAnimation();
   }

   @Override
   public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      if (!entitypatch.isFirstPerson()) {
         LivingMotion livingMotion = entitypatch.getCurrentLivingMotion();
         if (livingMotion != LivingMotions.SWIM && livingMotion != LivingMotions.FLY && livingMotion != LivingMotions.CREATIVE_FLY) {
            float pitch = entitypatch.getOriginal().m_5686_(Minecraft.m_91087_().m_91296_());
            StaticAnimation interpolateAnimation = pitch > 0.0F ? this.lookDown : this.lookUp;
            Pose pose1 = super.getPoseByTime(entitypatch, time, partialTicks);
            Pose pose2 = interpolateAnimation.getPoseByTime(entitypatch, time, partialTicks);
            return Pose.interpolatePose(pose1, pose2, Math.abs(pitch) / 90.0F);
         } else {
            Pose pose = this.lying.getPoseByTime(entitypatch, time, partialTicks);
            this.modifyPose(this, pose, entitypatch, time, partialTicks);
            return pose;
         }
      } else {
         return this.lookForward.getPoseByTime(entitypatch, time, partialTicks);
      }
   }

   @Override
   public List<AssetAccessor<? extends StaticAnimation>> getSubAnimations() {
      return List.of(this.lookForward, this.lookUp, this.lookDown, this.lying);
   }

   @Override
   public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
      return this.lookForward.getProperty(propertyType);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Layer.Priority getPriority() {
      return this.lookForward.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.LOWEST);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Layer.LayerType getLayerType() {
      return this.lookForward.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER);
   }

   @Override
   public AnimationClip getAnimationClip() {
      return this.lookForward.getAnimationClip();
   }

   @Override
   public boolean isClientAnimation() {
      return true;
   }
}
