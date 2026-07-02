package fabric.net.mca.entity.ai;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import fabric.net.mca.entity.ai.brain.sensor.ExplodingCreeperSensor;
import fabric.net.mca.entity.ai.brain.sensor.GuardEnemiesSensor;
import fabric.net.mca.entity.ai.brain.sensor.VillagerMCABabiesSensor;
import fabric.net.mca.mixin.MixinActivity;
import fabric.net.mca.mixin.MixinSensorType;
import java.util.function.Supplier;
import net.minecraft.class_2960;
import net.minecraft.class_4148;
import net.minecraft.class_4149;
import net.minecraft.class_4168;
import net.minecraft.class_7924;

public interface ActivityMCA {
   DeferredRegister<class_4168> ACTIVITIES = DeferredRegister.create("mca", class_7924.field_41222);
   DeferredRegister<class_4149<?>> SENSORS = DeferredRegister.create("mca", class_7924.field_41221);
   RegistrySupplier<class_4168> CHORE = activity("chore");
   RegistrySupplier<class_4168> GRIEVE = activity("grieve");
   RegistrySupplier<class_4149<ExplodingCreeperSensor>> EXPLODING_CREEPER = sensor("exploding_creeper", ExplodingCreeperSensor::new);
   RegistrySupplier<class_4149<GuardEnemiesSensor>> GUARD_ENEMIES = sensor("guard_enemies", GuardEnemiesSensor::new);
   RegistrySupplier<class_4149<VillagerMCABabiesSensor>> VILLAGER_BABIES = sensor("villager_babies_mca", VillagerMCABabiesSensor::new);

   static void bootstrap() {
      ACTIVITIES.register();
      SENSORS.register();
   }

   static RegistrySupplier<class_4168> activity(String name) {
      class_2960 id = new class_2960("mca", name);
      return ACTIVITIES.register(id, () -> MixinActivity.init(id.toString()));
   }

   static <T extends class_4148<?>> RegistrySupplier<class_4149<T>> sensor(String name, Supplier<T> factory) {
      class_2960 id = new class_2960("mca", name);
      return SENSORS.register(id, () -> MixinSensorType.init(factory));
   }
}
