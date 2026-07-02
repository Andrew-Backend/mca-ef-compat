package forge.net.mca.entity;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.entity.ai.ActivityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.entity.ai.SchedulesMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.monster.Zombie;

public interface EntitiesMCA {
   DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create("mca", Registries.f_256939_);
   RegistrySupplier<EntityType<VillagerEntityMCA>> MALE_VILLAGER = register(
      "male_villager",
      Builder.m_20704_((t, w) -> new VillagerEntityMCA(t, w, Gender.MALE), MobCategory.MISC).m_20699_(0.6F, 2.0F),
      VillagerEntityMCA::createVillagerAttributes
   );
   RegistrySupplier<EntityType<VillagerEntityMCA>> FEMALE_VILLAGER = register(
      "female_villager",
      Builder.m_20704_((t, w) -> new VillagerEntityMCA(t, w, Gender.FEMALE), MobCategory.MISC).m_20699_(0.6F, 2.0F),
      VillagerEntityMCA::createVillagerAttributes
   );
   RegistrySupplier<EntityType<ZombieVillagerEntityMCA>> MALE_ZOMBIE_VILLAGER = register(
      "male_zombie_villager",
      Builder.m_20704_((t, w) -> new ZombieVillagerEntityMCA(t, w, Gender.MALE), MobCategory.MONSTER).m_20699_(0.6F, 2.0F),
      Zombie::m_34328_
   );
   RegistrySupplier<EntityType<ZombieVillagerEntityMCA>> FEMALE_ZOMBIE_VILLAGER = register(
      "female_zombie_villager",
      Builder.m_20704_((t, w) -> new ZombieVillagerEntityMCA(t, w, Gender.FEMALE), MobCategory.MONSTER).m_20699_(0.6F, 2.0F),
      Zombie::m_34328_
   );
   RegistrySupplier<EntityType<GrimReaperEntity>> GRIM_REAPER = register(
      "grim_reaper", Builder.m_20704_(GrimReaperEntity::new, MobCategory.MONSTER).m_20699_(1.0F, 2.6F).m_20719_(), GrimReaperEntity::createAttributes
   );
   RegistrySupplier<EntityType<CribEntity>> CRIB = registerNonLiving(
      "crib", Builder.m_20704_((t, w) -> new CribEntity(t, w), MobCategory.MISC).m_20699_(1.2F, 1.0F).m_20719_()
   );

   static void bootstrap() {
      ENTITY_TYPES.register();
      MemoryModuleTypeMCA.bootstrap();
      ActivityMCA.bootstrap();
      SchedulesMCA.bootstrap();
      ProfessionsMCA.bootstrap();
   }

   static <T extends Entity> RegistrySupplier<EntityType<T>> registerNonLiving(String name, Builder<T> builder) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return ENTITY_TYPES.register(id, () -> builder.m_20712_(id.toString()));
   }

   static <T extends LivingEntity> RegistrySupplier<EntityType<T>> register(
      String name, Builder<T> builder, Supplier<net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder> attributes
   ) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return ENTITY_TYPES.register(id, () -> {
         EntityType<T> result = builder.m_20712_(id.toString());
         EntityAttributeRegistry.register(() -> result, attributes);
         return result;
      });
   }
}
