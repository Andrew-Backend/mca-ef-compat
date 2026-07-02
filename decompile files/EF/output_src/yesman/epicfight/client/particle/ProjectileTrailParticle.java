package yesman.epicfight.client.particle;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;

public class ProjectileTrailParticle extends AbstractTrailParticle<ProjectilePatch<AbstractArrow>> {
   protected float lastXRot;
   protected float lastYRot;

   protected ProjectileTrailParticle(ClientLevel level, ProjectilePatch<AbstractArrow> entitypatch, TrailInfo trailInfo) {
      super(level, entitypatch, trailInfo);
      this.f_107227_ = trailInfo.rCol();
      this.f_107228_ = trailInfo.gCol();
      this.f_107229_ = trailInfo.bCol();
   }

   @Override
   protected boolean canContinue() {
      if (this.owner.getOriginal() instanceof ThrownTrident thrownTrident && thrownTrident.f_37557_ > 0) {
         return false;
      } else {
         return this.owner.hit() ? false : ((AbstractArrow)this.owner.getOriginal()).m_6084_() && !((AbstractArrow)this.owner.getOriginal()).f_36703_;
      }
   }

   @Override
   protected void createNextCurve() {
      if (!this.shouldRemove) {
         if (this.owner.getOriginal() instanceof Arrow arrow) {
            int color = arrow.m_36889_();
            float r = ((color & 0xFF0000) >> 16) / 255.0F;
            float g = ((color & 0xFF00) >> 8) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            this.f_107227_ = r;
            this.f_107228_ = g;
            this.f_107229_ = b;
         }

         boolean isFirstTrail = this.trailEdges.isEmpty();
         if (isFirstTrail) {
            this.lastXRot = ((AbstractArrow)this.owner.getOriginal()).m_146909_();
            this.lastYRot = 180.0F + ((AbstractArrow)this.owner.getOriginal()).m_146908_();
         }

         TrailInfo trailInfo = this.trailInfo;
         Vec3 posOld = ((AbstractArrow)this.owner.getOriginal()).m_20318_(0.0F);
         Vec3 posCur = ((AbstractArrow)this.owner.getOriginal()).m_20318_(1.0F);
         Vec3 posMid = MathUtils.lerpVector(posOld, posCur, 0.5F);
         float xRotO = this.lastXRot;
         float xRot = ((AbstractArrow)this.owner.getOriginal()).m_146909_();
         float xRotMod = Mth.m_14189_(0.5F, xRotO, xRot);
         float yRotO = this.lastYRot;
         float yRot = 180.0F + ((AbstractArrow)this.owner.getOriginal()).m_146908_();
         float yRotMod = Mth.m_14189_(0.5F, yRotO, yRot);
         OpenMatrix4f prevTransform = OpenMatrix4f.createTranslation((float)posOld.f_82479_, (float)posOld.f_82480_, (float)posOld.f_82481_)
            .rotateDeg(yRotO, Vec3f.Y_AXIS)
            .rotateDeg(xRotO, Vec3f.X_AXIS);
         OpenMatrix4f modTransform = OpenMatrix4f.createTranslation((float)posMid.f_82479_, (float)posMid.f_82480_, (float)posMid.f_82481_)
            .rotateDeg(yRotMod, Vec3f.Y_AXIS)
            .rotateDeg(xRotMod, Vec3f.X_AXIS);
         OpenMatrix4f curTransform = OpenMatrix4f.createTranslation((float)posCur.f_82479_, (float)posCur.f_82480_, (float)posCur.f_82481_)
            .rotateDeg(yRot, Vec3f.Y_AXIS)
            .rotateDeg(xRot, Vec3f.X_AXIS);
         Vec3 prevStartPos = OpenMatrix4f.transform(prevTransform, trailInfo.start());
         Vec3 prevEndPos = OpenMatrix4f.transform(prevTransform, trailInfo.end());
         Vec3 middleStartPos = OpenMatrix4f.transform(modTransform, trailInfo.start());
         Vec3 middleEndPos = OpenMatrix4f.transform(modTransform, trailInfo.end());
         Vec3 currentStartPos = OpenMatrix4f.transform(curTransform, trailInfo.start());
         Vec3 currentEndPos = OpenMatrix4f.transform(curTransform, trailInfo.end());
         List<Vec3> startPosList = Lists.newArrayList();
         List<Vec3> endPosList = Lists.newArrayList();
         AbstractTrailParticle.TrailEdge edge1;
         AbstractTrailParticle.TrailEdge edge2;
         if (isFirstTrail) {
            edge1 = new AbstractTrailParticle.TrailEdge(prevStartPos, prevEndPos, -1);
            edge2 = new AbstractTrailParticle.TrailEdge(middleStartPos, middleEndPos, -1);
         } else {
            edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount() / 2 + 1));
            edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
            edge2.lifetime++;
         }

         startPosList.add(edge1.start);
         endPosList.add(edge1.end);
         startPosList.add(edge2.start);
         endPosList.add(edge2.end);
         startPosList.add(middleStartPos);
         endPosList.add(middleEndPos);
         startPosList.add(currentStartPos);
         endPosList.add(currentEndPos);
         List<Vec3> finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount());
         List<Vec3> finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount());
         if (!isFirstTrail) {
            finalStartPositions.remove(0);
            finalEndPositions.remove(0);
         }

         this.makeTrailEdges(finalStartPositions, finalEndPositions, this.trailEdges);
         this.lastXRot = xRot;
         this.lastYRot = yRot;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public static final TrailInfo ARROW_TRAIL_DEFAULT = TrailInfo.builder()
         .type((SimpleParticleType)EpicFightParticles.PROJECTILE_TRAIL.get())
         .startPos(new Vec3(-0.1, 0.0, 0.7))
         .endPos(new Vec3(0.1, 0.0, 0.7))
         .interpolations(4)
         .lifetime(9)
         .updateInterval(1)
         .texture(EpicFightMod.identifier("textures/particle/projectile_trail.png"))
         .create();
      public static final TrailInfo SPECTRAL_ARROW_TRAIL_DEFAULT = TrailInfo.builder()
         .type((SimpleParticleType)EpicFightParticles.PROJECTILE_TRAIL.get())
         .startPos(new Vec3(-0.1, 0.0, 0.7))
         .endPos(new Vec3(0.1, 0.0, 0.7))
         .interpolations(4)
         .lifetime(9)
         .updateInterval(1)
         .r(0.9882353F)
         .g(0.9882353F)
         .b(0.4627451F)
         .texture(EpicFightMod.identifier("textures/particle/projectile_trail.png"))
         .create();
      public static final TrailInfo TRIDENT_TRAIL_DEFAULT = TrailInfo.builder()
         .type((SimpleParticleType)EpicFightParticles.PROJECTILE_TRAIL.get())
         .startPos(new Vec3(-0.1, 0.0, 1.8))
         .endPos(new Vec3(0.1, 0.0, 1.8))
         .interpolations(4)
         .lifetime(9)
         .updateInterval(1)
         .r(0.0F)
         .g(0.9098039F)
         .b(0.9607843F)
         .texture(EpicFightMod.identifier("textures/particle/projectile_trail.png"))
         .create();

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         int eid = (int)Double.doubleToRawLongBits(x);
         Entity entity = level.m_6815_(eid);
         if (entity == null) {
            return null;
         }

         if (!(entity instanceof AbstractArrow)) {
            return null;
         }

         ProjectilePatch<AbstractArrow> entitypatch = EpicFightCapabilities.getEntityPatch(entity, ProjectilePatch.class);
         if (entitypatch != null) {
            TrailInfo trailInfo;
            if (entitypatch.getOriginal() instanceof Arrow) {
               trailInfo = ARROW_TRAIL_DEFAULT;
            } else if (entitypatch.getOriginal() instanceof SpectralArrow) {
               trailInfo = SPECTRAL_ARROW_TRAIL_DEFAULT;
            } else {
               if (!(entitypatch.getOriginal() instanceof ThrownTrident)) {
                  return null;
               }

               trailInfo = TRIDENT_TRAIL_DEFAULT;
            }

            return new ProjectileTrailParticle(level, entitypatch, trailInfo);
         } else {
            return null;
         }
      }
   }
}
