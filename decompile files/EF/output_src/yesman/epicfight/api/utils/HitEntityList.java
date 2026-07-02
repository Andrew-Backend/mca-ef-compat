package yesman.epicfight.api.utils;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class HitEntityList {
   private final List<Entity> hitEntites;
   private int index = -1;

   public HitEntityList(LivingEntityPatch<?> attacker, List<Entity> entities, HitEntityList.Priority priority) {
      this.hitEntites = priority.sort(attacker, entities);
   }

   public Entity getEntity() {
      return this.hitEntites.get(this.index);
   }

   public boolean next() {
      this.index++;
      return this.hitEntites.size() > this.index;
   }

   public enum Priority {
      DISTANCE((attacker, list) -> {
         DoubleList distanceToAttacker = new DoubleArrayList();
         List<Entity> hitEntites = Lists.newArrayList();

         label24:
         for (Entity entity : list) {
            double distance = attacker.getOriginal().m_20280_(entity);

            int index;
            for (index = 0; index < hitEntites.size(); index++) {
               if (distance < distanceToAttacker.getDouble(index)) {
                  hitEntites.add(index, entity);
                  distanceToAttacker.add(index, distance);
                  continue label24;
               }
            }

            hitEntites.add(index, entity);
            distanceToAttacker.add(index, distance);
         }

         return hitEntites;
      }),
      TARGET((attacker, list) -> {
         List<Entity> hitEntites = Lists.newArrayList();

         for (Entity entity : list) {
            if (entity.m_7306_(attacker.getTarget())) {
               hitEntites.add(entity);
            }
         }

         return hitEntites;
      }),
      HOSTILITY(
         (attacker, list) -> {
            List<Entity> firstTargets = Lists.newArrayList();
            List<Entity> secondTargets = Lists.newArrayList();
            List<Entity> lastTargets = Lists.newArrayList();

            label56:
            for (Entity e : list) {
               if (!attacker.isTargetInvulnerable(e)) {
                  if (attacker.getOriginal().m_21188_() != e && attacker.getTarget() != e) {
                     LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(e, LivingEntityPatch.class);
                     if (entitypatch != null && attacker.getOriginal().m_7306_(entitypatch.getTarget())) {
                        firstTargets.add(e);
                     } else {
                        if (e instanceof Mob mob) {
                           if (attacker.getOriginal().m_7306_(mob.m_5448_())) {
                              firstTargets.add(mob);
                              continue;
                           }

                           GoalSelector targetingAi = mob.f_21346_;

                           for (WrappedGoal goal : targetingAi.m_148105_()) {
                              if (goal.m_26015_() instanceof NearestAttackableTargetGoal<?> targetGoal
                                 && targetGoal.f_26048_.isAssignableFrom(attacker.getOriginal().getClass())
                                 && targetGoal.f_26051_.m_26885_(mob, attacker.getOriginal())) {
                                 secondTargets.add(mob);
                                 continue label56;
                              }
                           }
                        }

                        lastTargets.add(e);
                     }
                  } else {
                     firstTargets.add(e);
                  }
               }
            }

            secondTargets.addAll(lastTargets);
            firstTargets.addAll(secondTargets);
            return firstTargets;
         }
      );

      BiFunction<LivingEntityPatch<?>, List<Entity>, List<Entity>> sortingFunction;

      Priority(BiFunction<LivingEntityPatch<?>, List<Entity>, List<Entity>> sortingFunction) {
         this.sortingFunction = sortingFunction;
      }

      public List<Entity> sort(LivingEntityPatch<?> attacker, List<Entity> entities) {
         return this.sortingFunction.apply(attacker, entities);
      }
   }
}
