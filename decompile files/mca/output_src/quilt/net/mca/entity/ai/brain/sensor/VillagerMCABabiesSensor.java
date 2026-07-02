package quilt.net.mca.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import net.minecraft.class_4140;
import net.minecraft.class_4148;
import net.minecraft.class_6670;
import quilt.net.mca.entity.EntitiesMCA;

public class VillagerMCABabiesSensor extends class_4148<class_1309> {
   public Set<class_4140<?>> method_19099() {
      return ImmutableSet.of(class_4140.field_19006);
   }

   protected void method_19101(class_3218 world, class_1309 entity) {
      entity.method_18868().method_18878(class_4140.field_19006, this.getVisibleVillagerBabies(entity));
   }

   private List<class_1309> getVisibleVillagerBabies(class_1309 entities) {
      return this.getVisibleMobs(entities).method_38980(this::isVillagerBaby).toList();
   }

   private boolean isVillagerBaby(class_1309 entity) {
      return (entity.method_5864() == EntitiesMCA.FEMALE_VILLAGER.get() || entity.method_5864() == EntitiesMCA.MALE_VILLAGER.get()) && entity.method_6109();
   }

   private class_6670 getVisibleMobs(class_1309 entity) {
      return (class_6670)entity.method_18868().method_46873(class_4140.field_18442).orElseGet(class_6670::method_38971);
   }
}
