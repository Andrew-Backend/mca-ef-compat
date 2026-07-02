package yesman.epicfight.api.animation.types;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;

public class EntityState {
   public static final EntityState DEFAULT_STATE = new EntityState(new TypeFlexibleHashMap<>(true));
   public static final EntityState.StateFactor<Boolean> TURNING_LOCKED = new EntityState.StateFactor<>("turningLocked", false);
   public static final EntityState.StateFactor<Boolean> MOVEMENT_LOCKED = new EntityState.StateFactor<>("movementLocked", false);
   public static final EntityState.StateFactor<Boolean> ATTACKING = new EntityState.StateFactor<>("attacking", false);
   public static final EntityState.StateFactor<Boolean> CAN_BASIC_ATTACK = new EntityState.StateFactor<>("canBasicAttack", true);
   public static final EntityState.StateFactor<Boolean> CAN_SKILL_EXECUTION = new EntityState.StateFactor<>("canExecuteSkill", true);
   public static final EntityState.StateFactor<Boolean> CAN_USE_ITEM = new EntityState.StateFactor<>("canUseItem", true);
   public static final EntityState.StateFactor<Boolean> CAN_SWITCH_HAND_ITEM = new EntityState.StateFactor<>("canSwitchHandItem", true);
   public static final EntityState.StateFactor<Boolean> INACTION = new EntityState.StateFactor<>("takingAction", false);
   public static final EntityState.StateFactor<Boolean> KNOCKDOWN = new EntityState.StateFactor<>("knockdown", false);
   public static final EntityState.StateFactor<Boolean> LOCKON_ROTATE = new EntityState.StateFactor<>("lockonRotate", false);
   public static final EntityState.StateFactor<Boolean> UPDATE_LIVING_MOTION = new EntityState.StateFactor<>("updateLivingMotion", true);
   public static final EntityState.StateFactor<Integer> HURT_LEVEL = new EntityState.StateFactor<>("hurtLevel", 0);
   public static final EntityState.StateFactor<Integer> PHASE_LEVEL = new EntityState.StateFactor<>("phaseLevel", 0);
   public static final EntityState.StateFactor<Function<DamageSource, AttackResult.ResultType>> ATTACK_RESULT = new EntityState.StateFactor<>(
      "attackResultModifier", damagesource -> AttackResult.ResultType.SUCCESS
   );
   public static final EntityState.StateFactor<Consumer<ProjectileImpactEvent>> PROJECTILE_IMPACT_RESULT = new EntityState.StateFactor<>(
      "projectileImpactResult", event -> {}
   );
   private final TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap;

   public EntityState(TypeFlexibleHashMap<EntityState.StateFactor<?>> states) {
      this.stateMap = states;
   }

   public <T> void setState(EntityState.StateFactor<T> stateFactor, T val) {
      this.stateMap.put(stateFactor, val);
   }

   public <T> T getState(EntityState.StateFactor<T> stateFactor) {
      return this.stateMap.getOrDefault(stateFactor);
   }

   public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStateMap() {
      return this.stateMap;
   }

   public boolean turningLocked() {
      return this.getState(TURNING_LOCKED);
   }

   public boolean movementLocked() {
      return this.getState(MOVEMENT_LOCKED);
   }

   public boolean attacking() {
      return this.getState(ATTACKING);
   }

   public AttackResult.ResultType attackResult(DamageSource damagesource) {
      return this.getState(ATTACK_RESULT).apply(damagesource);
   }

   public void setProjectileImpactResult(ProjectileImpactEvent event) {
      this.getState(PROJECTILE_IMPACT_RESULT).accept(event);
   }

   public boolean canBasicAttack() {
      return this.getState(CAN_BASIC_ATTACK);
   }

   public boolean canUseSkill() {
      return this.getState(CAN_SKILL_EXECUTION);
   }

   public boolean canUseItem() {
      return this.canUseSkill() && this.getState(CAN_USE_ITEM);
   }

   public boolean canSwitchHoldingItem() {
      return !this.inaction() && this.getState(CAN_SWITCH_HAND_ITEM);
   }

   public boolean inaction() {
      return this.getState(INACTION);
   }

   public boolean updateLivingMotion() {
      return this.getState(UPDATE_LIVING_MOTION);
   }

   public boolean hurt() {
      return this.getState(HURT_LEVEL) > 0;
   }

   public int hurtLevel() {
      return this.getState(HURT_LEVEL);
   }

   public boolean knockDown() {
      return this.getState(KNOCKDOWN);
   }

   public boolean lockonRotate() {
      return this.getState(LOCKON_ROTATE);
   }

   public int getLevel() {
      return this.getState(PHASE_LEVEL);
   }

   @Override
   public String toString() {
      return this.stateMap.toString();
   }

   public static class StateFactor<T> implements TypeFlexibleHashMap.TypeKey<T> {
      private final String name;
      private final T defaultValue;

      public StateFactor(String name, T defaultValue) {
         this.name = name;
         this.defaultValue = defaultValue;
      }

      @Override
      public String toString() {
         return this.name;
      }

      @Override
      public T defaultValue() {
         return this.defaultValue;
      }
   }
}
