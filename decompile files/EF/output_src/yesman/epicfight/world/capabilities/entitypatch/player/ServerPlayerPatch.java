package yesman.epicfight.world.capabilities.entitypatch.player;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.forgeevent.InnateSkillChangeEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.network.server.SPInitSkills;
import yesman.epicfight.network.server.SPModifyPlayerData;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.DodgeSuccessEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SetTargetEvent;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

public class ServerPlayerPatch extends PlayerPatch<ServerPlayer> {
   private LivingEntity attackTarget;
   private boolean updatedMotionCurrentTick;

   public void onJoinWorld(ServerPlayer player, EntityJoinLevelEvent event) {
      super.onJoinWorld(player, event);
      EpicFightNetworkManager.sendToPlayer(new SPInitSkills(this.getSkillCapability()), player);
      this.eventListeners
         .addEventListener(
            PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE,
            PLAYER_EVENT_UUID,
            playerevent -> {
               if (playerevent.getDamageSource().isBasicAttack()) {
                  SkillContainer container = this.getSkill(SkillSlots.WEAPON_INNATE);
                  ItemStack mainHandItem = this.getOriginal().m_21205_();
                  if (!container.isFull()
                     && !container.isActivated()
                     && container.hasSkill(EpicFightCapabilities.getItemStackCapability(mainHandItem).getInnateSkill(this, mainHandItem))) {
                     float value = container.getResource() + playerevent.getAttackDamage();
                     if (value > 0.0F) {
                        container.getSkill().setConsumptionSynchronize(container, value);
                     }
                  }
               }
            },
            10
         );
   }

   @Override
   public void onStartTracking(ServerPlayer trackingPlayer) {
      EpicFightNetworkManager.PayloadBundleBuilder payloadBundleBuilder = EpicFightNetworkManager.PayloadBundleBuilder.create();
      SPChangeLivingMotion msg = new SPChangeLivingMotion(this.getOriginal().m_19879_());
      msg.putEntries(this.<Animator>getAnimator().getLivingAnimations().entrySet());
      payloadBundleBuilder.and(msg);
      this.getSkillCapability()
         .listSkillContainers()
         .filter(skillContainer -> !skillContainer.isEmpty() && skillContainer.getSkill().getCategory().shouldSynchronize())
         .forEach(skillContainer -> {
            payloadBundleBuilder.and(skillContainer.createSyncPacketToRemotePlayer());
            skillContainer.getDataManager().onTracked(payloadBundleBuilder);
            skillContainer.getSkill().onTracked(skillContainer, payloadBundleBuilder);
         });
      payloadBundleBuilder.and(SPModifyPlayerData.setPlayerMode(this.getOriginal().m_19879_(), this.playerMode));
      payloadBundleBuilder.send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, trackingPlayer, others));
   }

   @Override
   public void tick(LivingTickEvent event) {
      super.tick(event);
      this.updatedMotionCurrentTick = false;
   }

   @Override
   public void updateMotion(boolean considerInaction) {
   }

   @Override
   public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
      if (this.isHoldingAny()) {
         this.getSkillContainerFor(this.holdingSkill.asSkill()).ifPresent(container -> {
            container.getSkill().cancelOnServer(container, null);
            EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.expired(container.getSlotId()), this.original);
         });
         this.resetHolding();
      }

      CapabilityItem mainHandCap = hand == InteractionHand.MAIN_HAND ? toCap : this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
      mainHandCap.changeWeaponInnateSkill(this, hand == InteractionHand.MAIN_HAND ? to : this.original.m_21205_());
      MinecraftForge.EVENT_BUS.post(new InnateSkillChangeEvent(this, from, fromCap, to, toCap, hand));
      if (hand == InteractionHand.OFF_HAND) {
         if (!from.m_41619_()) {
            Multimap<Attribute, AttributeModifier> modifiers = from.m_41638_(EquipmentSlot.MAINHAND);
            modifiers.get(Attributes.f_22283_).forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::m_22130_);
         }

         if (!fromCap.isEmpty()) {
            Multimap<Attribute, AttributeModifier> modifiers = fromCap.getAllAttributeModifiers(EquipmentSlot.MAINHAND);
            modifiers.get((Attribute)EpicFightAttributes.ARMOR_NEGATION.get())
               .forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::m_22130_);
            modifiers.get((Attribute)EpicFightAttributes.IMPACT.get())
               .forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_IMPACT.get())::m_22130_);
            modifiers.get((Attribute)EpicFightAttributes.MAX_STRIKES.get())
               .forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::m_22130_);
            modifiers.get(Attributes.f_22283_).forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::m_22130_);
         }

         if (!to.m_41619_()) {
            Multimap<Attribute, AttributeModifier> modifiers = to.m_41638_(EquipmentSlot.MAINHAND);
            modifiers.get(Attributes.f_22283_).forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::m_22118_);
         }

         if (!toCap.isEmpty()) {
            Multimap<Attribute, AttributeModifier> modifiers = toCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this);
            modifiers.get((Attribute)EpicFightAttributes.ARMOR_NEGATION.get())
               .forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::m_22118_);
            modifiers.get((Attribute)EpicFightAttributes.IMPACT.get())
               .forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_IMPACT.get())::m_22118_);
            modifiers.get((Attribute)EpicFightAttributes.MAX_STRIKES.get())
               .forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::m_22118_);
            modifiers.get(Attributes.f_22283_).forEach(this.original.m_21051_((Attribute)EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::m_22118_);
         }
      }

      this.modifyLivingMotionByCurrentItem(true);
      super.updateHeldItem(fromCap, toCap, from, to, hand);
   }

   public void modifyLivingMotionByCurrentItem() {
      this.modifyLivingMotionByCurrentItem(false);
   }

   public void modifyLivingMotionByCurrentItem(boolean checkOldAnimations) {
      if (!this.updatedMotionCurrentTick || !checkOldAnimations) {
         Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> oldLivingAnimations = this.<Animator>getAnimator().getLivingAnimations();
         Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> newLivingAnimations = Maps.newHashMap();
         CapabilityItem mainhandCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
         CapabilityItem offhandCap = this.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
         Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> livingMotionModifiers = new HashMap<>(
            mainhandCap.getLivingMotionModifier(this, InteractionHand.MAIN_HAND)
         );
         livingMotionModifiers.putAll(offhandCap.getLivingMotionModifier(this, InteractionHand.OFF_HAND));

         for (Entry<LivingMotion, AssetAccessor<? extends StaticAnimation>> entry : livingMotionModifiers.entrySet()) {
            AssetAccessor<? extends StaticAnimation> aniamtion = entry.getValue();
            if (!oldLivingAnimations.containsKey(entry.getKey())) {
               this.updatedMotionCurrentTick = true;
            } else if (oldLivingAnimations.get(entry.getKey()) != aniamtion) {
               this.updatedMotionCurrentTick = true;
            }

            newLivingAnimations.put(entry.getKey(), aniamtion);
         }

         for (LivingMotion oldLivingMotion : oldLivingAnimations.keySet()) {
            if (!newLivingAnimations.containsKey(oldLivingMotion)) {
               this.updatedMotionCurrentTick = true;
               break;
            }
         }

         if (this.updatedMotionCurrentTick || !checkOldAnimations) {
            this.<Animator>getAnimator().resetLivingAnimations();
            newLivingAnimations.forEach(this.getAnimator()::addLivingAnimation);
            SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.m_19879_());
            msg.putEntries(newLivingAnimations.entrySet());
            EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(msg, this.original);
         }
      }
   }

   @Override
   public void sendToAllPlayersTrackingMe(Object packet) {
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(packet, this.original);
   }

   @Override
   public void setModelYRot(float amount, boolean sendPacket) {
      super.setModelYRot(amount, sendPacket);
      if (sendPacket) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(
            SPModifyPlayerData.setPlayerYRot(this.original.m_19879_(), this.modelYRot), this.original
         );
      }
   }

   @Override
   public void disableModelYRot(boolean sendPacket) {
      super.disableModelYRot(sendPacket);
      if (sendPacket) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.disablePlayerYRot(this.original.m_19879_()), this.original);
      }
   }

   @Override
   public AttackResult tryHurt(DamageSource damageSource, float amount) {
      if (this.getOriginal().m_150110_().f_35934_ && !damageSource.m_269533_(DamageTypeTags.f_268738_)) {
         return AttackResult.missed(amount);
      }

      TakeDamageEvent.Attack hurtEvent = new TakeDamageEvent.Attack(this, damageSource, amount);
      return this.getEventListener().triggerEvents(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, hurtEvent)
         ? AttackResult.missed(hurtEvent.getDamage())
         : super.tryHurt(damageSource, amount);
   }

   @Override
   public void onDodgeSuccess(DamageSource damageSource, Vec3 location) {
      super.onDodgeSuccess(damageSource, location);
      DodgeSuccessEvent dodgeSuccessEvent = new DodgeSuccessEvent(this, damageSource, location);
      this.getEventListener().triggerEvents(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, dodgeSuccessEvent);
   }

   @Override
   public void toVanillaMode(boolean synchronize) {
      super.toVanillaMode(synchronize);
      if (synchronize) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(
            SPModifyPlayerData.setPlayerMode(this.original.m_19879_(), PlayerPatch.PlayerMode.VANILLA), this.original
         );
      }
   }

   @Override
   public void toEpicFightMode(boolean synchronize) {
      super.toEpicFightMode(synchronize);
      if (synchronize) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(
            SPModifyPlayerData.setPlayerMode(this.original.m_19879_(), PlayerPatch.PlayerMode.EPICFIGHT), this.original
         );
      }
   }

   @Override
   public boolean isTargetInvulnerable(Entity target) {
      return target instanceof Player && !this.getOriginal().f_8924_.m_129799_() ? true : super.isTargetInvulnerable(target);
   }

   @Override
   public void setLastAttackSuccess(boolean setter) {
      if (setter) {
         EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setLastAttackResult(this.original.m_19879_(), true), this.original);
      }

      this.isLastAttackSuccess = setter;
   }

   public void setAttackTarget(LivingEntity entity) {
      SetTargetEvent setTargetEvent = new SetTargetEvent(this, entity);
      this.getEventListener().triggerEvents(PlayerEventListener.EventType.SET_TARGET_EVENT, setTargetEvent);
      this.attackTarget = setTargetEvent.getTarget();
   }

   @Override
   public boolean startSkillHolding(HoldableSkill chargingSkill) {
      if (super.startSkillHolding(chargingSkill)) {
         EpicFightNetworkManager.sendToPlayer(
            SPSkillExecutionFeedback.held(this.getSkillContainerFor(chargingSkill.asSkill()).get().getSlotId()), this.getOriginal()
         );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public LivingEntity getTarget() {
      return this.attackTarget;
   }

   @Override
   public void setGrapplingTarget(LivingEntity grapplingTarget) {
      super.setGrapplingTarget(grapplingTarget);
      EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setGrapplingTarget(this.original.m_19879_(), grapplingTarget), this.original);
   }
}
