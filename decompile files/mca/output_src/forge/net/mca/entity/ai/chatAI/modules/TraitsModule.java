package forge.net.mca.entity.ai.chatAI.modules;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.chatAI.OpenAIChatAI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;

public class TraitsModule {
   private static final Map<String, String> traitDescription = new HashMap<String, String>() {
      {
         this.put("lactose_intolerance", "$villager is intolerant to lactose.");
         this.put("coeliac_disease", "$villager has coeliac disease.");
         this.put("diabetes", "$villager has diabetes.");
         this.put("sirben", "$villager is unfortunately born as a Sirben.");
         this.put("dwarfism", "$villager has dwarfism.");
         this.put("albinism", "$villager is an albino.");
         this.put("heterochromia", "$villager has heterochromia.");
         this.put("color_blind", "$villager is color-blind.");
         this.put("vegetarian", "$villager is a vegetarian.");
         this.put("bisexual", "$villager is bisexual.");
         this.put("homosexual", "$villager is homosexual.");
         this.put("asexual", "$villager is asexual.");
         this.put("left_handed", "$villager is left handed.");
         this.put("electrified", "$villager has been struck by lightning.");
         this.put("rainbow", "$villager has colorful hair.");
      }
   };

   public static void apply(List<String> input, VillagerEntityMCA villager, ServerPlayer player) {
      for (Traits.Trait trait : villager.getTraits().getTraits()) {
         input.add(traitDescription.getOrDefault(trait.id(), "$villager has " + OpenAIChatAI.translate(trait.id()) + ". "));
      }
   }
}
