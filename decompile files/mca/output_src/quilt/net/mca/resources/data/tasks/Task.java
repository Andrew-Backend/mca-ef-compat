package quilt.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import java.io.Serializable;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_5250;
import quilt.net.mca.server.world.data.Village;

public abstract class Task implements Serializable {
   private static final long serialVersionUID = 6029812512760976500L;
   private final String id;

   public Task(JsonObject json) {
      this(class_3518.method_15265(json, "id"));
   }

   public Task(String id) {
      this.id = id;
   }

   public abstract boolean isCompleted(Village var1, class_3222 var2);

   public boolean isRequired() {
      return false;
   }

   public class_5250 getTranslatable() {
      return class_2561.method_43471("task." + this.id);
   }

   public String getId() {
      return this.id;
   }
}
