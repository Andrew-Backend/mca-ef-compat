package forge.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import forge.net.mca.server.world.data.Village;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class ReputationTask extends Task {
   private static final long serialVersionUID = -7232675787774372089L;
   private final int reputation;

   public ReputationTask(int reputation) {
      super("reputation_" + reputation);
      this.reputation = reputation;
   }

   public ReputationTask(JsonObject json) {
      this(GsonHelper.m_13927_(json, "reputation"));
   }

   @Override
   public boolean isCompleted(Village village, ServerPlayer player) {
      return village.getReputation(player) >= this.reputation;
   }

   @Override
   public boolean isRequired() {
      return true;
   }

   @Override
   public MutableComponent getTranslatable() {
      return Component.m_237110_("task.reputation", new Object[]{this.reputation});
   }
}
