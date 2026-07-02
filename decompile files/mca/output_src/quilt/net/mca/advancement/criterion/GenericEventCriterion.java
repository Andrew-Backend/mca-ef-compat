package quilt.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.class_195;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_4558;
import net.minecraft.class_5257;
import net.minecraft.class_5258;
import net.minecraft.class_5267;
import quilt.net.mca.MCA;

public class GenericEventCriterion extends class_4558<GenericEventCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("generic_event");

   public class_2960 method_794() {
      return ID;
   }

   public GenericEventCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      String event = json.has("event") ? json.get("event").getAsString() : "";
      return new GenericEventCriterion.Conditions(player, event);
   }

   public void trigger(class_3222 player, String event) {
      this.method_22510(player, conditions -> conditions.test(event));
   }

   public static class Conditions extends class_195 {
      private final String event;

      public Conditions(class_5258 player, String event) {
         super(GenericEventCriterion.ID, player);
         this.event = event;
      }

      public boolean test(String event) {
         return this.event.equals(event);
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("event", new JsonPrimitive(this.event));
         return json;
      }
   }
}
