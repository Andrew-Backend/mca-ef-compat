package forge.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import forge.net.mca.server.world.data.Village;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class BuildingTask extends Task {
   private static final long serialVersionUID = -6660910729161211245L;
   private final String type;

   public BuildingTask(String type) {
      super(type);
      this.type = type;
   }

   public BuildingTask(JsonObject json) {
      this(GsonHelper.m_13906_(json, "building"));
   }

   @Override
   public boolean isRequired() {
      return true;
   }

   @Override
   public boolean isCompleted(Village village, ServerPlayer player) {
      return village.getBuildings().values().stream().anyMatch(b -> b.getType().equals(this.type));
   }

   @Override
   public MutableComponent getTranslatable() {
      return Component.m_237110_("task.build", new Object[]{Component.m_237115_("buildingType." + this.type)});
   }
}
