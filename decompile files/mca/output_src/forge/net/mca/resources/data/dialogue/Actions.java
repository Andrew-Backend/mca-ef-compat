package forge.net.mca.resources.data.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.LongTermMemory;
import forge.net.mca.network.s2c.InteractionDialogueQuestionResponse;
import forge.net.mca.network.s2c.InteractionDialogueResponse;
import forge.net.mca.resources.Dialogues;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class Actions {
   public static final Map<String, Actions.Factory<JsonElement>> TYPES = new HashMap<>();
   private final List<Actions.Action> actions;
   private final boolean positive;
   private final boolean negative;

   public static <T> void register(String name, BiFunction<JsonElement, String, T> jsonParser, Actions.Factory<T> predicate) {
      TYPES.put(name, json -> predicate.parse(jsonParser.apply(json, name)));
   }

   public static Actions fromJson(JsonObject json) {
      List<Actions.Action> actions = new LinkedList<>();
      boolean positive = false;
      boolean negative = false;

      for (Entry<String, JsonElement> entry : json.entrySet()) {
         if (TYPES.containsKey(entry.getKey())) {
            Actions.Action parsed = TYPES.get(entry.getKey()).parse(entry.getValue());
            actions.add(parsed);
            if (entry.getKey().equals("positive")) {
               positive = true;
            }

            if (entry.getKey().equals("negative")) {
               negative = true;
            }
         } else {
            MCA.LOGGER.info("Unknown dialogue action " + entry.getKey());
         }
      }

      if (!json.has("next")) {
         Actions.Action parsed = TYPES.get("quit").parse(json);
         actions.add(parsed);
      }

      return new Actions(actions, positive, negative);
   }

   public Actions(List<Actions.Action> actions, boolean positive, boolean negative) {
      this.actions = actions;
      this.positive = positive;
      this.negative = negative;
   }

   public void trigger(VillagerEntityMCA villager, ServerPlayer player) {
      for (Actions.Action c : this.actions) {
         c.trigger(villager, player);
      }
   }

   public boolean isPositive() {
      return this.positive;
   }

   public boolean isNegative() {
      return this.negative;
   }

   static {
      register("next", GsonHelper::m_13805_, id -> (villager, player) -> {
         if (id != null) {
            Question newQuestion = Dialogues.getInstance().getQuestion(id);
            if (newQuestion != null) {
               if (newQuestion.isAuto()) {
                  Dialogues.getInstance().selectAnswer(villager, player, newQuestion.getName(), newQuestion.getRandomAnswer().getName());
                  return;
               }

               MutableComponent text = villager.getTranslatable(player, Question.getTranslationKey(id));
               NetworkHandler.sendToPlayer(new InteractionDialogueResponse(newQuestion, player, villager), player);
               NetworkHandler.sendToPlayer(new InteractionDialogueQuestionResponse(newQuestion.isSilent(), text), player);
            } else {
               villager.sendChatMessage(player, Question.getTranslationKey(id));
            }

            if (newQuestion == null || newQuestion.isCloseScreen()) {
               villager.getInteractions().stopInteracting();
            }
         } else {
            villager.getInteractions().stopInteracting();
         }
      });
      register("say", GsonHelper::m_13805_, id -> (villager, player) -> {
         MutableComponent text = villager.getTranslatable(player, Question.getTranslationKey(id));
         NetworkHandler.sendToPlayer(new InteractionDialogueQuestionResponse(false, text), player);
      });
      register("remember", GsonHelper::m_13918_, json -> (villager, player) -> {
         String id = LongTermMemory.parseId(json, player);
         if (json.has("time")) {
            villager.getLongTermMemory().remember(id, json.get("time").getAsLong());
         } else {
            villager.getLongTermMemory().remember(id);
         }
      });
      register("quit", (a, b) -> a, id -> (villager, player) -> villager.getInteractions().stopInteracting());
      register("negative", GsonHelper::m_13897_, hearts -> (villager, player) -> {
         villager.getVillagerBrain().modifyMoodValue(-hearts);
         villager.getVillagerBrain().rewardHearts(player, -hearts);
      });
      register("positive", GsonHelper::m_13897_, hearts -> (villager, player) -> {
         villager.getVillagerBrain().modifyMoodValue(hearts);
         villager.getVillagerBrain().rewardHearts(player, hearts);
      });
      register("command", GsonHelper::m_13805_, command -> (villager, player) -> villager.getInteractions().handle(player, command));
   }

   public interface Action {
      void trigger(VillagerEntityMCA var1, ServerPlayer var2);
   }

   public interface Factory<T> {
      Actions.Action parse(T var1);
   }
}
