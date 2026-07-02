package forge.net.mca.client.tts.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class SingleWeighedSoundEvents extends WeighedSoundEvents {
   private final Sound sound;

   public SingleWeighedSoundEvents(Sound sound, ResourceLocation identifier, @Nullable String string) {
      super(identifier, string);
      this.sound = sound;
   }

   public int m_7789_() {
      return 1;
   }

   public Sound m_213718_(RandomSource randomSource) {
      return this.sound;
   }

   public Sound getSound() {
      return this.sound;
   }
}
