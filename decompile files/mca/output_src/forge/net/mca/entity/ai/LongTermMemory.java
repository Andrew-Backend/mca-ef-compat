package forge.net.mca.entity.ai;

import com.google.gson.JsonObject;
import forge.net.mca.entity.VillagerEntityMCA;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class LongTermMemory {
   final HashMap<String, Long> memories = new HashMap<>();
   private final VillagerEntityMCA entity;

   public LongTermMemory(VillagerEntityMCA entity) {
      this.entity = entity;
   }

   public void writeToNbt(CompoundTag nbt) {
      CompoundTag memory = new CompoundTag();

      for (Entry<String, Long> entry : this.memories.entrySet()) {
         memory.m_128356_(entry.getKey(), entry.getValue());
      }

      nbt.m_128365_("longTermMemory", memory);
   }

   public void readFromNbt(CompoundTag nbt) {
      CompoundTag memory = nbt.m_128469_("longTermMemory");
      this.memories.clear();

      for (String key : memory.m_128431_()) {
         this.memories.put(key, memory.m_128454_(key));
      }
   }

   public void remember(String id) {
      this.remember(id, 2147483647L);
   }

   public void remember(String id, long time) {
      long currentTime = this.entity.m_9236_().m_46467_();
      if (this.memories.containsKey(id)) {
         currentTime = Math.max(currentTime, this.memories.get(id));
      }

      this.memories.put(id, currentTime + time);
   }

   public long getMemory(String id) {
      if (this.memories.containsKey(id)) {
         if (this.entity.m_9236_().m_46467_() <= this.memories.get(id)) {
            return this.memories.get(id) - this.entity.m_9236_().m_46467_();
         }

         this.memories.remove(id);
      }

      return 0L;
   }

   public boolean hasMemory(String id) {
      return this.getMemory(id) > 0L;
   }

   public static String parseId(JsonObject json, ServerPlayer player) {
      String id = json.get("id").getAsString();
      if (json.has("var")) {
         switch (json.get("var").getAsString()) {
            case "player":
               assert player != null;
               id = id + "." + player.m_20148_().toString();
         }
      }

      return id;
   }
}
