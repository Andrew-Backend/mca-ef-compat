package forge.net.mca.server.world.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.CustomSkinsChangedMessage;
import forge.net.mca.resources.data.skin.Clothing;
import forge.net.mca.resources.data.skin.Hair;
import forge.net.mca.resources.data.skin.SkinListEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

public class CustomClothingManager {
   static final CustomClothingManager.Storage<Clothing> CLOTHING_DUMMY = new CustomClothingManager.Storage<>();
   static final CustomClothingManager.Storage<Hair> HAIR_DUMMY = new CustomClothingManager.Storage<>();

   public static CustomClothingManager.Storage<Clothing> getClothing() {
      Optional<MinecraftServer> server = MCA.getServer();
      return server.isPresent()
         ? (CustomClothingManager.Storage)server.get()
            .m_129783_()
            .m_8895_()
            .m_164861_(nbt -> new CustomClothingManager.Storage<>(nbt, Clothing::new), CustomClothingManager.Storage::new, "immersive_library_clothing")
         : CLOTHING_DUMMY;
   }

   public static CustomClothingManager.Storage<Hair> getHair() {
      Optional<MinecraftServer> server = MCA.getServer();
      return server.isPresent()
         ? (CustomClothingManager.Storage)server.get()
            .m_129783_()
            .m_8895_()
            .m_164861_(nbt -> new CustomClothingManager.Storage<>(nbt, Hair::new), CustomClothingManager.Storage::new, "immersive_library_hair")
         : HAIR_DUMMY;
   }

   public static class Storage<T extends SkinListEntry> extends SavedData {
      final Map<String, T> entries = new HashMap<>();

      public Storage() {
      }

      public Storage(CompoundTag nbt, BiFunction<String, JsonObject, T> entryFromNbt) {
         Gson gson = new Gson();

         for (String identifier : nbt.m_128431_()) {
            this.entries.put(identifier, entryFromNbt.apply(identifier, (JsonObject)gson.fromJson(nbt.m_128461_(identifier), JsonObject.class)));
         }
      }

      public CompoundTag m_7176_(CompoundTag nbt) {
         CompoundTag c = new CompoundTag();

         for (Entry<String, T> entry : this.entries.entrySet()) {
            c.m_128359_(entry.getKey(), entry.getValue().toJson().toString());
         }

         return c;
      }

      public Map<String, T> getEntries() {
         return this.entries;
      }

      public void addEntry(String id, T entry) {
         this.entries.put(id, entry);
         this.m_77762_();
      }

      public void removeEntry(String id) {
         this.entries.remove(id);
         this.m_77762_();
      }

      public void m_77762_() {
         super.m_77762_();
         MCA.getServer().ifPresent(s -> {
            for (ServerPlayer player : s.m_6846_().m_11314_()) {
               NetworkHandler.sendToPlayer(new CustomSkinsChangedMessage(), player);
            }
         });
      }
   }
}
