package forge.net.mca.entity.ai.chatAI;

import forge.net.mca.entity.VillagerEntityMCA;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

public interface ChatAIStrategy {
   Optional<String> answer(ServerPlayer var1, VillagerEntityMCA var2, String var3);
}
