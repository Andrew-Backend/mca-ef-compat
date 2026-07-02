package forge.net.mca.entity.ai;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import forge.net.mca.entity.ai.brain.sensor.ExplodingCreeperSensor;
import forge.net.mca.entity.ai.brain.sensor.GuardEnemiesSensor;
import forge.net.mca.entity.ai.brain.sensor.VillagerMCABabiesSensor;
import forge.net.mca.mixin.MixinActivity;
import forge.net.mca.mixin.MixinSensorType;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public interface ActivityMCA {
   DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create("mca", Registries.f_257025_);
   DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create("mca", Registries.f_256937_);
   RegistrySupplier<Activity> CHORE = activity("chore");
   RegistrySupplier<Activity> GRIEVE = activity("grieve");
   RegistrySupplier<SensorType<ExplodingCreeperSensor>> EXPLODING_CREEPER = sensor("exploding_creeper", ExplodingCreeperSensor::new);
   RegistrySupplier<SensorType<GuardEnemiesSensor>> GUARD_ENEMIES = sensor("guard_enemies", GuardEnemiesSensor::new);
   RegistrySupplier<SensorType<VillagerMCABabiesSensor>> VILLAGER_BABIES = sensor("villager_babies_mca", VillagerMCABabiesSensor::new);

   static void bootstrap() {
      ACTIVITIES.register();
      SENSORS.register();
   }

   static RegistrySupplier<Activity> activity(String name) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return ACTIVITIES.register(id, () -> MixinActivity.init(id.toString()));
   }

   static <T extends Sensor<?>> RegistrySupplier<SensorType<T>> sensor(String name, Supplier<T> factory) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return SENSORS.register(id, () -> MixinSensorType.init(factory));
   }
}
