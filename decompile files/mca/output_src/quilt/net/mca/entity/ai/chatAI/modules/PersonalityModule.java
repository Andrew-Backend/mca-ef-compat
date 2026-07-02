package quilt.net.mca.entity.ai.chatAI.modules;

import java.util.List;
import net.minecraft.class_3222;
import net.minecraft.class_3852;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.chatAI.OpenAIChatAI;
import quilt.net.mca.entity.ai.relationship.AgeState;

public class PersonalityModule {
   public static void apply(List<String> input, VillagerEntityMCA villager, class_3222 player) {
      input.add(
         "This is a conversation with a "
            + OpenAIChatAI.translate(villager.getGenetics().getGender().name())
            + " Minecraft villager named $villager and the Player named $player. "
      );
      input.add(
         "$villager is "
            + OpenAIChatAI.translate(villager.getVillagerBrain().getPersonality().name())
            + " and "
            + OpenAIChatAI.translate(villager.getVillagerBrain().getMood().getName())
            + ". "
      );
      if (villager.getAgeState() == AgeState.TODDLER) {
         input.add("$villager is a toddler. ");
      }

      if (villager.getAgeState() == AgeState.CHILD) {
         input.add("$villager is a child. ");
      }

      if (villager.getAgeState() == AgeState.TEEN) {
         input.add("$villager is a teen. ");
      } else if (villager.getProfession() != class_3852.field_17051) {
         input.add("$villager is a " + OpenAIChatAI.translate(villager.getProfession().comp_818()) + ". ");
      }
   }
}
