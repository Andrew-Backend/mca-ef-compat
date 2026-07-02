package yesman.epicfight.api.collider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class Collider {
   protected final Vec3 modelCenter;
   protected final AABB outerAABB;
   protected Vec3 worldCenter;

   public Collider(Vec3 center, @Nullable AABB outerAABB) {
      this.modelCenter = center;
      this.outerAABB = outerAABB;
      this.worldCenter = new Vec3(0.0, 0.0, 0.0);
   }

   protected void transform(OpenMatrix4f mat) {
      this.worldCenter = OpenMatrix4f.transform(mat, this.modelCenter);
   }

   public List<Entity> updateAndSelectCollideEntity(
      LivingEntityPatch<?> entitypatch, AttackAnimation attackAnimation, float prevElapsedTime, float elapsedTime, Joint joint, float attackSpeed
   ) {
      Armature armature = entitypatch.getArmature();
      OpenMatrix4f transformMatrix;
      if (armature.rootJoint.equals(joint)) {
         Pose rootPose = new Pose();
         rootPose.putJointData("Root", JointTransform.empty());
         attackAnimation.modifyPose(attackAnimation, rootPose, entitypatch, elapsedTime, 1.0F);
         transformMatrix = rootPose.orElseEmpty("Root").getAnimationBoundMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
      } else {
         transformMatrix = armature.getBoundTransformFor(attackAnimation.getPoseByTime(entitypatch, elapsedTime, 1.0F), joint);
      }

      OpenMatrix4f toWorldCoord = OpenMatrix4f.createTranslation(
         -((float)entitypatch.getOriginal().m_20185_()), (float)entitypatch.getOriginal().m_20186_(), -((float)entitypatch.getOriginal().m_20189_())
      );
      transformMatrix.mulFront(toWorldCoord.mulBack(entitypatch.getModelMatrix(1.0F)));
      this.transform(transformMatrix);
      return this.getCollideEntities(entitypatch.getOriginal());
   }

   public List<Entity> getCollideEntities(Entity entity) {
      return entity.m_9236_().m_6249_(entity, this.getHitboxAABB(), e -> {
         if (e instanceof PartEntity<?> partEntity && partEntity.getParent().m_7306_(entity)) {
            return false;
         } else {
            return e.m_5833_() ? false : this.isCollide(e);
         }
      });
   }

   @OnlyIn(Dist.CLIENT)
   public abstract void drawInternal(PoseStack var1, VertexConsumer var2, Armature var3, Joint var4, Pose var5, Pose var6, float var7, int var8);

   @OnlyIn(Dist.CLIENT)
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
      Armature armature = entitypatch.getArmature();
      EntityState state = animation.getState(entitypatch, elapsedTime);
      EntityState prevState = animation.getState(entitypatch, prevElapsedTime);
      boolean attacking = prevState.attacking() || state.attacking() || prevState.getLevel() < 2 && state.getLevel() > 2;
      Pose prevPose;
      Pose currentPose;
      if (joint.getName().equals(armature.rootJoint.getName())) {
         prevPose = new Pose();
         currentPose = new Pose();
         prevPose.putJointData("Root", JointTransform.empty());
         currentPose.putJointData("Root", JointTransform.empty());
         animation.modifyPose(animation, prevPose, entitypatch, prevElapsedTime, 0.0F);
         animation.modifyPose(animation, currentPose, entitypatch, elapsedTime, 1.0F);
      } else {
         prevPose = animation.getPoseByTime(entitypatch, prevElapsedTime, 0.0F);
         currentPose = animation.getPoseByTime(entitypatch, elapsedTime, 1.0F);
      }

      this.drawInternal(poseStack, buffer.m_6299_(this.getRenderType()), armature, joint, prevPose, currentPose, partialTicks, attacking ? -65536 : -1);
   }

   public abstract Collider deepCopy();

   public abstract boolean isCollide(Entity var1);

   @OnlyIn(Dist.CLIENT)
   public abstract RenderType getRenderType();

   protected AABB getHitboxAABB() {
      return this.outerAABB.m_82386_(-this.worldCenter.f_82479_, this.worldCenter.f_82480_, -this.worldCenter.f_82481_);
   }

   public CompoundTag serialize(CompoundTag resultTag) {
      return resultTag;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + " center: " + this.modelCenter;
   }
}
