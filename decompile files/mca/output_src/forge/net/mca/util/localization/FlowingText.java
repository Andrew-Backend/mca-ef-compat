package forge.net.mca.util.localization;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class FlowingText {
   private final List<FormattedCharSequence> lines;
   private final float scale;

   public FlowingText(List<FormattedCharSequence> lines, float scale) {
      this.lines = lines;
      this.scale = scale;
   }

   public List<FormattedCharSequence> lines() {
      return this.lines;
   }

   public float scale() {
      return this.scale;
   }

   public static List<Component> wrap(Component text, int maxWidth) {
      return Minecraft.m_91087_().f_91062_.m_92865_().m_92414_(text, maxWidth, Style.f_131099_).stream().map(line -> {
         MutableComponent compiled = Component.m_237113_("");
         line.m_7451_((s, t) -> {
            compiled.m_7220_(Component.m_237113_(t).m_6270_(s));
            return Optional.empty();
         }, text.m_7383_());
         return compiled;
      }).collect(Collectors.toList());
   }

   public interface Factory {
      static FlowingText wrapLines(Font renderer, Component text, int maxBlockWidth, int maxBlockHeight) {
         float scale = 1.0F;

         List<FormattedCharSequence> output;
         do {
            output = renderer.m_92923_(text, (int)Math.ceil(maxBlockWidth / scale));
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
