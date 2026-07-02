package fabric.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import fabric.net.mca.server.world.data.Village;
import net.minecraft.class_3222;

public class BlockingTask extends Task {
   private static final long serialVersionUID = -211723796850841823L;

   public BlockingTask(JsonObject json) {
      super(json);
   }

   @Override
   public boolean isCompleted(Village village, class_3222 player) {
      return false;
   }

   @Override
   public boolean isRequired() {
      return true;
   }
}
