package quilt.net.mca.entity.ai.chatAI.modules;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.class_161;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.VillagerEntityMCA;

public class PlayerModule {
   private static final Map<class_2960, String> advancements = Map.of(
      new class_2960("story/mine_diamond"),
      "$player found diamonds.",
      new class_2960("story/enter_the_nether"),
      "$player explored the nether.",
      new class_2960("nether/find_fortress"),
      "$player found a nether fortress.",
      new class_2960("story/enchant_item"),
      "$player enchanted items.",
      new class_2960("story/cure_zombie_villager"),
      "$player cured a zombie villager.",
      new class_2960("end/kill_dragon"),
      "$player killed the ender dragon.",
      new class_2960("nether/summon_wither"),
      "$player summoned the wither.",
      new class_2960("adventure/hero_of_the_village"),
      "$player is the hero of the village."
   );

   public static void apply(List<String> input, VillagerEntityMCA villager, class_3222 player) {
      List<String> list = advancements.entrySet().stream().filter(entry -> {
         class_161 advancementx = Objects.requireNonNull(player.method_5682()).method_3851().method_12896(entry.getKey());
         if (advancementx == null) {
            MCA.LOGGER.warn("Advancement {} not found.", entry.getKey());
            return false;
         } else {
            return player.method_14236().method_12882(advancementx).method_740();
         }
      }).map(Entry::getValue).toList();
      if (!list.isEmpty()) {
         input.add("Player has completed the following advancements: ");

         for (String advancement : list) {
            input.add(advancement + " ");
         }
      }
   }
}
