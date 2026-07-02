package yesman.epicfight.api.collider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

public class LineCollider extends Collider {
   protected Vec3 modelVec;
   protected Vec3 worldVec;

   public LineCollider(double posX, double posY, double posZ, double vecX, double vecY, double vecZ) {
      this(getInitialAABB(posX, posY, posZ, vecX, vecY, vecZ), posX, posY, posZ, vecX, vecY, vecZ);
   }

   protected LineCollider(AABB outerAABB, double posX, double posY, double posZ, double vecX, double vecY, double vecZ) {
      super(new Vec3(posX, posY, posZ), outerAABB);
      this.modelVec = new Vec3(vecX, vecY, vecZ);
      this.worldVec = new Vec3(0.0, 0.0, 0.0);
   }

   static AABB getInitialAABB(double posX, double posY, double posZ, double vecX, double vecY, double vecZ) {
      Vec3 start = new Vec3(posX, posY, posZ);
      Vec3 end = new Vec3(vecX + posX, vecY + posY, vecZ + posZ);
      double length = Math.max(start.m_82553_(), end.m_82553_());
      return new AABB(length, length, length, -length, -length, -length);
   }

   @Override
   public void transform(OpenMatrix4f mat) {
      this.worldVec = OpenMatrix4f.transform(mat.removeTranslation(), this.modelVec);
      super.transform(mat);
   }

   @Override
   public boolean isCollide(Entity entity) {
      AABB opponent = entity.m_20191_();
      if (this.worldVec.f_82479_ != 0.0 || !(this.worldCenter.f_82479_ < opponent.f_82288_) && !(this.worldCenter.f_82479_ > opponent.f_82291_)) {
         double startX = Mth.m_14008_((opponent.f_82288_ + this.worldCenter.f_82479_) / -this.worldVec.f_82479_, 0.0, 1.0);
         double endX = Mth.m_14008_((opponent.f_82291_ + this.worldCenter.f_82479_) / -this.worldVec.f_82479_, 0.0, 1.0);
         if (startX > endX) {
            double temp = startX;
            startX = endX;
            endX = temp;
         }

         double maxStart = startX;
         double minEnd = endX;
         if (minEnd == maxStart) {
            return false;
         }

         if (this.worldVec.f_82480_ != 0.0 || !(this.worldCenter.f_82480_ < opponent.f_82289_) && !(this.worldCenter.f_82480_ > opponent.f_82292_)) {
            double startY = Mth.m_14008_((float)(opponent.f_82289_ - this.worldCenter.f_82480_) / this.worldVec.f_82480_, 0.0, 1.0);
            double endY = Mth.m_14008_((float)(opponent.f_82292_ - this.worldCenter.f_82480_) / this.worldVec.f_82480_, 0.0, 1.0);
            if (startY > endY) {
               double temp = startY;
               startY = endY;
               endY = temp;
            }

            maxStart = maxStart < startY ? startY : maxStart;
            minEnd = minEnd > endY ? endY : minEnd;
            if (maxStart >= minEnd) {
               return false;
            }

            if (this.worldVec.f_82481_ != 0.0 || !(this.worldCenter.f_82481_ < opponent.f_82290_) && !(this.worldCenter.f_82481_ > opponent.f_82293_)) {
               double startZ = Mth.m_14008_((float)(opponent.f_82290_ + this.worldCenter.f_82481_) / -this.worldVec.f_82481_, 0.0, 1.0);
               double endZ = Mth.m_14008_((float)(opponent.f_82293_ + this.worldCenter.f_82481_) / -this.worldVec.f_82481_, 0.0, 1.0);
               if (startZ > endZ) {
                  double temp = startZ;
                  startZ = endZ;
                  endZ = temp;
               }

               maxStart = maxStart < startZ ? startZ : maxStart;
               minEnd = minEnd > endZ ? endZ : minEnd;
               return !(maxStart >= minEnd);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public LineCollider deepCopy() {
      return new LineCollider(
         this.modelCenter.f_82479_,
         this.modelCenter.f_82480_,
         this.modelCenter.f_82481_,
         this.modelVec.f_82479_,
         this.modelVec.f_82480_,
         this.modelVec.f_82481_
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getRenderType() {
      return EpicFightRenderTypes.debugCollider();
   }

   @Override
   public void drawInternal(
      PoseStack poseStack, VertexConsumer vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color
   ) {
      Pose interpolatedPose = Pose.interpolatePose(pose1, pose2, partialTicks);
      OpenMatrix4f poseMatrix;
      if (armature.rootJoint.equals(joint)) {
         poseMatrix = interpolatedPose.orElseEmpty("Root").getAnimationBoundMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
      } else {
         poseMatrix = armature.getBoundTransformFor(interpolatedPose, joint);
      }

      MathUtils.mulStack(poseStack, poseMatrix);
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      float startX = (float)this.modelCenter.f_82479_;
      float startY = (float)this.modelCenter.f_82480_;
      float startZ = (float)this.modelCenter.f_82481_;
      float endX = (float)(this.modelCenter.f_82479_ + this.modelVec.f_82479_);
      float endY = (float)(this.modelCenter.f_82480_ + this.modelVec.f_82480_);
      float endZ = (float)(this.modelCenter.f_82481_ + this.modelVec.f_82481_);
      vertexConsumer.m_252986_(matrix, startX, startY, startZ).m_193479_(color).m_5752_();
      vertexConsumer.m_252986_(matrix, endX, endY, endZ).m_193479_(color).m_5752_();
   }
}
