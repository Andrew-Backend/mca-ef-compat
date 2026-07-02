package fabric.net.mca.entity.ai;

import dev.architectury.registry.registries.RegistrySupplier;
import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_2400;
import net.minecraft.class_2561;
import net.minecraft.class_3414;

public class Mood {
   private final String name;
   private final int soundInterval;
   private final RegistrySupplier<class_3414> soundMale;
   private final RegistrySupplier<class_3414> soundFemale;
   private final int particleInterval;
   private final class_2400 particle;
   private final class_124 color;
   private final String building;

   Mood(
      String name,
      int soundInterval,
      RegistrySupplier<class_3414> soundMale,
      RegistrySupplier<class_3414> soundFemale,
      int particleInterval,
      class_2400 particle,
      class_124 color,
      String building
   ) {
      this.name = name;
      this.soundInterval = soundInterval;
      this.soundMale = soundMale;
      this.soundFemale = soundFemale;
      this.particleInterval = particleInterval;
      this.particle = particle;
      this.color = color;
      this.building = building;
   }

   public class_2561 getText() {
      return class_2561.method_43471("mood." + this.name.toLowerCase(Locale.ENGLISH));
   }

   public String getName() {
      return this.name;
   }

   public int getSoundInterval() {
      return this.soundInterval;
   }

   public class_3414 getSoundMale() {
      return (class_3414)this.soundMale.get();
   }

   public class_3414 getSoundFemale() {
      return (class_3414)this.soundFemale.get();
   }

   public int getParticleInterval() {
      return this.particleInterval;
   }

   public class_2400 getParticle() {
      return this.particle;
   }

   public class_124 getColor() {
      return this.color;
   }

   public String getBuilding() {
      return this.building;
   }
}
