package yesman.epicfight.client.particle;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public abstract class AbstractTrailParticle<T extends EntityPatch<?>> extends TextureSheetParticle {
   protected final TrailInfo trailInfo;
   protected final T owner;
   protected final List<AbstractTrailParticle.TrailEdge> trailEdges;
   protected float startEdgeCorrection = 0.0F;
   protected boolean shouldRemove;

   protected AbstractTrailParticle(ClientLevel level, T entitypatch, TrailInfo trailInfo) {
      super(level, 0.0, 0.0, 0.0);
      this.f_107219_ = false;
      this.owner = entitypatch;
      this.trailEdges = Lists.newLinkedList();
      this.trailInfo = trailInfo;
      Vec3 entityPos = entitypatch.getOriginal().m_20182_();
      this.m_6257_(entityPos.f_82479_, entityPos.f_82480_ + entitypatch.getOriginal().m_20192_(), entityPos.f_82481_);
      float size = (float)Math.max(this.trailInfo.start().m_82553_(), this.trailInfo.end().m_82553_()) * 2.0F;
      this.m_107250_(size, size);
      this.f_107227_ = Math.max(this.trailInfo.rCol(), 0.0F);
      this.f_107228_ = Math.max(this.trailInfo.gCol(), 0.0F);
      this.f_107229_ = Math.max(this.trailInfo.bCol(), 0.0F);
   }

   @Deprecated
   protected AbstractTrailParticle(T entitypatch, TrailInfo trailInfo) {
      super(null, 0.0, 0.0, 0.0);
      this.owner = entitypatch;
      this.trailEdges = Lists.newLinkedList();
      this.trailInfo = trailInfo;
      this.f_107227_ = Math.max(this.trailInfo.rCol(), 0.0F);
      this.f_107228_ = Math.max(this.trailInfo.gCol(), 0.0F);
      this.f_107229_ = Math.max(this.trailInfo.bCol(), 0.0F);
   }

   protected abstract boolean canContinue();

   protected boolean canCreateNextCurve() {
      return this.f_107224_ % this.trailInfo.updateInterval() == 0 && !this.f_107220_;
   }

   protected abstract void createNextCurve();

   public void m_5989_() {
      if (this.shouldRemove) {
         if (this.f_107224_ >= this.f_107225_) {
            this.m_107274_();
         }
      } else if (!this.canContinue()) {
         this.shouldRemove = true;
         this.f_107225_ = this.f_107224_ + this.trailInfo.trailLifetime();
      }

      this.f_107224_++;
      this.trailEdges.removeIf(v -> !v.isAlive());
      if (this.canCreateNextCurve()) {
         Vec3 lastPos = this.owner.getOriginal().m_20318_(0.0F);
         double xd = Math.pow(this.owner.getOriginal().m_20185_() - lastPos.f_82479_, 2.0);
         double yd = Math.pow(this.owner.getOriginal().m_20186_() - lastPos.f_82480_, 2.0);
         double zd = Math.pow(this.owner.getOriginal().m_20189_() - lastPos.f_82481_, 2.0);
         float move = (float)Math.sqrt(xd + yd + zd) * 2.0F;
         this.m_107250_(this.f_107221_ + move, this.f_107222_ + move);
         this.createNextCurve();
      }
   }

   public void m_5744_(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
      if (!this.trailEdges.isEmpty()) {
         PoseStack poseStack = new PoseStack();
         int light = this.m_6355_(partialTick);
         this.setupPoseStack(poseStack, camera, partialTick);
         Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
         int edges = this.trailEdges.size() - 1;
         boolean startFade = this.trailEdges.get(0).lifetime == 1;
         boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime();
         float startEdge = (startFade ? this.trailInfo.interpolateCount() * 2 * partialTick : 0.0F) + this.startEdgeCorrection;
         float endEdge = endFade ? Math.min(edges - this.trailInfo.interpolateCount() * 2 * (1.0F - partialTick), edges - 1) : edges - 1;
         float interval = 1.0F / (endEdge - startEdge);
         float fading = 1.0F;
         if (this.shouldRemove) {
            if (TrailInfo.isValidTime(this.trailInfo.fadeTime())) {
               fading = (float)(this.f_107225_ - this.f_107224_) / this.trailInfo.trailLifetime();
            } else {
               fading = Mth.m_14036_((this.f_107225_ - this.f_107224_ + (1.0F - partialTick)) / this.trailInfo.trailLifetime(), 0.0F, 1.0F);
            }
         }

         float partialStartEdge = interval * (startEdge % 1.0F);
         float from = -partialStartEdge;
         float to = -partialStartEdge + interval;

         for (int i = (int)startEdge; i < (int)endEdge + 1; i++) {
            AbstractTrailParticle.TrailEdge e1 = this.trailEdges.get(i);
            AbstractTrailParticle.TrailEdge e2 = this.trailEdges.get(i + 1);
            Vector4f pos1 = new Vector4f((float)e1.start.f_82479_, (float)e1.start.f_82480_, (float)e1.start.f_82481_, 1.0F);
            Vector4f pos2 = new Vector4f((float)e1.end.f_82479_, (float)e1.end.f_82480_, (float)e1.end.f_82481_, 1.0F);
            Vector4f pos3 = new Vector4f((float)e2.end.f_82479_, (float)e2.end.f_82480_, (float)e2.end.f_82481_, 1.0F);
            Vector4f pos4 = new Vector4f((float)e2.start.f_82479_, (float)e2.start.f_82480_, (float)e2.start.f_82481_, 1.0F);
            pos1.mul(matrix4f);
            pos2.mul(matrix4f);
            pos3.mul(matrix4f);
            pos4.mul(matrix4f);
            float alphaFrom = Mth.m_14036_(from, 0.0F, 1.0F);
            float alphaTo = Mth.m_14036_(to, 0.0F, 1.0F);
            vertexConsumer.m_5483_(pos1.x(), pos1.y(), pos1.z())
               .m_7421_(from, 1.0F)
               .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaFrom * fading)
               .m_85969_(light)
               .m_5752_();
            vertexConsumer.m_5483_(pos2.x(), pos2.y(), pos2.z())
               .m_7421_(from, 0.0F)
               .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaFrom * fading)
               .m_85969_(light)
               .m_5752_();
            vertexConsumer.m_5483_(pos3.x(), pos3.y(), pos3.z())
               .m_7421_(to, 0.0F)
               .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaTo * fading)
               .m_85969_(light)
               .m_5752_();
            vertexConsumer.m_5483_(pos4.x(), pos4.y(), pos4.z())
               .m_7421_(to, 1.0F)
               .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaTo * fading)
               .m_85969_(light)
               .m_5752_();
            from += interval;
            to += interval;
         }
      }
   }

   public boolean shouldCull() {
      return false;
   }

   public ParticleRenderType m_7556_() {
      return EpicFightParticleRenderTypes.TRAIL_EFFECT.apply(this.trailInfo.texturePath());
   }

   protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTicks) {
      Vec3 vec3 = camera.m_90583_();
      float x = (float)(-vec3.m_7096_());
      float y = (float)(-vec3.m_7098_());
      float z = (float)(-vec3.m_7094_());
      poseStack.m_252880_(x, y, z);
   }

   protected void makeTrailEdges(List<Vec3> startPositions, List<Vec3> endPositions, List<AbstractTrailParticle.TrailEdge> dest) {
      for (int i = 0; i < startPositions.size(); i++) {
         dest.add(new AbstractTrailParticle.TrailEdge(startPositions.get(i), endPositions.get(i), this.trailInfo.trailLifetime()));
      }
   }

   protected int m_6355_(float pPartialTick) {
      BlockPos blockpos = BlockPos.m_274561_(this.f_107212_, this.f_107213_, this.f_107214_);
      return this.f_107208_.m_46805_(blockpos) ? this.getLightColor(this.f_107208_, this.f_107208_.m_8055_(blockpos), blockpos) : 0;
   }

   private int getLightColor(BlockAndTintGetter level, BlockState state, BlockPos pos) {
      if (state.m_60788_(level, pos)) {
         return 15728880;
      }

      int i = Mth.m_14045_(Math.max(this.trailInfo.skyLight(), level.m_45517_(LightLayer.SKY, pos)), 0, 15);
      int j = Mth.m_14045_(Math.max(this.trailInfo.blockLight(), level.m_45517_(LightLayer.BLOCK, pos)), 0, 15);
      int k = state.getLightEmission(level, pos);
      if (j < k) {
         j = k;
      }

      return i << 20 | j << 4;
   }

   public static class TrailEdge {
      public final Vec3 start;
      public final Vec3 end;
      public int lifetime;

      public TrailEdge(Vec3 start, Vec3 end, int lifetime) {
         this.start = start;
         this.end = end;
         this.lifetime = lifetime;
      }

      public boolean isAlive() {
         return --this.lifetime > 0;
      }
   }
}
