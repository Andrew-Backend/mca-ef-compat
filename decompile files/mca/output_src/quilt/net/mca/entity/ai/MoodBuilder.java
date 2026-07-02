package quilt.net.mca.entity.ai;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.class_124;
import net.minecraft.class_2400;
import net.minecraft.class_3414;

public class MoodBuilder {
   private final String name;
   private int soundInterval = 0;
   private RegistrySupplier<class_3414> soundMale;
   private RegistrySupplier<class_3414> soundFemale;
   private int particleInterval = 0;
   private class_2400 particle;
   private class_124 color = class_124.field_1068;
   private String building;

   public MoodBuilder(String name) {
      this.name = name;
   }

   public MoodBuilder sounds(int soundInterval, RegistrySupplier<class_3414> soundMale, RegistrySupplier<class_3414> soundFemale) {
      this.soundInterval = soundInterval;
      this.soundMale = soundMale;
      this.soundFemale = soundFemale;
      return this;
   }

   public MoodBuilder particles(int particleInterval, class_2400 particle) {
      this.particleInterval = particleInterval;
      this.particle = particle;
      return this;
   }

   public MoodBuilder color(class_124 color) {
      this.color = color;
      return this;
   }

   public MoodBuilder building(String building) {
      this.building = building;
      return this;
   }

   public Mood build() {
      return new Mood(this.name, this.soundInterval, this.soundMale, this.soundFemale, this.particleInterval, this.particle, this.color, this.building);
   }
}
