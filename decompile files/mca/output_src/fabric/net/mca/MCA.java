package fabric.net.mca;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_2960;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MCA {
   public static final String MOD_ID = "mca";
   public static final Logger LOGGER = LogManager.getLogger();
   private static final Map<String, Boolean> MOD_CACHE = new HashMap<>();
   public static final ExecutorService executorService = Executors.newSingleThreadExecutor();
   public static Map<String, String> translations = new HashMap<>();
   public static String language;
   private static MinecraftServer server;

   public static class_2960 locate(String id) {
      return new class_2960("mca", id);
   }

   public static boolean isBlankString(String string) {
      return string == null || string.trim().isEmpty();
   }

   public static boolean doesModExist(String modId) {
      if (!MOD_CACHE.containsKey(modId)) {
         Optional<Mod> modData;
         try {
            modData = Optional.of(Platform.getMod(modId));
         } catch (Exception ignored) {
            modData = Optional.empty();
         }

         MOD_CACHE.put(modId, modData.isPresent());
      }

      return MOD_CACHE.get(modId);
   }

   public static void setServer(MinecraftServer server) {
      MCA.server = server;
   }

   public static Optional<MinecraftServer> getServer() {
      return Optional.ofNullable(server);
   }
}
