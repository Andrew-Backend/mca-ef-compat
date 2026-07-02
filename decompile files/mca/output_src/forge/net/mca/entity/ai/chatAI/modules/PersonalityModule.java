package forge.net.mca.entity.ai.chatAI.modules;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.chatAI.OpenAIChatAI;
import forge.net.mca.entity.ai.relationship.AgeState;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PersonalityModule {
   public static void apply(List<String> input, VillagerEntityMCA villager, ServerPlayer player) {
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
      } else if (villager.getProfession() != VillagerProfession.f_35585_) {
         input.add("$villager is a " + OpenAIChatAI.translate(villager.getProfession().f_35600_()) + ". ");
      }
   }
}
