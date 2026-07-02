package fabric.net.mca.entity.ai.chatAI;

import fabric.net.mca.entity.VillagerEntityMCA;
import java.util.Optional;
import net.minecraft.class_3222;

public interface ChatAIStrategy {
   Optional<String> answer(class_3222 var1, VillagerEntityMCA var2, String var3);
}
