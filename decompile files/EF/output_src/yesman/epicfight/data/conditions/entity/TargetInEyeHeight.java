package yesman.epicfight.data.conditions.entity;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetInEyeHeight extends Condition.EntityPatchCondition {
   public TargetInEyeHeight read(CompoundTag tag) {
      return this;
   }

   @Override
   public CompoundTag serializePredicate() {
      return new CompoundTag();
   }

   public boolean predicate(LivingEntityPatch<?> target) {
      double veticalDistance = Math.abs(target.getOriginal().m_20186_() - target.getTarget().m_20186_());
      return veticalDistance < target.getOriginal().m_20192_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      return List.of();
   }
}
