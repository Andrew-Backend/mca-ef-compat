package forge.net.mca.resources.data.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.interaction.InteractionPredicate;
import forge.net.mca.resources.data.analysis.IntAnalysis;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;

public class Result {
   private final Actions actions;
   private final int baseChance;
   private final List<InteractionPredicate> conditions;
   private final boolean applyFatigue;

   public Result(Actions actions, int baseChance, List<InteractionPredicate> conditions, boolean applyFatigue) {
      this.actions = actions;
      this.baseChance = baseChance;
      this.conditions = conditions;
      this.applyFatigue = applyFatigue;
   }

   public static Result fromJson(JsonObject json) {
      Actions actions = Actions.fromJson(json.getAsJsonObject("actions"));
      int baseChance = json.has("baseChance") ? json.get("baseChance").getAsInt() : 0;
      List<InteractionPredicate> conditions = new LinkedList<>();
      if (json.has("conditions")) {
         for (JsonElement e : json.getAsJsonArray("conditions")) {
            conditions.add(InteractionPredicate.fromJson(e.getAsJsonObject()));
         }
      }

      boolean applyFatigue = json.has("applyFatigue") && json.get("applyFatigue").getAsBoolean();
      return new Result(actions, baseChance, conditions, applyFatigue);
   }

   public Actions getActions() {
      return this.actions;
   }

   public int getBaseChance() {
      return this.baseChance;
   }

   public boolean shouldApplyFatigue() {
      return this.applyFatigue;
   }

   public List<InteractionPredicate> getConditions() {
      return Objects.requireNonNullElse(this.conditions, Collections.emptyList());
   }

   public IntAnalysis getChances(VillagerEntityMCA villager, ServerPlayer player) {
      IntAnalysis analysis = new IntAnalysis();
      Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);
      if (this.getBaseChance() != 0) {
         analysis.add("base", this.getBaseChance());
      }

      int f = (int)(memory.getInteractionFatigue() * Config.getInstance().interactionChanceFatigue);
      if (this.shouldApplyFatigue() && f > 0) {
         analysis.add("fatigue", f);
      }

      for (InteractionPredicate c : this.getConditions()) {
         int chance = (int)(c.getChance() * c.test(villager, player));
         if (chance != 0) {
            analysis.add(c.getConditionKeys().get(0), chance);
         }
      }

      return analysis;
   }
}
