package yesman.epicfight.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DeathHarvestOrb extends Entity {
   private Player dest;
   private Vec3 randVec;
   private float value;

   public DeathHarvestOrb(EntityType<?> type, Level level) {
      super(type, level);
   }

   public DeathHarvestOrb(Player dest, double x, double y, double z, int value) {
      this((EntityType<?>)EpicFightEntities.DEATH_HARVEST_ORB.get(), dest.m_9236_());
      this.m_6034_(x, y, z);
      this.dest = dest;
      this.value = value;
      Vec3 toContrast = this.dest.m_20182_().m_82520_(0.0, this.dest.m_20206_() * 0.5, 0.0).m_82546_(this.m_20182_()).m_82490_(-1.0);
      double randX = this.f_19796_.m_188500_() * (toContrast.f_82479_ > 0.0 ? 1.0 : -1.0);
      double randY = this.f_19796_.m_188500_() * (toContrast.f_82480_ > 0.0 ? 0.75 : -0.75);
      double randZ = this.f_19796_.m_188500_() * (toContrast.f_82481_ > 0.0 ? 1.0 : -1.0);
      this.randVec = new Vec3(randX, randY, randZ).m_82541_();
   }

   public void m_8119_() {
      this.f_19794_ = true;
      if (!this.m_9236_().m_5776_()) {
         double scaleFactor = Math.pow(Math.max(0.0, (this.f_19797_ - 10) / 10.0), 2.0);
         Vec3 destCenter = this.dest.m_20182_().m_82520_(0.0, this.dest.m_20206_() * 0.5, 0.0);
         Vec3 v1 = destCenter.m_82546_(this.m_20182_()).m_82490_(scaleFactor);
         Vec3 v2 = this.randVec.m_82490_(1.0 - scaleFactor);
         this.m_6478_(MoverType.SELF, v1.m_82549_(v2).m_82490_(0.23));
         if (this.m_20182_().m_82557_(destCenter) < 0.5625 || this.f_19797_ > 40) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(this.dest, ServerPlayerPatch.class).ifPresent(playerpatch -> {
               SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
               container.getSkill().setConsumptionSynchronize(container, container.getResource() + this.value);
            });
            this.m_146870_();
         }
      } else {
         this.m_9236_().m_7106_(ParticleTypes.f_123762_, this.m_20185_(), this.m_20186_(), this.m_20189_(), 0.0, 0.0, 0.0);
      }
   }

   public void m_7378_(CompoundTag tag) {
   }

   public void m_7380_(CompoundTag tag) {
   }

   protected void m_8097_() {
   }

   public Packet<ClientGamePacketListener> m_5654_() {
      return new ClientboundAddEntityPacket(this);
   }
}
