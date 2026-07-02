package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.world.entity.Mob;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class GlobalMobPatch extends HurtableEntityPatch<Mob> {
   private int remainStunTime;

   @Override
   protected void updateStunTime() {
      super.updateStunTime();
      this.remainStunTime--;
   }

   @Override
   public boolean applyStun(StunType stunType, float stunTime) {
      this.original.f_20900_ = 0.0F;
      this.original.f_20901_ = 0.0F;
      this.original.f_20902_ = 0.0F;
      this.original.m_20334_(0.0, 0.0, 0.0);
      this.cancelKnockback = true;
      this.remainStunTime = (int)(stunTime * 20.0F);
      return true;
   }

   @Override
   public boolean isStunned() {
      return this.remainStunTime > 0 && EpicFightGameRules.GLOBAL_STUN.getRuleValue(this.original.m_9236_());
   }
}
