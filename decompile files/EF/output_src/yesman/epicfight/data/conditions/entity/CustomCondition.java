package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CustomCondition<T extends LivingEntityPatch<?>> implements Condition<T> {
   private final Function<T, Boolean> predicate;

   public CustomCondition(Function<T, Boolean> predicate) {
      this.predicate = predicate;
   }

   public CustomCondition<T> read(CompoundTag tag) {
      return null;
   }

   @Override
   public CompoundTag serializePredicate() {
      return null;
   }

   public boolean predicate(T target) {
      return this.predicate.apply(target);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      return null;
   }
}
