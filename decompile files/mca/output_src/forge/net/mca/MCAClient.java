package forge.net.mca;

import forge.net.mca.client.gui.SkinLibraryScreen;
import forge.net.mca.client.tts.SpeechManager;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.network.c2s.ConfigRequest;
import forge.net.mca.network.c2s.PlayerDataRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;

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
         if (!playerDataRequests.contains(uuid) && Minecraft.m_91087_().m_91403_() != null) {
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
      boolean isTTSPackActive = Minecraft.m_91087_().m_91098_().m_7536_().anyMatch(pack -> pack.m_5542_().contains("MCAVoices"));
      String language = Minecraft.m_91087_().f_91066_.f_92075_;
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

   public static void tickClient(Minecraft client) {
      destinyManager.tick(client);
      if (KeyBindings.SKIN_LIBRARY.m_90859_()) {
         Minecraft.m_91087_().m_91152_(new SkinLibraryScreen());
      }

      SpeechManager.INSTANCE.tick(client);
   }

   public static void addPlayerData(UUID uuid, VillagerEntityMCA villager) {
      playerData.put(uuid, villager);
      Minecraft client = Minecraft.m_91087_();
      if (client.f_91074_ != null) {
         client.f_91074_.m_6210_();
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
