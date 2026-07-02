package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import yesman.epicfight.api.client.forgeevent.ProcessEntityPairingPacketEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.network.server.SPEntityPairingPacket;

public abstract class EntityPatch<T extends Entity> {
   protected T original;
   protected boolean initialized = false;

   public void onOldPosUpdate() {
   }

   public void onAddedToWorld() {
   }

   public abstract boolean overrideRender();

   public void onStartTracking(ServerPlayer trackingPlayer) {
   }

   public void onStopTracking(ServerPlayer trackingPlayer) {
   }

   public void onConstructed(T entity) {
      this.original = entity;
   }

   public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
      this.initialized = true;
   }

   public void onDeath(LivingDeathEvent event) {
   }

   public final T getOriginal() {
      return this.original;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public boolean isLogicalClient() {
      return this.original.m_9236_().m_5776_();
   }

   public OpenMatrix4f getMatrix(float partialTick) {
      return MathUtils.getModelMatrixIntegral(
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         this.original.f_19860_,
         this.original.m_146909_(),
         this.original.f_19859_,
         this.original.m_146908_(),
         partialTick,
         1.0F,
         1.0F,
         1.0F
      );
   }

   public double getAngleTo(Entity entity) {
      Vec3 a = this.original.m_20154_();
      Vec3 b = new Vec3(
            entity.m_20185_() - this.original.m_20185_(), entity.m_20186_() - this.original.m_20186_(), entity.m_20189_() - this.original.m_20189_()
         )
         .m_82541_();
      double cos = a.f_82479_ * b.f_82479_ + a.f_82480_ * b.f_82480_ + a.f_82481_ * b.f_82481_;
      return Math.toDegrees(Math.acos(cos));
   }

   public double getAngleToHorizontal(Entity entity) {
      Vec3 a = this.original.m_20154_();
      Vec3 b = new Vec3(entity.m_20185_() - this.original.m_20185_(), 0.0, entity.m_20189_() - this.original.m_20189_()).m_82541_();
      double cos = a.f_82479_ * b.f_82479_ + a.f_82480_ * b.f_82480_ + a.f_82481_ * b.f_82481_;
      return Math.toDegrees(Math.acos(cos));
   }

   public abstract OpenMatrix4f getModelMatrix(float var1);

   @OnlyIn(Dist.CLIENT)
   public void fireEntityPairingEvent(SPEntityPairingPacket msg) {
      ProcessEntityPairingPacketEvent pairingPacketEvent = new ProcessEntityPairingPacketEvent(this, msg);
      MinecraftForge.EVENT_BUS.post(pairingPacketEvent);
      if (!pairingPacketEvent.isCanceled()) {
         this.entityPairing(msg);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void entityPairing(SPEntityPairingPacket packet) {
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isOutlineVisible(LocalPlayer player) {
      return true;
   }
}
