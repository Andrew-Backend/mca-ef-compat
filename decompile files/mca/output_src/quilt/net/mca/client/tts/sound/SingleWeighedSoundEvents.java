package quilt.net.mca.client.tts.sound;

import net.minecraft.class_1111;
import net.minecraft.class_1146;
import net.minecraft.class_2960;
import net.minecraft.class_5819;
import org.jetbrains.annotations.Nullable;

public class SingleWeighedSoundEvents extends class_1146 {
   private final class_1111 sound;

   public SingleWeighedSoundEvents(class_1111 sound, class_2960 identifier, @Nullable String string) {
      super(identifier, string);
      this.sound = sound;
   }

   public int method_4894() {
      return 1;
   }

   public class_1111 method_4887(class_5819 randomSource) {
      return this.sound;
   }

   public class_1111 getSound() {
      return this.sound;
   }
}
