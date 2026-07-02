package yesman.epicfight.api.animation;

public enum LivingMotions implements LivingMotion {
   ALL,
   INACTION,
   IDLE,
   CONFRONT,
   ANGRY,
   FLOAT,
   WALK,
   RUN,
   SWIM,
   FLY,
   SNEAK,
   KNEEL,
   FALL,
   SIT,
   MOUNT,
   DEATH,
   CHASE,
   SPELLCAST,
   JUMP,
   CELEBRATE,
   LANDING_RECOVERY,
   CREATIVE_FLY,
   CREATIVE_IDLE,
   SLEEP,
   DIGGING,
   ADMIRE,
   CLIMB,
   DRINK,
   EAT,
   NONE,
   AIM,
   BLOCK,
   BLOCK_SHIELD,
   RELOAD,
   SHOT,
   SPECTATE;

   final int id = LivingMotion.ENUM_MANAGER.assign(this);

   @Override
   public int universalOrdinal() {
      return this.id;
   }
}
