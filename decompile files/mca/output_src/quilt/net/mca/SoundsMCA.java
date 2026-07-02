package quilt.net.mca;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_7924;

public interface SoundsMCA {
   DeferredRegister<class_3414> SOUNDS = DeferredRegister.create("mca", class_7924.field_41225);
   RegistrySupplier<class_3414> REAPER_SCYTHE_OUT = register("reaper.scythe_out");
   RegistrySupplier<class_3414> REAPER_SCYTHE_SWING = register("reaper.scythe_swing");
   RegistrySupplier<class_3414> REAPER_IDLE = register("reaper.idle");
   RegistrySupplier<class_3414> REAPER_DEATH = register("reaper.death");
   RegistrySupplier<class_3414> REAPER_BLOCK = register("reaper.block");
   RegistrySupplier<class_3414> REAPER_SUMMON = register("reaper.summon");
   RegistrySupplier<class_3414> VILLAGER_BABY_LAUGH = register("villager.baby.laugh");
   RegistrySupplier<class_3414> VILLAGER_MALE_SCREAM = register("villager.male.scream");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_SCREAM = register("villager.female.scream");
   RegistrySupplier<class_3414> VILLAGER_MALE_HURT = register("villager.male.hurt");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_HURT = register("villager.female.hurt");
   RegistrySupplier<class_3414> VILLAGER_MALE_LAUGH = register("villager.male.laugh");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_LAUGH = register("villager.female.laugh");
   RegistrySupplier<class_3414> VILLAGER_MALE_CRY = register("villager.male.cry");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_CRY = register("villager.female.cry");
   RegistrySupplier<class_3414> VILLAGER_MALE_ANGRY = register("villager.male.angry");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_ANGRY = register("villager.female.angry");
   RegistrySupplier<class_3414> VILLAGER_MALE_CELEBRATE = register("villager.male.celebrate");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_CELEBRATE = register("villager.female.celebrate");
   RegistrySupplier<class_3414> VILLAGER_MALE_GREET = register("villager.male.greet");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_GREET = register("villager.female.greet");
   RegistrySupplier<class_3414> VILLAGER_MALE_SURPRISE = register("villager.male.surprise");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_SURPRISE = register("villager.female.surprise");
   RegistrySupplier<class_3414> VILLAGER_MALE_YES = register("villager.male.yes");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_YES = register("villager.female.yes");
   RegistrySupplier<class_3414> VILLAGER_MALE_NO = register("villager.male.no");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_NO = register("villager.female.no");
   RegistrySupplier<class_3414> VILLAGER_MALE_COUGH = register("villager.male.cough");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_COUGH = register("villager.female.cough");
   RegistrySupplier<class_3414> VILLAGER_MALE_SNORE = register("villager.male.snore");
   RegistrySupplier<class_3414> VILLAGER_FEMALE_SNORE = register("villager.female.snore");
   RegistrySupplier<class_3414> SIRBEN = register("villager.sirben");
   RegistrySupplier<class_3414> SILENT = register("silent");

   static void bootstrap() {
      SOUNDS.register();
   }

   static RegistrySupplier<class_3414> register(String sound) {
      class_2960 id = new class_2960("mca", sound);
      return SOUNDS.register(id, () -> class_3414.method_47908(id));
   }
}
