package forge.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import forge.net.mca.server.world.data.Village;
import java.io.Serializable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public abstract class Task implements Serializable {
   private static final long serialVersionUID = 6029812512760976500L;
   private final String id;

   public Task(JsonObject json) {
      this(GsonHelper.m_13906_(json, "id"));
   }

   public Task(String id) {
      this.id = id;
   }

   public abstract boolean isCompleted(Village var1, ServerPlayer var2);

   public boolean isRequired() {
      return false;
   }

   public MutableComponent getTranslatable() {
      return Component.m_237115_("task." + this.id);
   }

   public String getId() {
      return this.id;
   }
}
