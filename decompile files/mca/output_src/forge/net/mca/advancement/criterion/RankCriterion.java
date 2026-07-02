package forge.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import forge.net.mca.MCA;
import forge.net.mca.resources.Rank;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class RankCriterion extends SimpleCriterionTrigger<RankCriterion.Conditions> {
   private static final ResourceLocation ID = MCA.locate("rank");

   public ResourceLocation m_7295_() {
      return ID;
   }

   public RankCriterion.Conditions conditionsFromJson(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializer) {
      Rank rank = Rank.fromName(json.get("rank").getAsString());
      return new RankCriterion.Conditions(player, rank);
   }

   public void trigger(ServerPlayer player, Rank rank) {
      this.m_66234_(player, conditions -> conditions.test(rank));
   }

   public static class Conditions extends AbstractCriterionTriggerInstance {
      private final Rank rank;

      public Conditions(ContextAwarePredicate player, Rank rank) {
         super(RankCriterion.ID, player);
         this.rank = rank;
      }

      public boolean test(Rank rank) {
         return this.rank == rank;
      }

      public JsonObject m_7683_(SerializationContext serializer) {
         JsonObject json = super.m_7683_(serializer);
         json.add("rank", new JsonPrimitive(this.rank.name()));
         return json;
      }
   }
}
