package forge.net.mca.entity.interaction.gifts;

import java.util.Locale;

public enum Response {
   FAIL,
   GOOD,
   BETTER,
   BEST;

   public String getDefaultDialogue() {
      return "gift." + this.name().toLowerCase(Locale.ENGLISH);
   }
}
