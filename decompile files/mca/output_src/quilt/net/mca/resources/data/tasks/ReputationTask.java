package quilt.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_5250;
import quilt.net.mca.server.world.data.Village;

public class ReputationTask extends Task {
   private static final long serialVersionUID = -7232675787774372089L;
   private final int reputation;

   public ReputationTask(int reputation) {
      super("reputation_" + reputation);
      this.reputation = reputation;
   }

   public ReputationTask(JsonObject json) {
      this(class_3518.method_15260(json, "reputation"));
   }

   @Override
   public boolean isCompleted(Village village, class_3222 player) {
      return village.getReputation(player) >= this.reputation;
   }

   @Override
   public boolean isRequired() {
      return true;
   }

   @Override
   public class_5250 getTranslatable() {
      return class_2561.method_43469("task.reputation", new Object[]{this.reputation});
   }
}
