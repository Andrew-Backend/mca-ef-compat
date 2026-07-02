package yesman.epicfight.world.capabilities.projectile;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.weaponinnate.EverlastingAllegiance;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

public class ThrownTridentPatch extends ProjectilePatch<ThrownTrident> {
   private boolean innateActivated;
   private int returnTick;
   private float independentXRotO;
   private float independentXRot;
   public float renderXRot;
   public float renderXRotO;
   public float renderYRot;
   public float renderYRotO;

   @Override
   public void onStartTracking(ServerPlayer trackingPlayer) {
      if (this.innateActivated) {
         SPEntityPairingPacket packet = new SPEntityPairingPacket(this.original.m_19879_(), EntityPairingPacketTypes.TRIDENT_THROWN);
         packet.getBuffer().writeInt(this.returnTick);
         packet.getBuffer().writeInt(this.original.f_19797_);
         EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void entityPairing(SPEntityPairingPacket packet) {
      super.entityPairing(packet);
      if (packet.getPairingPacketType() == EntityPairingPacketTypes.TRIDENT_THROWN) {
         this.innateActivated = true;
         this.returnTick = packet.getBuffer().readInt();
         this.original.f_19797_ = packet.getBuffer().readInt();
      }
   }

   protected void setMaxStrikes(ThrownTrident projectileEntity, int maxStrikes) {
      projectileEntity.m_36767_((byte)(maxStrikes - 1));
   }

   public void onJoinWorld(ThrownTrident projectileEntity, EntityJoinLevelEvent event) {
      super.onJoinWorld(projectileEntity, event);
      if (!this.isLogicalClient()) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(projectileEntity.m_19749_(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
            SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
            if (container.getSkill() instanceof EverlastingAllegiance) {
               EverlastingAllegiance.setThrownTridentEntityId(container, projectileEntity.m_19879_());
            }
         });
         this.armorNegation = 20.0F;
      }
   }

   public void tickEnd() {
      if (!this.isLogicalClient()) {
         if (this.original.f_37556_) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(this.original.m_19749_(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
               SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
               if (container.getSkill() instanceof EverlastingAllegiance && EverlastingAllegiance.getThrownTridentEntityId(container) > -1) {
                  EverlastingAllegiance.setThrownTridentEntityId(container, -1);
               }
            });
         }

         if (this.innateActivated) {
            List<Entity> entities = this.original.m_9236_().m_45933_(this.original, this.original.m_20191_().m_82377_(1.0, 1.0, 1.0));
            EpicFightDamageSource source = EpicFightDamageSources.trident(this.original.m_19749_(), this.original)
               .setStunType(StunType.HOLD)
               .addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE)
               .addExtraDamage(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create())
               .setBaseArmorNegation(30.0F)
               .attachDamageModifier(ValueModifier.multiplier(1.4F));

            for (Entity entity : entities) {
               if (!entity.m_7306_(this.original.m_19749_())) {
                  float f = 8.0F;
                  if (entity instanceof LivingEntity livingentity) {
                     f += EnchantmentHelper.m_44833_(this.original.f_37555_, livingentity.m_6336_());
                     if (entity.m_6469_(source, f)) {
                        entity.m_5496_((SoundEvent)EpicFightSounds.BLADE_HIT.get(), 1.0F, 1.0F);
                        ((ServerLevel)entity.m_9236_())
                           .m_8767_(
                              (HitParticleType)EpicFightParticles.HIT_BLADE.get(),
                              entity.m_20182_().f_82479_,
                              entity.m_20182_().f_82480_ + entity.m_20206_() * 0.5,
                              entity.m_20182_().f_82481_,
                              0,
                              0.0,
                              0.0,
                              0.0,
                              1.0
                           );
                     }
                  }
               }
            }
         }
      }

      if (this.innateActivated) {
         int elapsedTicks = Math.max(this.original.f_19797_ - this.returnTick - 10, 0);
         Vec3 toOwner = this.original.m_19749_().m_146892_().m_82546_(this.original.m_20182_());
         double length = toOwner.m_82553_();
         double speed = Math.min(Math.pow(elapsedTicks, 2.0) * 5.0E-4 + Math.abs(elapsedTicks * 0.05), Math.min(10.0, length));
         Vec3 toMaster = toOwner.m_82541_().m_82490_(speed);
         this.original.m_20256_(new Vec3(0.0, 0.0, 0.0));
         Vec3 pos = this.original.m_20182_();
         this.original.m_6034_(pos.f_82479_ + toMaster.f_82479_, pos.f_82480_ + toMaster.f_82480_, pos.f_82481_ + toMaster.f_82481_);
         this.original.m_146926_(0.0F);
         this.original.f_19860_ = 0.0F;
         this.original.m_146922_(0.0F);
         this.original.f_19859_ = 0.0F;
         this.independentXRotO = this.independentXRot;
         this.independentXRot += 60.0F;
         this.original.f_19860_ = this.independentXRotO;
         this.original.m_146926_(this.independentXRot);
         if (this.original.f_19797_ % 3 == 0) {
            this.original.m_5496_((SoundEvent)EpicFightSounds.WHOOSH_ROD.get(), 3.0F, 1.0F);
         }
      }
   }

   public boolean isInnateActivated() {
      return this.innateActivated;
   }

   public void catchByPlayer(PlayerPatch<?> playerpatch) {
      playerpatch.playAnimationSynchronized(Animations.EVERLASTING_ALLEGIANCE_CATCH, 0.0F);
   }

   public void recalledBySkill() {
      this.original.m_5496_(SoundEvents.f_12516_, 10.0F, 1.0F);
      this.original.f_37556_ = true;
      this.innateActivated = true;
      this.independentXRot = this.original.m_146909_();
      this.returnTick = this.original.f_19797_;
      this.initialFirePosition = this.original.m_20182_();
   }

   @Override
   public EpicFightDamageSource createEpicFightDamageSource() {
      return EpicFightDamageSources.trident(this.original, this.original.m_19749_())
         .setStunType(StunType.SHORT)
         .addRuntimeTag(DamageTypeTags.f_268524_)
         .setBaseArmorNegation(this.armorNegation)
         .setBaseImpact(this.impact)
         .setInitialPosition(this.initialFirePosition);
   }
}
