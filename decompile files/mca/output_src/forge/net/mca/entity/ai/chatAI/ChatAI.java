package forge.net.mca.entity.ai.chatAI;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.util.WorldUtils;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.minecraft.server.level.ServerPlayer;

public class ChatAI {
   private static final int VILLAGER_SEARCH_RANGE = 32;
   private static final int CONVERSATION_TIME = 1200;
   private static final int CONVERSATION_DISTANCE = 16;
   private static final Map<UUID, ChatAIStrategy> strategies = new HashMap<>();
   private static final Map<UUID, ChatAI.OpenConversation> currentConversations = new ConcurrentHashMap<>();

   public static Optional<String> answer(ServerPlayer player, VillagerEntityMCA villager, String msg) {
      ChatAIStrategy strategy = computeStrategyIfAbsent(villager.m_20148_());
      long time = villager.m_9236_().m_46467_();
      currentConversations.put(player.m_20148_(), new ChatAI.OpenConversation(villager.m_20148_(), time));
      return strategy.answer(player, villager, msg);
   }

   private static ChatAIStrategy computeStrategyIfAbsent(UUID villagerID) {
      return strategies.computeIfAbsent(villagerID, v -> {
         String inworldResourceName = Config.getInstance().inworldAIResourceNames.getOrDefault(v, "");
         return inworldResourceName.isEmpty() ? new OpenAIChatAI() : new InworldAI(inworldResourceName);
      });
   }

   public static void clearStrategy(UUID villagerID) {
      strategies.remove(villagerID);
   }

   public static Optional<VillagerEntityMCA> getVillagerForConversation(ServerPlayer player, String msg) {
      UUID playerUUID = player.m_20148_();
      List<VillagerEntityMCA> nearbyVillagers = WorldUtils.getCloseEntities(player.m_9236_(), player, 32.0, VillagerEntityMCA.class);
      String normalizedMsg = normalizeString(msg);

      for (VillagerEntityMCA villager : nearbyVillagers) {
         String normalizedName = normalizeString(villager.getTrackedValue(VillagerLike.VILLAGER_NAME));
         String[] nameParts = normalizedName.split(" ");

         for (String part : nameParts) {
            if (Pattern.compile("\\b" + Pattern.quote(part) + "\\b").matcher(normalizedMsg).find()) {
               return Optional.of(villager);
            }
         }
      }

      ChatAI.OpenConversation conv = currentConversations.getOrDefault(playerUUID, new ChatAI.OpenConversation(playerUUID, 0L));
      Optional<VillagerEntityMCA> optionalVillager = nearbyVillagers.stream().filter(v -> conv.villagerUUID.equals(v.m_20148_())).findFirst();
      return optionalVillager.isPresent() && isInConversationWith(player, optionalVillager.get()) ? optionalVillager : Optional.empty();
   }

   private static boolean isInConversationWith(ServerPlayer player, VillagerEntityMCA villager) {
      ChatAI.OpenConversation conversation = currentConversations.getOrDefault(player.m_20148_(), new ChatAI.OpenConversation(villager.m_20148_(), 0L));
      return villager.m_20270_(player) < 16.0F && villager.m_9236_().m_46467_() < conversation.lastInteractionTime + 1200L;
   }

   public static Optional<VillagerEntityMCA> findVillagerInArea(ServerPlayer player, String searchName) {
      List<VillagerEntityMCA> entities = WorldUtils.getCloseEntities(player.m_9236_(), player, 32.0, VillagerEntityMCA.class);
      String normalizedSearchName = normalizeString(searchName);

      for (VillagerEntityMCA villager : entities) {
         String villagerName = normalizeString(villager.getTrackedValue(VillagerLike.VILLAGER_NAME));
         if (normalizedSearchName.equals(villagerName)) {
            return Optional.of(villager);
         }
      }

      return Optional.empty();
   }

   private static String normalizeString(String string) {
      return Normalizer.normalize(string, Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
   }

   private record OpenConversation(UUID villagerUUID, Long lastInteractionTime) {
   }
}
