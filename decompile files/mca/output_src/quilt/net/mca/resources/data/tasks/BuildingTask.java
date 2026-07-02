package quilt.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_5250;
import quilt.net.mca.server.world.data.Village;

public class BuildingTask extends Task {
   private static final long serialVersionUID = -6660910729161211245L;
   private final String type;

   public BuildingTask(String type) {
      super(type);
      this.type = type;
   }

   public BuildingTask(JsonObject json) {
      this(class_3518.method_15265(json, "building"));
   }

   @Override
   public boolean isRequired() {
      return true;
   }

   @Override
   public boolean isCompleted(Village village, class_3222 player) {
      return village.getBuildings().values().stream().anyMatch(b -> b.getType().equals(this.type));
   }

   @Override
   public class_5250 getTranslatable() {
      return class_2561.method_43469("task.build", new Object[]{class_2561.method_43471("buildingType." + this.type)});
   }
}
