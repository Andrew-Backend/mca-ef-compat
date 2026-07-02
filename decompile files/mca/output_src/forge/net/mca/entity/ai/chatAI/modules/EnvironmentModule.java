package forge.net.mca.entity.ai.chatAI.modules;

import forge.net.mca.entity.VillagerEntityMCA;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public class EnvironmentModule {
   public static void apply(List<String> input, VillagerEntityMCA villager, ServerPlayer player) {
      if (player.m_9236_().m_46471_()) {
         input.add("It is raining. ");
      }

      if (player.m_9236_().m_46470_()) {
         input.add("It is thundering. ");
      }

      if (player.m_9236_().m_46462_()) {
         input.add("It is night. ");
      }
   }
}
