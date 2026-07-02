package yesman.epicfight.world.capabilities.entitypatch;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSetAttackTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public abstract class MobPatch<T extends Mob> extends LivingEntityPatch<T> {
   protected final Faction mobFaction;

   public MobPatch() {
      this.mobFaction = Factions.NEUTRAL;
   }

   public MobPatch(Faction faction) {
      this.mobFaction = faction;
   }

   public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
      super.onJoinWorld(entity, event);
      if (!entity.m_9236_().m_5776_() && !this.original.m_21525_()) {
         this.initAI();
      }
   }

   protected void initAI() {
      if (this.original.m_6274_().f_21845_.isEmpty()) {
         Set<Goal> toRemove = Sets.newHashSet();
         this.selectGoalToRemove(toRemove);
         toRemove.forEach(this.original.f_21345_::m_25363_);
      }
   }

   protected void selectGoalToRemove(Set<Goal> toRemove) {
      for (WrappedGoal wrappedGoal : this.original.f_21345_.m_148105_()) {
         Goal goal = wrappedGoal.m_26015_();
         if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof RangedAttackGoal || goal instanceof TargetChasingGoal) {
            toRemove.add(goal);
         }
      }
   }

   protected final void commonMobUpdateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else if (this.state.inaction() && considerInaction) {
         this.currentLivingMotion = LivingMotions.INACTION;
      } else if (this.original.m_20202_() != null) {
         this.currentLivingMotion = LivingMotions.MOUNT;
      } else if (!(this.original.m_20184_().f_82480_ < -0.55F) && !this.isAirborneState()) {
         if (this.original.f_267362_.m_267731_() > 0.01F) {
            this.currentLivingMotion = LivingMotions.WALK;
         } else {
            this.currentLivingMotion = LivingMotions.IDLE;
         }
      } else {
         this.currentLivingMotion = LivingMotions.FALL;
      }

      this.currentCompositeMotion = this.currentLivingMotion;
   }

   protected final void commonAggressiveMobUpdateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else if (this.state.inaction() && considerInaction) {
         this.currentLivingMotion = LivingMotions.IDLE;
      } else if (this.original.m_20202_() != null) {
         this.currentLivingMotion = LivingMotions.MOUNT;
      } else if (!(this.original.m_20184_().f_82480_ < -0.55F) && !this.isAirborneState()) {
         if (this.original.f_267362_.m_267731_() > 0.08F) {
            if (this.original.m_5912_()) {
               this.currentLivingMotion = LivingMotions.CHASE;
            } else {
               this.currentLivingMotion = LivingMotions.WALK;
            }
         } else {
            this.currentLivingMotion = LivingMotions.IDLE;
         }
      } else {
         this.currentLivingMotion = LivingMotions.FALL;
      }

      this.currentCompositeMotion = this.currentLivingMotion;
   }

   protected final void commonAggressiveRangedMobUpdateMotion(boolean considerInaction) {
      this.commonAggressiveMobUpdateMotion(considerInaction);
      UseAnim useAction = this.original.m_21120_(this.original.m_7655_()).m_41780_();
      if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getRealAnimation().get().isReboundAnimation()) {
         this.currentCompositeMotion = LivingMotions.SHOT;
      } else if (this.original.m_6117_()) {
         if (useAction == UseAnim.CROSSBOW) {
            this.currentCompositeMotion = LivingMotions.RELOAD;
         } else {
            this.currentCompositeMotion = LivingMotions.AIM;
         }
      } else if (CrossbowItem.m_40932_(this.original.m_21205_())) {
         this.currentCompositeMotion = LivingMotions.AIM;
      } else {
         this.currentCompositeMotion = this.currentLivingMotion;
      }
   }

   @Override
   public boolean isTargetInvulnerable(Entity entity) {
      MobPatch<?> mobpatch = EpicFightCapabilities.getEntityPatch(entity, MobPatch.class);
      if (mobpatch == null || !mobpatch.mobFaction.equals(this.mobFaction)) {
         return super.isTargetInvulnerable(entity);
      } else {
         return this.getTarget() == null ? true : !this.getTarget().m_7306_(entity);
      }
   }

   @Override
   public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
      boolean offhandValid = this.isOffhandItemValid();
      ItemStack mainHandItem = this.getOriginal().m_21205_();
      ItemStack offHandItem = this.getOriginal().m_21206_();
      Collection<AttributeModifier> mainHandAttributes = CapabilityItem.getAttributeModifiers(
         Attributes.f_22281_, EquipmentSlot.MAINHAND, this.original.m_21205_(), this
      );
      Collection<AttributeModifier> offHandAttributes = this.isOffhandItemValid()
         ? CapabilityItem.getAttributeModifiers(Attributes.f_22281_, EquipmentSlot.MAINHAND, this.original.m_21206_(), this)
         : Set.of();
      this.epicFightDamageSource = damageSource;
      this.setOffhandDamage(hand, mainHandItem, offHandItem, offhandValid, mainHandAttributes, offHandAttributes);
      this.original.m_7327_(target);
      this.recoverMainhandDamage(hand, mainHandItem, offHandItem, mainHandAttributes, offHandAttributes);
      this.epicFightDamageSource = null;
      return super.attack(damageSource, target, hand);
   }

   @Override
   public LivingEntity getTarget() {
      return this.original.m_5448_();
   }

   public void setAttakTargetSync(LivingEntity entityIn) {
      if (!this.original.m_9236_().m_5776_()) {
         this.original.m_6710_(entityIn);
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
            new SPSetAttackTarget(this.original.m_19879_(), entityIn != null ? entityIn.m_19879_() : -1), this.original
         );
      }
   }

   @Override
   public float getAttackDirectionPitch() {
      Entity attackTarget = this.getTarget();
      if (attackTarget != null) {
         float partialTicks = EpicFightSharedConstants.isPhysicalClient() ? Minecraft.m_91087_().m_91296_() : 1.0F;
         Vec3 target = attackTarget.m_20299_(partialTicks);
         Vec3 vector3d = this.original.m_20299_(partialTicks);
         double d0 = target.f_82479_ - vector3d.f_82479_;
         double d1 = target.f_82480_ - vector3d.f_82480_;
         double d2 = target.f_82481_ - vector3d.f_82481_;
         double d3 = Math.sqrt(d0 * d0 + d2 * d2);
         return Mth.m_14036_(Mth.m_14177_((float)(Mth.m_14136_(d1, d3) * 180.0F / (float)Math.PI)), -30.0F, 30.0F);
      } else {
         return super.getAttackDirectionPitch();
      }
   }

   @Override
   public Faction getFaction() {
      return this.mobFaction;
   }
}
