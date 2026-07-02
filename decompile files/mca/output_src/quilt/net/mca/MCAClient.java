package quilt.net.mca;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_310;
import quilt.net.mca.client.gui.SkinLibraryScreen;
import quilt.net.mca.client.tts.SpeechManager;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.network.c2s.ConfigRequest;
import quilt.net.mca.network.c2s.PlayerDataRequest;

public class MCAClient {
   public static VillagerEntityMCA fallbackVillager;
   public static final Map<UUID, VillagerLike<?>> playerData = new HashMap<>();
   public static final Set<UUID> playerDataRequests = new HashSet<>();
   private static final DestinyManager destinyManager = new DestinyManager();

   public static DestinyManager getDestinyManager() {
      return destinyManager;
   }

   public static void onLogin() {
      playerDataRequests.clear();
      NetworkHandler.sendToServer(new ConfigRequest());
   }

   public static Optional<VillagerLike<?>> getPlayerData(UUID uuid) {
      if (isPlayerRendererAllowed()) {
         if (!playerDataRequests.contains(uuid) && class_310.method_1551().method_1562() != null) {
            playerDataRequests.add(uuid);
            NetworkHandler.sendToServer(new PlayerDataRequest(uuid));
         }

         if (playerData.containsKey(uuid)) {
            return Optional.of(playerData.get(uuid));
         }
      }

      return Optional.empty();
   }

   public static boolean useExpandedPersonalityTranslations() {
      boolean isTTSPackActive = class_310.method_1551().method_1478().method_29213().anyMatch(pack -> pack.method_14409().contains("MCAVoices"));
      String language = class_310.method_1551().field_1690.field_1883;
      return !isTTSPackActive && (language.equals("en_us") || language.equals("ru_ru")) && !Config.getInstance().enableOnlineTTS;
   }

   public static boolean useGeneticsRenderer(UUID uuid) {
      return getPlayerData(uuid).filter(f -> f.getPlayerModel() != VillagerLike.PlayerModel.VANILLA).isPresent();
   }

   public static boolean useVillagerRenderer(UUID uuid) {
      return useGeneticsRenderer(uuid) && playerData.get(uuid).getPlayerModel() == VillagerLike.PlayerModel.VILLAGER;
   }

   public static boolean renderArms(UUID uuid, String key) {
      return useVillagerRenderer(uuid)
         && Config.getInstance()
            .playerRendererBlacklist
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals("arms") || entry.getValue().equals(key))
            .noneMatch(entry -> MCA.doesModExist(entry.getKey()));
   }

   public static void tickClient(class_310 client) {
      destinyManager.tick(client);
      if (KeyBindings.SKIN_LIBRARY.method_1436()) {
         class_310.method_1551().method_1507(new SkinLibraryScreen());
      }

      SpeechManager.INSTANCE.tick(client);
   }

   public static void addPlayerData(UUID uuid, VillagerEntityMCA villager) {
      playerData.put(uuid, villager);
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null) {
         client.field_1724.method_18382();
      }
   }

   public static boolean isPlayerRendererAllowed() {
      return Config.getInstance().enableVillagerPlayerModel
         && Config.getInstance()
            .playerRendererBlacklist
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals("all") || entry.getValue().equals("block_player"))
            .noneMatch(entry -> MCA.doesModExist(entry.getKey()));
   }

   public static boolean isVillagerRendererAllowed() {
      return !Config.getInstance().forceVillagerPlayerModel
         && Config.getInstance()
            .playerRendererBlacklist
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals("all") || entry.getValue().equals("block_villager"))
            .noneMatch(entry -> MCA.doesModExist(entry.getKey()));
   }

   public static boolean areShadersAllowed(String key) {
      return Config.getInstance().enablePlayerShaders
         && Config.getInstance()
            .playerRendererBlacklist
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals("shaders") || entry.getValue().equals(key))
            .noneMatch(entry -> MCA.doesModExist(entry.getKey()));
   }

   public static boolean areShadersAllowed() {
      return areShadersAllowed("shaders");
   }
}
