package fabric.net.mca.util.localization;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_5250;
import net.minecraft.class_5481;

public final class FlowingText {
   private final List<class_5481> lines;
   private final float scale;

   public FlowingText(List<class_5481> lines, float scale) {
      this.lines = lines;
      this.scale = scale;
   }

   public List<class_5481> lines() {
      return this.lines;
   }

   public float scale() {
      return this.scale;
   }

   public static List<class_2561> wrap(class_2561 text, int maxWidth) {
      return class_310.method_1551().field_1772.method_27527().method_27495(text, maxWidth, class_2583.field_24360).stream().map(line -> {
         class_5250 compiled = class_2561.method_43470("");
         line.method_27658((s, t) -> {
            compiled.method_10852(class_2561.method_43470(t).method_10862(s));
            return Optional.empty();
         }, text.method_10866());
         return compiled;
      }).collect(Collectors.toList());
   }

   public interface Factory {
      static FlowingText wrapLines(class_327 renderer, class_2561 text, int maxBlockWidth, int maxBlockHeight) {
         float scale = 1.0F;

         List<class_5481> output;
         do {
            output = renderer.method_1728(text, (int)Math.ceil(maxBlockWidth / scale));
            if (output.size() * 10 * scale <= maxBlockHeight) {
               break;
            }

            scale -= 0.01F;
         } while (scale > 0.08F);

         int maxLines = (int)Math.ceil(maxBlockHeight / (10.0F * scale));
         return new FlowingText(output.stream().limit(maxLines).collect(Collectors.toList()), scale);
      }
   }
}
