package forge.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import forge.net.mca.MCA;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ChildAgeStateChangeCriterion extends SimpleCriterionTrigger<ChildAgeStateChangeCriterion.Conditions> {
   private static final ResourceLocation ID = MCA.locate("child_age_state_change");

   public ResourceLocation m_7295_() {
      return ID;
   }

   public ChildAgeStateChangeCriterion.Conditions conditionsFromJson(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializer) {
      String event = json.has("state") ? json.get("state").getAsString() : "";
      return new ChildAgeStateChangeCriterion.Conditions(player, event);
   }

   public void trigger(ServerPlayer player, String event) {
      this.m_66234_(player, conditions -> conditions.test(event));
   }

   public static class Conditions extends AbstractCriterionTriggerInstance {
      private final String event;

      public Conditions(ContextAwarePredicate player, String event) {
         super(ChildAgeStateChangeCriterion.ID, player);
         this.event = event;
      }

      public boolean test(String event) {
         return this.event.equals(event);
      }

      public JsonObject m_7683_(SerializationContext serializer) {
         JsonObject json = super.m_7683_(serializer);
         json.add("state", new JsonPrimitive(this.event));
         return json;
      }
   }
}
