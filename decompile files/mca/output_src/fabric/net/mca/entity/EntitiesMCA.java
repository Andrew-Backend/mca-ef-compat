package fabric.net.mca.entity;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import fabric.net.mca.ProfessionsMCA;
import fabric.net.mca.entity.ai.ActivityMCA;
import fabric.net.mca.entity.ai.MemoryModuleTypeMCA;
import fabric.net.mca.entity.ai.SchedulesMCA;
import fabric.net.mca.entity.ai.relationship.Gender;
import java.util.function.Supplier;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1311;
import net.minecraft.class_1642;
import net.minecraft.class_2960;
import net.minecraft.class_7924;
import net.minecraft.class_1299.class_1300;
import net.minecraft.class_5132.class_5133;

public interface EntitiesMCA {
   DeferredRegister<class_1299<?>> ENTITY_TYPES = DeferredRegister.create("mca", class_7924.field_41266);
   RegistrySupplier<class_1299<VillagerEntityMCA>> MALE_VILLAGER = register(
      "male_villager",
      class_1300.method_5903((t, w) -> new VillagerEntityMCA(t, w, Gender.MALE), class_1311.field_17715).method_17687(0.6F, 2.0F),
      VillagerEntityMCA::createVillagerAttributes
   );
   RegistrySupplier<class_1299<VillagerEntityMCA>> FEMALE_VILLAGER = register(
      "female_villager",
      class_1300.method_5903((t, w) -> new VillagerEntityMCA(t, w, Gender.FEMALE), class_1311.field_17715).method_17687(0.6F, 2.0F),
      VillagerEntityMCA::createVillagerAttributes
   );
   RegistrySupplier<class_1299<ZombieVillagerEntityMCA>> MALE_ZOMBIE_VILLAGER = register(
      "male_zombie_villager",
      class_1300.method_5903((t, w) -> new ZombieVillagerEntityMCA(t, w, Gender.MALE), class_1311.field_6302).method_17687(0.6F, 2.0F),
      class_1642::method_26940
   );
   RegistrySupplier<class_1299<ZombieVillagerEntityMCA>> FEMALE_ZOMBIE_VILLAGER = register(
      "female_zombie_villager",
      class_1300.method_5903((t, w) -> new ZombieVillagerEntityMCA(t, w, Gender.FEMALE), class_1311.field_6302).method_17687(0.6F, 2.0F),
      class_1642::method_26940
   );
   RegistrySupplier<class_1299<GrimReaperEntity>> GRIM_REAPER = register(
      "grim_reaper",
      class_1300.method_5903(GrimReaperEntity::new, class_1311.field_6302).method_17687(1.0F, 2.6F).method_19947(),
      GrimReaperEntity::createAttributes
   );
   RegistrySupplier<class_1299<CribEntity>> CRIB = registerNonLiving(
      "crib", class_1300.method_5903((t, w) -> new CribEntity(t, w), class_1311.field_17715).method_17687(1.2F, 1.0F).method_19947()
   );

   static void bootstrap() {
      ENTITY_TYPES.register();
      MemoryModuleTypeMCA.bootstrap();
      ActivityMCA.bootstrap();
      SchedulesMCA.bootstrap();
      ProfessionsMCA.bootstrap();
   }

   static <T extends class_1297> RegistrySupplier<class_1299<T>> registerNonLiving(String name, class_1300<T> builder) {
      class_2960 id = new class_2960("mca", name);
      return ENTITY_TYPES.register(id, () -> builder.method_5905(id.toString()));
   }

   static <T extends class_1309> RegistrySupplier<class_1299<T>> register(String name, class_1300<T> builder, Supplier<class_5133> attributes) {
      class_2960 id = new class_2960("mca", name);
      return ENTITY_TYPES.register(id, () -> {
         class_1299<T> result = builder.method_5905(id.toString());
         EntityAttributeRegistry.register(() -> result, attributes);
         return result;
      });
   }
}
