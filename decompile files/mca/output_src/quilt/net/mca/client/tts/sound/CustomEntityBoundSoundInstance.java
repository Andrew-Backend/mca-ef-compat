package quilt.net.mca.client.tts.sound;

import net.minecraft.class_1106;
import net.minecraft.class_1144;
import net.minecraft.class_1146;
import net.minecraft.class_1297;
import net.minecraft.class_3414;
import net.minecraft.class_3419;

public class CustomEntityBoundSoundInstance extends class_1106 {
   private final SingleWeighedSoundEvents weighedSoundEvents;

   public CustomEntityBoundSoundInstance(
      SingleWeighedSoundEvents weighedSoundEvents, class_3414 soundEvent, class_3419 soundSource, float volume, float pitch, class_1297 entity, long l
   ) {
      super(soundEvent, soundSource, volume, pitch, entity, l);
      this.weighedSoundEvents = weighedSoundEvents;
   }

   public class_1146 method_4783(class_1144 soundManager) {
      this.field_5444 = this.weighedSoundEvents.getSound();
      return this.weighedSoundEvents;
   }
}
