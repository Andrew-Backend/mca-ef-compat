package quilt.net.mca.resources;

import java.util.Locale;

public enum Rank {
   OUTLAW,
   PEASANT,
   MERCHANT,
   NOBLE,
   MAYOR,
   MONARCH;

   private static final Rank[] VALUES = values();

   public Rank promote() {
      return this.ordinal() + 1 < VALUES.length ? VALUES[this.ordinal() + 1] : MONARCH;
   }

   public Rank degrade() {
      return this.ordinal() - 1 >= 0 ? VALUES[this.ordinal() - 1] : null;
   }

   public static Rank fromName(String name) {
      try {
         return valueOf(name.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var2) {
         return PEASANT;
      }
   }

   public boolean isAtLeast(Rank r) {
      return this.ordinal() >= r.ordinal();
   }

   public String getTranslationKey() {
      return "gui.village.rank." + this.name().toLowerCase(Locale.ENGLISH);
   }
}
