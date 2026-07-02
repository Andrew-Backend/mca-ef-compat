package yesman.epicfight.world.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class CreeperSwellStoppableGoal extends SwellGoal {
   protected Creeper creeperEntity;
   protected MobPatch<?> creeperpatch;

   public CreeperSwellStoppableGoal(MobPatch<?> creeperpatch, Creeper creeper) {
      super(creeper);
      this.creeperEntity = creeper;
      this.creeperpatch = creeperpatch;
   }

   public boolean m_8036_() {
      return super.m_8036_() && !this.creeperpatch.getEntityState().inaction();
   }

   public boolean m_183429_() {
      return false;
   }

   public void m_8041_() {
      super.m_8041_();
      this.creeperEntity.m_32283_(-1);
   }
}
