package forge.net.mca.resources;

import com.google.gson.JsonElement;
import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.network.s2c.AnalysisResults;
import forge.net.mca.resources.data.SerializablePair;
import forge.net.mca.resources.data.analysis.ChanceAnalysis;
import forge.net.mca.resources.data.analysis.IntAnalysis;
import forge.net.mca.resources.data.dialogue.Actions;
import forge.net.mca.resources.data.dialogue.Answer;
import forge.net.mca.resources.data.dialogue.Question;
import forge.net.mca.resources.data.dialogue.Result;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class Dialogues extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = new ResourceLocation("mca", "dialogues");
   private static Dialogues INSTANCE;
   private final Map<String, Question> questions = new HashMap<>();

   public static Dialogues getInstance() {
      return INSTANCE;
   }

   public Dialogues() {
      super(Resources.GSON, "dialogues");
      INSTANCE = this;
   }

   protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      this.questions.clear();
      data.forEach(this::loadDialogue);
   }

   private void loadDialogue(ResourceLocation identifier, JsonElement element) {
      String id = identifier.m_135815_().substring(identifier.m_135815_().lastIndexOf(47) + 1);
      if (!this.checkIsMcaDialogue(element)) {
         MCA.LOGGER.warn("Dialogue {} is not properly formatted, not loading", identifier);
      } else {
         Question q = Question.fromJson(id, element.getAsJsonObject());
         if (this.questions.containsKey(id)) {
            q.merge(this.questions.get(id));
         }

         q.getAnswers().sort(Comparator.comparingInt(Answer::getPriority));
         this.questions.put(id, q);
      }
   }

   private boolean checkIsMcaDialogue(JsonElement element) {
      JsonElement answersElement = element.getAsJsonObject().get("answers");
      return answersElement != null && answersElement.isJsonArray();
   }

   public Question getQuestion(String i) {
      return this.questions.get(i);
   }

   public void selectAnswer(VillagerEntityMCA villager, ServerPlayer player, String questionId, String answerId) {
      Question question = this.getQuestion(questionId);
      Answer answer = question.getAnswer(answerId);
      int total = 0;
      List<IntAnalysis> analysis = new LinkedList<>();

      for (Result r : answer.getResults()) {
         IntAnalysis a = r.getChances(villager, player);
         analysis.add(a);
         total += Math.max(0, a.getTotal());
      }

      int chosen = -1;
      total = total == 0 ? 0 : villager.m_217043_().m_188503_(total);

      for (IntAnalysis a : analysis) {
         total -= Math.max(0, a.getTotal());
         chosen++;
         if (total < 0) {
            break;
         }
      }

      Actions chosenActions = answer.getResults().get(chosen).getActions();
      if (chosenActions.isNegative() || chosenActions.isPositive()) {
         ChanceAnalysis finalAnalysis = new ChanceAnalysis();

         for (int i = 0; i < analysis.size(); i++) {
            boolean positive = answer.getResults().get(i).getActions().isPositive();
            boolean negative = answer.getResults().get(i).getActions().isNegative();

            for (SerializablePair<String, Integer> value : analysis.get(i).getSummands()) {
               finalAnalysis.add(value.getLeft(), value.getRight() * (positive ? 1 : (negative ? -1 : 0)));
            }
         }

         NetworkHandler.sendToPlayer(new AnalysisResults(finalAnalysis), player);
      }

      chosenActions.trigger(villager, player);
   }
}
