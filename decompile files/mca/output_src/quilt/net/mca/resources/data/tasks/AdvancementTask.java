package quilt.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.class_161;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import quilt.net.mca.server.world.data.Village;

public class AdvancementTask extends Task {
   private final String identifier;

   public AdvancementTask(String identifier) {
      super("advancement_" + identifier);
      this.identifier = identifier;
   }

   public AdvancementTask(JsonObject json) {
      this(class_3518.method_15265(json, "id"));
   }

   @Override
   public boolean isCompleted(Village village, class_3222 player) {
      class_161 advancement = Objects.requireNonNull(player.method_5682()).method_3851().method_12896(new class_2960(this.identifier));
      return player.method_14236().method_12882(advancement).method_740();
   }
}
