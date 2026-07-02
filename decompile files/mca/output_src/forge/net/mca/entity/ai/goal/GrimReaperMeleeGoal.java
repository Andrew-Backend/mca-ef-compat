package forge.net.mca.entity.ai.goal;

import forge.net.mca.entity.GrimReaperEntity;
import forge.net.mca.entity.ReaperAttackState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GrimReaperMeleeGoal extends Goal {
   private static final int COOLDOWN = 150;
   private final GrimReaperEntity reaper;
   private int blockDuration;
   private int attackDuration;
   private int retreatDuration;
   private int lastAttack = 0;

   public GrimReaperMeleeGoal(GrimReaperEntity reaper) {
      this.reaper = reaper;
   }

   public boolean m_8036_() {
      LivingEntity entityToAttack = this.reaper.m_5448_();
      return entityToAttack != null
         && this.reaper.m_20280_(entityToAttack) <= 144.0
         && this.reaper.f_19797_ > this.lastAttack + 150
         && this.reaper.getAttackState() != ReaperAttackState.REST;
   }

   public boolean m_8045_() {
      return this.retreatDuration > 0 && this.reaper.getAttackState() != ReaperAttackState.REST;
   }

   public boolean m_6767_() {
      return false;
   }

   public void m_8056_() {
      this.blockDuration = 50;
      this.attackDuration = 100;
      this.retreatDuration = 20;
      this.lastAttack = this.reaper.f_19797_;
   }

   public void m_8041_() {
      super.m_8041_();
      this.reaper.setAttackState(ReaperAttackState.IDLE);
   }

   private void curse() {
      LivingEntity entityToAttack = this.reaper.m_5448_();
      if (entityToAttack instanceof Player player && player.m_21254_()) {
         double dX = this.reaper.m_20185_() - player.m_20185_();
         double dZ = this.reaper.m_20189_() - player.m_20189_();
         this.reaper.m_6021_(player.m_20185_() - dX * 2.0, player.m_20186_() + 2.0, this.reaper.m_20189_() - dZ * 2.0);
         if (!this.reaper.m_9236_().f_46443_ && this.reaper.m_217043_().m_188501_() >= 0.2F) {
            int currentItem = player.m_150109_().f_35977_;
            int randomItem = this.reaper.m_217043_().m_188503_(9);
            ItemStack currentItemStack = player.m_150109_().m_8020_(currentItem);
            ItemStack randomItemStack = player.m_150109_().m_8020_(randomItem);
            player.m_150109_().m_6836_(currentItem, randomItemStack);
            player.m_150109_().m_6836_(randomItem, currentItemStack);
            entityToAttack.m_7292_(new MobEffectInstance(MobEffects.f_19610_, 200));
         }
      }
   }

   public void m_8037_() {
      if (this.reaper.getAttackState() != ReaperAttackState.REST) {
         LivingEntity entityToAttack = this.reaper.m_5448_();
         if (entityToAttack == null) {
            this.retreatDuration = 0;
         } else {
            if (this.blockDuration > 0) {
               this.blockDuration--;
               this.reaper.setAttackState(ReaperAttackState.BLOCK);
               if (this.blockDuration == 0) {
                  this.curse();
               }

               if (this.reaper.m_20280_(entityToAttack) <= 4.0) {
                  int rX = this.reaper.m_217043_().m_188503_(10);
                  int rY = this.reaper.m_217043_().m_188503_(6);
                  int rZ = this.reaper.m_217043_().m_188503_(10);
                  this.reaper.m_6021_(this.reaper.m_20185_() - 5.0 + rX, this.reaper.m_20186_() + rY, this.reaper.m_20189_() - 5.0 + rZ);
                  this.reaper.m_21573_().m_26573_();
               }

               Vec3 deltaMovement = this.reaper.m_20184_();
               this.reaper.m_20256_(new Vec3(deltaMovement.f_82479_, 0.05, deltaMovement.f_82481_));
            } else if (this.attackDuration > 0) {
               this.attackDuration--;
               this.reaper.setAttackState(ReaperAttackState.PRE);
               Vec3 dir = entityToAttack.m_20182_().m_82546_(this.reaper.m_20182_()).m_82541_().m_82490_(0.15);
               this.reaper.m_5997_(dir.f_82479_, dir.f_82480_, dir.f_82481_);
               if (this.reaper.m_20280_(entityToAttack) <= 1.0) {
                  this.reaper.m_6674_(InteractionHand.MAIN_HAND);
                  this.attackDuration = 0;
                  entityToAttack.m_6469_(this.reaper.m_9236_().m_269111_().m_269333_(this.reaper), (float)this.reaper.m_21133_(Attributes.f_22281_));
                  entityToAttack.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 200));
               }
            } else {
               this.retreatDuration--;
               this.reaper.setAttackState(ReaperAttackState.POST);
               Vec3 dir = entityToAttack.m_20182_().m_82546_(this.reaper.m_20182_()).m_82541_().m_82490_(-0.1);
               this.reaper.m_20334_(dir.f_82479_, dir.f_82480_, dir.f_82481_);
            }
         }
      }
   }
}
