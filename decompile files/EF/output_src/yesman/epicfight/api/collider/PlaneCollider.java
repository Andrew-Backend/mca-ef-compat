package yesman.epicfight.api.collider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Deprecated
public class PlaneCollider extends Collider {
   private final Vec3[] modelPos = new Vec3[2];
   private final Vec3[] worldPos = new Vec3[2];

   static AABB getInitialAABB(double center_x, double center_y, double center_z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
      double xLength = Math.max(Math.abs(aX), Math.abs(bX)) + Math.abs(center_x);
      double yLength = Math.max(Math.abs(aY), Math.abs(bY)) + Math.abs(center_y);
      double zLength = Math.max(Math.abs(aZ), Math.abs(bZ)) + Math.abs(center_z);
      double maxLength = Math.max(xLength, Math.max(yLength, zLength));
      return new AABB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
   }

   public PlaneCollider(double x, double y, double z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
      this(getInitialAABB(x, y, z, aX, aY, aZ, bX, bY, bZ), x, y, z, aX, aY, aZ, bX, bY, bZ);
   }

   public PlaneCollider(
      AABB entityCallAABB, double centerX, double centerY, double centerZ, double pos1X, double pos1Y, double pos1Z, double pos2X, double pos2Y, double pos2Z
   ) {
      super(new Vec3(centerX, centerY, centerZ), entityCallAABB);
      this.modelPos[0] = new Vec3(pos1X, pos1Y, pos1Z);
      this.modelPos[1] = new Vec3(pos2X, pos2Y, pos2Z);
      this.worldPos[0] = new Vec3(0.0, 0.0, 0.0);
      this.worldPos[1] = new Vec3(0.0, 0.0, 0.0);
   }

   @Override
   public boolean isCollide(Entity entity) {
      AABB opponent = entity.m_20191_();
      Vec3 planeNorm = this.worldPos[0].m_82537_(this.worldPos[1]);
      Vec3 pos = new Vec3(
         planeNorm.f_82479_ >= 0.0 ? opponent.f_82291_ : opponent.f_82288_,
         planeNorm.f_82480_ >= 0.0 ? opponent.f_82292_ : opponent.f_82289_,
         planeNorm.f_82481_ >= 0.0 ? opponent.f_82293_ : opponent.f_82290_
      );
      Vec3 neg = new Vec3(
         planeNorm.f_82479_ >= 0.0 ? opponent.f_82288_ : opponent.f_82291_,
         planeNorm.f_82480_ >= 0.0 ? opponent.f_82289_ : opponent.f_82292_,
         planeNorm.f_82481_ >= 0.0 ? opponent.f_82290_ : opponent.f_82293_
      );
      double planeD = planeNorm.m_82526_(this.worldCenter);
      double dot1 = planeNorm.m_82526_(pos) - planeD;
      if (dot1 < 0.0) {
         return false;
      }

      double dot2 = planeNorm.m_82526_(neg) - planeD;
      return !(dot2 > 0.0);
   }

   @Override
   public void transform(OpenMatrix4f mat) {
      for (int i = 0; i < 2; i++) {
         this.worldPos[i] = OpenMatrix4f.transform(mat.removeTranslation(), this.modelPos[i]);
      }

      super.transform(mat);
   }

   public PlaneCollider deepCopy() {
      Vec3 aVec = this.modelPos[0];
      Vec3 bVec = this.modelPos[1];
      return new PlaneCollider(
         this.modelCenter.f_82479_,
         this.modelCenter.f_82480_,
         this.modelCenter.f_82481_,
         aVec.f_82479_,
         aVec.f_82480_,
         aVec.f_82481_,
         bVec.f_82479_,
         bVec.f_82480_,
         bVec.f_82481_
      );
   }

   @Override
   public void drawInternal(
      PoseStack poseStack, VertexConsumer vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color
   ) {
   }

   @Override
   public void draw(
      PoseStack matrixStackIn,
      MultiBufferSource buffer,
      LivingEntityPatch<?> entitypatch,
      AttackAnimation animation,
      Joint joint,
      float prevElapsedTime,
      float elapsedTime,
      float partialTicks,
      float attackSpeed
   ) {
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getRenderType() {
      return null;
   }
}
