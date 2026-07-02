package quilt.net.mca.resources;

import com.google.gson.JsonElement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4309;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.network.s2c.AnalysisResults;
import quilt.net.mca.resources.data.SerializablePair;
import quilt.net.mca.resources.data.analysis.ChanceAnalysis;
import quilt.net.mca.resources.data.analysis.IntAnalysis;
import quilt.net.mca.resources.data.dialogue.Actions;
import quilt.net.mca.resources.data.dialogue.Answer;
import quilt.net.mca.resources.data.dialogue.Question;
import quilt.net.mca.resources.data.dialogue.Result;

public class Dialogues extends class_4309 {
   protected static final class_2960 ID = new class_2960("mca", "dialogues");
   private static Dialogues INSTANCE;
   private final Map<String, Question> questions = new HashMap<>();

   public static Dialogues getInstance() {
      return INSTANCE;
   }

   public Dialogues() {
      super(Resources.GSON, "dialogues");
      INSTANCE = this;
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.questions.clear();
      data.forEach(this::loadDialogue);
   }

   private void loadDialogue(class_2960 identifier, JsonElement element) {
      String id = identifier.method_12832().substring(identifier.method_12832().lastIndexOf(47) + 1);
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

   public void selectAnswer(VillagerEntityMCA villager, class_3222 player, String questionId, String answerId) {
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
      total = total == 0 ? 0 : villager.method_6051().method_43048(total);

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
