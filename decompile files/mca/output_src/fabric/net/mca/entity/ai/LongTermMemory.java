package fabric.net.mca.entity.ai;

import com.google.gson.JsonObject;
import fabric.net.mca.entity.VillagerEntityMCA;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.class_2487;
import net.minecraft.class_3222;

public class LongTermMemory {
   final HashMap<String, Long> memories = new HashMap<>();
   private final VillagerEntityMCA entity;

   public LongTermMemory(VillagerEntityMCA entity) {
      this.entity = entity;
   }

   public void writeToNbt(class_2487 nbt) {
      class_2487 memory = new class_2487();

      for (Entry<String, Long> entry : this.memories.entrySet()) {
         memory.method_10544(entry.getKey(), entry.getValue());
      }

      nbt.method_10566("longTermMemory", memory);
   }

   public void readFromNbt(class_2487 nbt) {
      class_2487 memory = nbt.method_10562("longTermMemory");
      this.memories.clear();

      for (String key : memory.method_10541()) {
         this.memories.put(key, memory.method_10537(key));
      }
   }

   public void remember(String id) {
      this.remember(id, 2147483647L);
   }

   public void remember(String id, long time) {
      long currentTime = this.entity.method_37908().method_8510();
      if (this.memories.containsKey(id)) {
         currentTime = Math.max(currentTime, this.memories.get(id));
      }

      this.memories.put(id, currentTime + time);
   }

   public long getMemory(String id) {
      if (this.memories.containsKey(id)) {
         if (this.entity.method_37908().method_8510() <= this.memories.get(id)) {
            return this.memories.get(id) - this.entity.method_37908().method_8510();
         }

         this.memories.remove(id);
      }

      return 0L;
   }

   public boolean hasMemory(String id) {
      return this.getMemory(id) > 0L;
   }

   public static String parseId(JsonObject json, class_3222 player) {
      String id = json.get("id").getAsString();
      if (json.has("var")) {
         switch (json.get("var").getAsString()) {
            case "player":
               assert player != null;
               id = id + "." + player.method_5667().toString();
         }
      }

      return id;
   }
}
