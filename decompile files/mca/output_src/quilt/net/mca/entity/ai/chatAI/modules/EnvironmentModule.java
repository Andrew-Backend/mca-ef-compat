package quilt.net.mca.entity.ai.chatAI.modules;

import java.util.List;
import net.minecraft.class_3222;
import quilt.net.mca.entity.VillagerEntityMCA;

public class EnvironmentModule {
   public static void apply(List<String> input, VillagerEntityMCA villager, class_3222 player) {
      if (player.method_37908().method_8419()) {
         input.add("It is raining. ");
      }

      if (player.method_37908().method_8546()) {
         input.add("It is thundering. ");
      }

      if (player.method_37908().method_23886()) {
         input.add("It is night. ");
      }
   }
}
