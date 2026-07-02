package quilt.net.mca.entity.ai.chatAI;

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
import net.minecraft.class_3222;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.util.WorldUtils;

public class ChatAI {
   private static final int VILLAGER_SEARCH_RANGE = 32;
   private static final int CONVERSATION_TIME = 1200;
   private static final int CONVERSATION_DISTANCE = 16;
   private static final Map<UUID, ChatAIStrategy> strategies = new HashMap<>();
   private static final Map<UUID, ChatAI.OpenConversation> currentConversations = new ConcurrentHashMap<>();

   public static Optional<String> answer(class_3222 player, VillagerEntityMCA villager, String msg) {
      ChatAIStrategy strategy = computeStrategyIfAbsent(villager.method_5667());
      long time = villager.method_37908().method_8510();
      currentConversations.put(player.method_5667(), new ChatAI.OpenConversation(villager.method_5667(), time));
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

   public static Optional<VillagerEntityMCA> getVillagerForConversation(class_3222 player, String msg) {
      UUID playerUUID = player.method_5667();
      List<VillagerEntityMCA> nearbyVillagers = WorldUtils.getCloseEntities(player.method_37908(), player, 32.0, VillagerEntityMCA.class);
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
      Optional<VillagerEntityMCA> optionalVillager = nearbyVillagers.stream().filter(v -> conv.villagerUUID.equals(v.method_5667())).findFirst();
      return optionalVillager.isPresent() && isInConversationWith(player, optionalVillager.get()) ? optionalVillager : Optional.empty();
   }

   private static boolean isInConversationWith(class_3222 player, VillagerEntityMCA villager) {
      ChatAI.OpenConversation conversation = currentConversations.getOrDefault(player.method_5667(), new ChatAI.OpenConversation(villager.method_5667(), 0L));
      return villager.method_5739(player) < 16.0F && villager.method_37908().method_8510() < conversation.lastInteractionTime + 1200L;
   }

   public static Optional<VillagerEntityMCA> findVillagerInArea(class_3222 player, String searchName) {
      List<VillagerEntityMCA> entities = WorldUtils.getCloseEntities(player.method_37908(), player, 32.0, VillagerEntityMCA.class);
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
