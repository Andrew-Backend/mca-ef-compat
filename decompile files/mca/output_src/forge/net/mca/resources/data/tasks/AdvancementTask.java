package forge.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import forge.net.mca.server.world.data.Village;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class AdvancementTask extends Task {
   private final String identifier;

   public AdvancementTask(String identifier) {
      super("advancement_" + identifier);
      this.identifier = identifier;
   }

   public AdvancementTask(JsonObject json) {
      this(GsonHelper.m_13906_(json, "id"));
   }

   @Override
   public boolean isCompleted(Village village, ServerPlayer player) {
      Advancement advancement = Objects.requireNonNull(player.m_20194_()).m_129889_().m_136041_(new ResourceLocation(this.identifier));
      return player.m_8960_().m_135996_(advancement).m_8193_();
   }
}
