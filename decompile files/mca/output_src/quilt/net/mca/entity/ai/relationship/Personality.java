package quilt.net.mca.entity.ai.relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_5819;

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

   private static final class_5819 random = class_5819.method_43047();

   public static Personality getRandom() {
      List<Personality> validList = new ArrayList<>();

      for (Personality personality : values()) {
         if (personality != UNASSIGNED) {
            validList.add(personality);
         }
      }

      return validList.get(random.method_43048(validList.size()));
   }

   public float getSpeedModifier() {
      if (this == ATHLETIC) {
         return 1.15F;
      } else {
         return this == LAZY ? 0.8F : 1.0F;
      }
   }

   public class_2561 getName() {
      return class_2561.method_43471("personality." + this.name().toLowerCase(Locale.ENGLISH));
   }

   public class_2561 getDescription() {
      return class_2561.method_43471("personalityDescription." + this.name().toLowerCase(Locale.ENGLISH));
   }
}
