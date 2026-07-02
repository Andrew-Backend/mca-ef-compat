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

public class GenericEventCriterion extends SimpleCriterionTrigger<GenericEventCriterion.Conditions> {
   private static final ResourceLocation ID = MCA.locate("generic_event");

   public ResourceLocation m_7295_() {
      return ID;
   }

   public GenericEventCriterion.Conditions conditionsFromJson(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializer) {
      String event = json.has("event") ? json.get("event").getAsString() : "";
      return new GenericEventCriterion.Conditions(player, event);
   }

   public void trigger(ServerPlayer player, String event) {
      this.m_66234_(player, conditions -> conditions.test(event));
   }

   public static class Conditions extends AbstractCriterionTriggerInstance {
      private final String event;

      public Conditions(ContextAwarePredicate player, String event) {
         super(GenericEventCriterion.ID, player);
         this.event = event;
      }

      public boolean test(String event) {
         return this.event.equals(event);
      }

      public JsonObject m_7683_(SerializationContext serializer) {
         JsonObject json = super.m_7683_(serializer);
         json.add("event", new JsonPrimitive(this.event));
         return json;
      }
   }
}
