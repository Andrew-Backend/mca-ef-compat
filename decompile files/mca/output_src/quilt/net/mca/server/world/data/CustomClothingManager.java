package quilt.net.mca.server.world.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import net.minecraft.class_18;
import net.minecraft.class_2487;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.CustomSkinsChangedMessage;
import quilt.net.mca.resources.data.skin.Clothing;
import quilt.net.mca.resources.data.skin.Hair;
import quilt.net.mca.resources.data.skin.SkinListEntry;

public class CustomClothingManager {
   static final CustomClothingManager.Storage<Clothing> CLOTHING_DUMMY = new CustomClothingManager.Storage<>();
   static final CustomClothingManager.Storage<Hair> HAIR_DUMMY = new CustomClothingManager.Storage<>();

   public static CustomClothingManager.Storage<Clothing> getClothing() {
      Optional<MinecraftServer> server = MCA.getServer();
      return server.isPresent()
         ? (CustomClothingManager.Storage)server.get()
            .method_30002()
            .method_17983()
            .method_17924(nbt -> new CustomClothingManager.Storage<>(nbt, Clothing::new), CustomClothingManager.Storage::new, "immersive_library_clothing")
         : CLOTHING_DUMMY;
   }

   public static CustomClothingManager.Storage<Hair> getHair() {
      Optional<MinecraftServer> server = MCA.getServer();
      return server.isPresent()
         ? (CustomClothingManager.Storage)server.get()
            .method_30002()
            .method_17983()
            .method_17924(nbt -> new CustomClothingManager.Storage<>(nbt, Hair::new), CustomClothingManager.Storage::new, "immersive_library_hair")
         : HAIR_DUMMY;
   }

   public static class Storage<T extends SkinListEntry> extends class_18 {
      final Map<String, T> entries = new HashMap<>();

      public Storage() {
      }

      public Storage(class_2487 nbt, BiFunction<String, JsonObject, T> entryFromNbt) {
         Gson gson = new Gson();

         for (String identifier : nbt.method_10541()) {
            this.entries.put(identifier, entryFromNbt.apply(identifier, (JsonObject)gson.fromJson(nbt.method_10558(identifier), JsonObject.class)));
         }
      }

      public class_2487 method_75(class_2487 nbt) {
         class_2487 c = new class_2487();

         for (Entry<String, T> entry : this.entries.entrySet()) {
            c.method_10582(entry.getKey(), entry.getValue().toJson().toString());
         }

         return c;
      }

      public Map<String, T> getEntries() {
         return this.entries;
      }

      public void addEntry(String id, T entry) {
         this.entries.put(id, entry);
         this.method_80();
      }

      public void removeEntry(String id) {
         this.entries.remove(id);
         this.method_80();
      }

      public void method_80() {
         super.method_80();
         MCA.getServer().ifPresent(s -> {
            for (class_3222 player : s.method_3760().method_14571()) {
               NetworkHandler.sendToPlayer(new CustomSkinsChangedMessage(), player);
            }
         });
      }
   }
}
