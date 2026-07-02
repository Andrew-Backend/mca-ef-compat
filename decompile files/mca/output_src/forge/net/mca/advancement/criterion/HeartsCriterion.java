package forge.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import forge.net.mca.MCA;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class HeartsCriterion extends SimpleCriterionTrigger<HeartsCriterion.Conditions> {
   private static final ResourceLocation ID = MCA.locate("hearts");

   public ResourceLocation m_7295_() {
      return ID;
   }

   public HeartsCriterion.Conditions conditionsFromJson(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializer) {
      Ints hearts = Ints.m_55373_(json.get("hearts"));
      Ints increase = Ints.m_55373_(json.get("increase"));
      String source = json.has("source") ? json.get("source").getAsString() : "";
      return new HeartsCriterion.Conditions(player, hearts, increase, source);
   }

   public void trigger(ServerPlayer player, int hearts, int increase, String source) {
      this.m_66234_(player, conditions -> conditions.test(hearts, increase, source));
   }

   public static class Conditions extends AbstractCriterionTriggerInstance {
      private final Ints hearts;
      private final Ints increase;
      private final String source;

      public Conditions(ContextAwarePredicate player, Ints hearts, Ints increase, String source) {
         super(HeartsCriterion.ID, player);
         this.hearts = hearts;
         this.increase = increase;
         this.source = source;
      }

      public boolean test(int hearts, int increase, String source) {
         return this.hearts.m_55390_(hearts) && this.increase.m_55390_(increase) && (MCA.isBlankString(this.source) || this.source.equals(source));
      }

      public JsonObject m_7683_(SerializationContext serializer) {
         JsonObject json = super.m_7683_(serializer);
         json.add("hearts", this.hearts.m_55328_());
         json.add("increase", this.increase.m_55328_());
         json.add("source", new JsonPrimitive(this.source));
         return json;
      }
   }
}
