package forge.net.mca.entity.ai;

import dev.architectury.registry.registries.RegistrySupplier;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class Mood {
   private final String name;
   private final int soundInterval;
   private final RegistrySupplier<SoundEvent> soundMale;
   private final RegistrySupplier<SoundEvent> soundFemale;
   private final int particleInterval;
   private final SimpleParticleType particle;
   private final ChatFormatting color;
   private final String building;

   Mood(
      String name,
      int soundInterval,
      RegistrySupplier<SoundEvent> soundMale,
      RegistrySupplier<SoundEvent> soundFemale,
      int particleInterval,
      SimpleParticleType particle,
      ChatFormatting color,
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

   public Component getText() {
      return Component.m_237115_("mood." + this.name.toLowerCase(Locale.ENGLISH));
   }

   public String getName() {
      return this.name;
   }

   public int getSoundInterval() {
      return this.soundInterval;
   }

   public SoundEvent getSoundMale() {
      return (SoundEvent)this.soundMale.get();
   }

   public SoundEvent getSoundFemale() {
      return (SoundEvent)this.soundFemale.get();
   }

   public int getParticleInterval() {
      return this.particleInterval;
   }

   public SimpleParticleType getParticle() {
      return this.particle;
   }

   public ChatFormatting getColor() {
      return this.color;
   }

   public String getBuilding() {
      return this.building;
   }
}
