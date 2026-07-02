package quilt.net.mca.entity.ai.chatAI;

import java.util.Optional;
import net.minecraft.class_3222;
import quilt.net.mca.entity.VillagerEntityMCA;

public interface ChatAIStrategy {
   Optional<String> answer(class_3222 var1, VillagerEntityMCA var2, String var3);
}
