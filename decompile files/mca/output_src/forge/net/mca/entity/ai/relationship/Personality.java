package forge.net.mca.entity.ai.relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public enum Personality {
   UNASSIGNED,
   ATHLETIC,
   CONFIDENT,
   FRIENDLY,
   FLIRTY,
   WITTY,
   SHY,
   GLOOMY,
   SENSITIVE,
   GREEDY,
   ODD,
   LAZY,
   GRUMPY,
   PEPPY;

   private static final RandomSource random = RandomSource.m_216327_();

   public static Personality getRandom() {
      List<Personality> validList = new ArrayList<>();

      for (Personality personality : values()) {
         if (personality != UNASSIGNED) {
            validList.add(personality);
         }
      }

      return validList.get(random.m_188503_(validList.size()));
   }

   public float getSpeedModifier() {
      if (this == ATHLETIC) {
         return 1.15F;
      } else {
         return this == LAZY ? 0.8F : 1.0F;
      }
   }

   public Component getName() {
      return Component.m_237115_("personality." + this.name().toLowerCase(Locale.ENGLISH));
   }

   public Component getDescription() {
      return Component.m_237115_("personalityDescription." + this.name().toLowerCase(Locale.ENGLISH));
   }
}
