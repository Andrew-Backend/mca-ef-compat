package yesman.epicfight.api.collider;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MultiOBBCollider extends MultiCollider<OBBCollider> {
   public MultiOBBCollider(int arrayLength, double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
      super(arrayLength, centerX, centerY, centerZ, null);
      AABB aabb = OBBCollider.getInitialAABB(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
      OBBCollider colliderForAll = new OBBCollider(aabb, vertexX, vertexY, vertexZ, centerX, centerY, centerZ);

      for (int i = 0; i < arrayLength; i++) {
         this.colliders.add(colliderForAll);
      }
   }

   public MultiOBBCollider(OBBCollider... colliders) {
      super(colliders);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void draw(
      PoseStack poseStack,
      MultiBufferSource buffer,
      LivingEntityPatch<?> entitypatch,
      AttackAnimation animation,
      Joint joint,
      float prevElapsedTime,
      float elapsedTime,
      float partialTicks,
      float attackSpeed
   ) {
      int colliderCount = Math.max(
         Math.round((this.numberOfColliders + animation.getProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS).orElse(0)) * attackSpeed),
         this.numberOfColliders
      );
      float partialScale = 1.0F / (colliderCount - 1);
      float interpolation = 0.0F;
      Armature armature = entitypatch.getArmature();
      EntityState state = animation.getState(entitypatch, elapsedTime);
      EntityState prevState = animation.getState(entitypatch, prevElapsedTime);
      boolean attacking = prevState.attacking() || state.attacking() || prevState.getLevel() < 2 && state.getLevel() > 2;
      List<OBBCollider> colliders = Lists.newArrayList();
      float index = 0.0F;
      float interIndex = Math.min((float)(this.numberOfColliders - 1) / (colliderCount - 1), 1.0F);

      for (int i = 0; i < colliderCount; i++) {
         colliders.add(this.colliders.get((int)index).deepCopy());
         index += interIndex;
      }

      for (OBBCollider obbCollider : colliders) {
         float pt1 = prevElapsedTime + (elapsedTime - prevElapsedTime) * partialTicks;
         float pt2 = prevElapsedTime + (elapsedTime - prevElapsedTime) * interpolation;
         TransformSheet coordTransform = animation.getCoord();
         Vec3f p1 = coordTransform.getInterpolatedTranslation(pt1);
         Vec3f p2 = coordTransform.getInterpolatedTranslation(pt2);
         poseStack.m_85836_();
         poseStack.m_252880_(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
         Pose pose;
         if (armature.rootJoint.getName().equals(joint.getName())) {
            pose = new Pose();
            pose.putJointData("Root", JointTransform.empty());
            animation.modifyPose(animation, pose, entitypatch, elapsedTime, 1.0F);
         } else {
            pose = animation.getPoseByTime(entitypatch, pt2, 1.0F);
         }

         obbCollider.drawInternal(poseStack, buffer.m_6299_(this.getRenderType()), armature, joint, pose, pose, 1.0F, attacking ? -65536 : -1);
         poseStack.m_85849_();
         interpolation += partialScale;
      }
   }

   @Override
   public CompoundTag serialize(CompoundTag resultTag) {
      if (resultTag == null) {
         resultTag = new CompoundTag();
      }

      resultTag.m_128405_("number", this.numberOfColliders);
      ListTag center = new ListTag();
      center.add(DoubleTag.m_128500_(this.modelCenter.f_82479_));
      center.add(DoubleTag.m_128500_(this.modelCenter.f_82480_));
      center.add(DoubleTag.m_128500_(this.modelCenter.f_82481_));
      resultTag.m_128365_("center", center);
      ListTag size = new ListTag();
      size.add(DoubleTag.m_128500_(this.colliders.get(0).modelVertices[1].f_82479_));
      size.add(DoubleTag.m_128500_(this.colliders.get(0).modelVertices[1].f_82480_));
      size.add(DoubleTag.m_128500_(this.colliders.get(0).modelVertices[1].f_82481_));
      resultTag.m_128365_("size", size);
      return resultTag;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getRenderType() {
      return this.colliders.get(0).getRenderType();
   }
}
