package fabric.net.mca.entity.ai.chatAI;

import fabric.net.mca.entity.VillagerEntityMCA;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import net.minecraft.class_3222;

public class TriggerCommandInfo {
   public String command;
   public String description;
   public BiPredicate<class_3222, VillagerEntityMCA> isActive;
   public BiConsumer<class_3222, VillagerEntityMCA> call;

   public TriggerCommandInfo(
      String command, String description, BiConsumer<class_3222, VillagerEntityMCA> call, BiPredicate<class_3222, VillagerEntityMCA> isActive
   ) {
      this.command = command;
      this.description = description;
      this.call = call;
      this.isActive = isActive;
   }

   public TriggerCommandInfo(String command, String description, BiConsumer<class_3222, VillagerEntityMCA> call) {
      this(command, description, call, (p, v) -> true);
   }
}
