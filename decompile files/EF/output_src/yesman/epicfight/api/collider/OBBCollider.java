package yesman.epicfight.api.collider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;

public class OBBCollider extends Collider {
   protected final Vec3[] modelVertices;
   protected final Vec3[] modelNormals;
   protected Vec3[] rotatedVertices;
   protected Vec3[] rotatedNormals;
   protected Vec3f scale;

   public OBBCollider(double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
      this(getInitialAABB(vertexX, vertexY, vertexZ, centerX, centerY, centerZ), vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
   }

   protected OBBCollider(AABB outerAABB, double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
      super(new Vec3(centerX, centerY, centerZ), outerAABB);
      this.modelVertices = new Vec3[4];
      this.modelNormals = new Vec3[3];
      this.rotatedVertices = new Vec3[4];
      this.rotatedNormals = new Vec3[3];
      this.modelVertices[0] = new Vec3(vertexX, vertexY, -vertexZ);
      this.modelVertices[1] = new Vec3(vertexX, vertexY, vertexZ);
      this.modelVertices[2] = new Vec3(-vertexX, vertexY, vertexZ);
      this.modelVertices[3] = new Vec3(-vertexX, vertexY, -vertexZ);
      this.modelNormals[0] = new Vec3(1.0, 0.0, 0.0);
      this.modelNormals[1] = new Vec3(0.0, 1.0, 0.0);
      this.modelNormals[2] = new Vec3(0.0, 0.0, 1.0);
      this.rotatedVertices[0] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedVertices[1] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedVertices[2] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedVertices[3] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedNormals[0] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedNormals[1] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedNormals[2] = new Vec3(0.0, 0.0, 0.0);
   }

   static AABB getInitialAABB(double posX, double posY, double posZ, double center_x, double center_y, double center_z) {
      double xLength = Math.abs(posX) + Math.abs(center_x);
      double yLength = Math.abs(posY) + Math.abs(center_y);
      double zLength = Math.abs(posZ) + Math.abs(center_z);
      double maxLength = Math.max(xLength, Math.max(yLength, zLength));
      return new AABB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
   }

   public OBBCollider(
      AABB entityCallAABB,
      double pos1_x,
      double pos1_y,
      double pos1_z,
      double pos2_x,
      double pos2_y,
      double pos2_z,
      double norm1_x,
      double norm1_y,
      double norm1_z,
      double norm2_x,
      double norm2_y,
      double norm2_z,
      double center_x,
      double center_y,
      double center_z
   ) {
      super(new Vec3(center_x, center_y, center_z), entityCallAABB);
      this.modelVertices = new Vec3[2];
      this.modelNormals = new Vec3[2];
      this.rotatedVertices = new Vec3[2];
      this.rotatedNormals = new Vec3[2];
      this.modelVertices[0] = new Vec3(pos1_x, pos1_y, pos1_z);
      this.modelVertices[1] = new Vec3(pos2_x, pos2_y, pos2_z);
      this.modelNormals[0] = new Vec3(norm1_x, norm1_y, norm1_z);
      this.modelNormals[1] = new Vec3(norm2_x, norm2_y, norm2_z);
      this.rotatedVertices[0] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedVertices[1] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedNormals[0] = new Vec3(0.0, 0.0, 0.0);
      this.rotatedNormals[1] = new Vec3(0.0, 0.0, 0.0);
   }

   public OBBCollider(AABB aabb) {
      super(null, null);
      this.modelVertices = null;
      this.modelNormals = null;
      double xSize = (aabb.f_82291_ - aabb.f_82288_) / 2.0;
      double ySize = (aabb.f_82292_ - aabb.f_82289_) / 2.0;
      double zSize = (aabb.f_82293_ - aabb.f_82290_) / 2.0;
      this.worldCenter = new Vec3(-((float)aabb.f_82288_ + xSize), (float)aabb.f_82289_ + ySize, -((float)aabb.f_82290_ + zSize));
      this.rotatedVertices = new Vec3[4];
      this.rotatedNormals = new Vec3[3];
      this.rotatedVertices[0] = new Vec3(-xSize, ySize, -zSize);
      this.rotatedVertices[1] = new Vec3(-xSize, ySize, zSize);
      this.rotatedVertices[2] = new Vec3(xSize, ySize, zSize);
      this.rotatedVertices[3] = new Vec3(xSize, ySize, -zSize);
      this.rotatedNormals[0] = new Vec3(1.0, 0.0, 0.0);
      this.rotatedNormals[1] = new Vec3(0.0, 1.0, 0.0);
      this.rotatedNormals[2] = new Vec3(0.0, 0.0, 1.0);
   }

   @Override
   public void transform(OpenMatrix4f modelMatrix) {
      OpenMatrix4f noTranslation = modelMatrix.removeTranslation();

      for (int i = 0; i < this.modelVertices.length; i++) {
         this.rotatedVertices[i] = OpenMatrix4f.transform(noTranslation, this.modelVertices[i]);
      }

      for (int i = 0; i < this.modelNormals.length; i++) {
         this.rotatedNormals[i] = OpenMatrix4f.transform(noTranslation, this.modelNormals[i]);
      }

      this.scale = noTranslation.toScaleVector();
      super.transform(modelMatrix);
   }

   @Override
   protected AABB getHitboxAABB() {
      return this.outerAABB
         .m_82377_(
            (this.outerAABB.f_82291_ - this.outerAABB.f_82288_) * this.scale.x,
            (this.outerAABB.f_82292_ - this.outerAABB.f_82289_) * this.scale.y,
            (this.outerAABB.f_82293_ - this.outerAABB.f_82290_) * this.scale.z
         )
         .m_82386_(-this.worldCenter.f_82479_, this.worldCenter.f_82480_, -this.worldCenter.f_82481_);
   }

   public boolean isCollide(OBBCollider opponent) {
      Vec3 toOpponent = opponent.worldCenter.m_82546_(this.worldCenter);

      for (Vec3 seperateAxis : this.rotatedNormals) {
         if (!checkSeparateAxisOverlap(seperateAxis, toOpponent, this, opponent)) {
            return false;
         }
      }

      for (Vec3 seperateAxis : opponent.rotatedNormals) {
         if (!checkSeparateAxisOverlap(seperateAxis, toOpponent, this, opponent)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean isCollide(Entity entity) {
      OBBCollider obb = new OBBCollider(entity.m_20191_());
      return this.isCollide(obb);
   }

   public OBBCollider deepCopy() {
      Vec3 xyzVec = this.modelVertices[1];
      return new OBBCollider(xyzVec.f_82479_, xyzVec.f_82480_, xyzVec.f_82481_, this.modelCenter.f_82479_, this.modelCenter.f_82480_, this.modelCenter.f_82481_);
   }

   private static boolean checkSeparateAxisOverlap(Vec3 seperateAxis, Vec3 toOpponent, OBBCollider box1, OBBCollider box2) {
      Vec3 maxProj1 = null;
      Vec3 maxProj2 = null;
      double maxDot1 = -1.0;
      double maxDot2 = -1.0;
      if (seperateAxis.m_82526_(toOpponent) < 0.0) {
         seperateAxis.m_82490_(-1.0);
      }

      for (Vec3 vertexVector : box1.rotatedVertices) {
         Vec3 temp = seperateAxis.m_82526_(vertexVector) > 0.0 ? vertexVector : vertexVector.m_82490_(-1.0);
         double dot = seperateAxis.m_82526_(temp);
         if (dot > maxDot1) {
            maxDot1 = dot;
            maxProj1 = temp;
         }
      }

      for (Vec3 vertexVector : box2.rotatedVertices) {
         Vec3 temp = seperateAxis.m_82526_(vertexVector) > 0.0 ? vertexVector : vertexVector.m_82490_(-1.0);
         double dot = seperateAxis.m_82526_(temp);
         if (dot > maxDot2) {
            maxDot2 = dot;
            maxProj2 = temp;
         }
      }

      return MathUtils.projectVector(toOpponent, seperateAxis).m_82553_()
         < MathUtils.projectVector(maxProj1, seperateAxis).m_82553_() + MathUtils.projectVector(maxProj2, seperateAxis).m_82553_();
   }

   @Override
   public String toString() {
      return super.toString() + " worldCenter : " + this.worldCenter + " world direction : " + this.rotatedVertices[0];
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getRenderType() {
      return RenderType.m_110504_();
   }

   @OnlyIn(Dist.CLIENT)
   public void draw(PoseStack poseStack, MultiBufferSource buffer, int color) {
      VertexConsumer vertexConsumer = buffer.m_6299_(this.getRenderType());
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      float v1x = (float)(this.worldCenter.f_82479_ + this.rotatedVertices[0].f_82479_);
      float v1y = (float)(this.worldCenter.f_82480_ + this.rotatedVertices[0].f_82480_);
      float v1z = (float)(this.worldCenter.f_82481_ + this.rotatedVertices[0].f_82481_);
      float v2x = (float)(this.worldCenter.f_82479_ + this.rotatedVertices[1].f_82479_);
      float v2y = (float)(this.worldCenter.f_82480_ + this.rotatedVertices[1].f_82480_);
      float v2z = (float)(this.worldCenter.f_82481_ + this.rotatedVertices[1].f_82481_);
      float v3x = (float)(this.worldCenter.f_82479_ + this.rotatedVertices[2].f_82479_);
      float v3y = (float)(this.worldCenter.f_82480_ + this.rotatedVertices[2].f_82480_);
      float v3z = (float)(this.worldCenter.f_82481_ + this.rotatedVertices[2].f_82481_);
      float v4x = (float)(this.worldCenter.f_82479_ + this.rotatedVertices[3].f_82479_);
      float v4y = (float)(this.worldCenter.f_82480_ + this.rotatedVertices[3].f_82480_);
      float v4z = (float)(this.worldCenter.f_82481_ + this.rotatedVertices[3].f_82481_);
      float v5x = (float)(this.worldCenter.f_82479_ + -this.rotatedVertices[2].f_82479_);
      float v5y = (float)(this.worldCenter.f_82480_ + -this.rotatedVertices[2].f_82480_);
      float v5z = (float)(this.worldCenter.f_82481_ + -this.rotatedVertices[2].f_82481_);
      float v6x = (float)(this.worldCenter.f_82479_ + -this.rotatedVertices[3].f_82479_);
      float v6y = (float)(this.worldCenter.f_82480_ + -this.rotatedVertices[3].f_82480_);
      float v6z = (float)(this.worldCenter.f_82481_ + -this.rotatedVertices[3].f_82481_);
      float v7x = (float)(this.worldCenter.f_82479_ + -this.rotatedVertices[0].f_82479_);
      float v7y = (float)(this.worldCenter.f_82480_ + -this.rotatedVertices[0].f_82480_);
      float v7z = (float)(this.worldCenter.f_82481_ + -this.rotatedVertices[0].f_82481_);
      float v8x = (float)(this.worldCenter.f_82479_ + -this.rotatedVertices[1].f_82479_);
      float v8y = (float)(this.worldCenter.f_82480_ + -this.rotatedVertices[1].f_82480_);
      float v8z = (float)(this.worldCenter.f_82481_ + -this.rotatedVertices[1].f_82481_);
      vertexConsumer.m_252986_(matrix, v1x, v1y, v1z).m_193479_(color).m_5601_(v2x - v1x, v2y - v1y, v2z - v1z).m_5752_();
      vertexConsumer.m_252986_(matrix, v2x, v2y, v2z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v2x, v2y, v2z).m_193479_(color).m_5601_(v3x - v2x, v3y - v2y, v3z - v2z).m_5752_();
      vertexConsumer.m_252986_(matrix, v3x, v3y, v3z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v3x, v3y, v3z).m_193479_(color).m_5601_(v4x - v3x, v4y - v3y, v4z - v3z).m_5752_();
      vertexConsumer.m_252986_(matrix, v4x, v4y, v4z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v4x, v4y, v4z).m_193479_(color).m_5601_(v1x - v4x, v1y - v4y, v1z - v4z).m_5752_();
      vertexConsumer.m_252986_(matrix, v1x, v1y, v1z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v1x, v1y, v1z).m_193479_(color).m_5601_(v5x - v1x, v5y - v1y, v5z - v1z).m_5752_();
      vertexConsumer.m_252986_(matrix, v5x, v5y, v5z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v2x, v2y, v2z).m_193479_(color).m_5601_(v6x - v2x, v6y - v2y, v6z - v2z).m_5752_();
      vertexConsumer.m_252986_(matrix, v6x, v6y, v6z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v3x, v3y, v3z).m_193479_(color).m_5601_(v7x - v3x, v7y - v3y, v7z - v3z).m_5752_();
      vertexConsumer.m_252986_(matrix, v7x, v7y, v7z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v4x, v4y, v4z).m_193479_(color).m_5601_(v8x - v4x, v8y - v4y, v8z - v4z).m_5752_();
      vertexConsumer.m_252986_(matrix, v8x, v8y, v8z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v5x, v5y, v5z).m_193479_(color).m_5601_(v6x - v5x, v6y - v5y, v6z - v5z).m_5752_();
      vertexConsumer.m_252986_(matrix, v6x, v6y, v6z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v6x, v6y, v6z).m_193479_(color).m_5601_(v7x - v6x, v7y - v6y, v7z - v6z).m_5752_();
      vertexConsumer.m_252986_(matrix, v7x, v7y, v7z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v7x, v7y, v7z).m_193479_(color).m_5601_(v8x - v7x, v8y - v7y, v8z - v7z).m_5752_();
      vertexConsumer.m_252986_(matrix, v8x, v8y, v8z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, v8x, v8y, v8z).m_193479_(color).m_5601_(v5x - v8x, v5y - v8y, v5z - v8z).m_5752_();
      vertexConsumer.m_252986_(matrix, v5x, v5y, v5z).m_193479_(color).m_5601_(0.0F, 0.0F, 0.0F).m_5752_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawInternal(
      PoseStack poseStack, VertexConsumer vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color
   ) {
      Pose interpolatedPose = Pose.interpolatePose(pose1, pose2, partialTicks);
      OpenMatrix4f poseMatrix;
      if (armature.rootJoint.equals(joint)) {
         JointTransform jt = interpolatedPose.orElseEmpty("Root");
         jt.rotation().x = 0.0F;
         jt.rotation().y = 0.0F;
         jt.rotation().z = 0.0F;
         jt.rotation().w = 1.0F;
         poseMatrix = jt.getAnimationBoundMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
      } else {
         poseMatrix = armature.getBoundTransformFor(interpolatedPose, joint);
      }

      poseStack.m_85836_();
      MathUtils.mulStack(poseStack, poseMatrix);
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      Vec3 vec = this.modelVertices[1];
      float maxX = (float)(this.modelCenter.f_82479_ + vec.f_82479_);
      float maxY = (float)(this.modelCenter.f_82480_ + vec.f_82480_);
      float maxZ = (float)(this.modelCenter.f_82481_ + vec.f_82481_);
      float minX = (float)(this.modelCenter.f_82479_ - vec.f_82479_);
      float minY = (float)(this.modelCenter.f_82480_ - vec.f_82480_);
      float minZ = (float)(this.modelCenter.f_82481_ - vec.f_82481_);
      vertexConsumer.m_252986_(matrix, minX, maxY, minZ).m_193479_(color).m_5601_(0.0F, 0.0F, 1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, maxZ).m_193479_(color).m_5601_(0.0F, 0.0F, 1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, maxZ).m_193479_(color).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, maxZ).m_193479_(color).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, maxZ).m_193479_(color).m_5601_(0.0F, 0.0F, -1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, minZ).m_193479_(color).m_5601_(0.0F, 0.0F, -1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, minZ).m_193479_(color).m_5601_(-1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, minZ).m_193479_(color).m_5601_(-1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, maxZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, maxZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, maxZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, minY, maxZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, minZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, minZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, minZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, minY, minZ).m_193479_(color).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, minY, minZ).m_193479_(color).m_5601_(0.0F, 0.0F, 1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, minY, maxZ).m_193479_(color).m_5601_(0.0F, 0.0F, 1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, minY, maxZ).m_193479_(color).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, maxZ).m_193479_(color).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, maxZ).m_193479_(color).m_5601_(0.0F, 0.0F, -1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, minZ).m_193479_(color).m_5601_(0.0F, 0.0F, -1.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, minZ).m_193479_(color).m_5601_(-1.0F, 0.0F, 0.0F).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, minY, minZ).m_193479_(color).m_5601_(-1.0F, 0.0F, 0.0F).m_5752_();
      poseStack.m_85849_();
   }

   @Override
   public CompoundTag serialize(CompoundTag resultTag) {
      if (resultTag == null) {
         resultTag = new CompoundTag();
      }

      resultTag.m_128405_("number", 1);
      ListTag center = new ListTag();
      center.add(DoubleTag.m_128500_(this.modelCenter.f_82479_));
      center.add(DoubleTag.m_128500_(this.modelCenter.f_82480_));
      center.add(DoubleTag.m_128500_(this.modelCenter.f_82481_));
      resultTag.m_128365_("center", center);
      ListTag size = new ListTag();
      size.add(DoubleTag.m_128500_(this.modelVertices[1].f_82479_));
      size.add(DoubleTag.m_128500_(this.modelVertices[1].f_82480_));
      size.add(DoubleTag.m_128500_(this.modelVertices[1].f_82481_));
      resultTag.m_128365_("size", size);
      return resultTag;
   }
}
