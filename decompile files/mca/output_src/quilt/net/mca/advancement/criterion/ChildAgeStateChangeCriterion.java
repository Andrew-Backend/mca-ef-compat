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

public class ChildAgeStateChangeCriterion extends class_4558<ChildAgeStateChangeCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("child_age_state_change");

   public class_2960 method_794() {
      return ID;
   }

   public ChildAgeStateChangeCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      String event = json.has("state") ? json.get("state").getAsString() : "";
      return new ChildAgeStateChangeCriterion.Conditions(player, event);
   }

   public void trigger(class_3222 player, String event) {
      this.method_22510(player, conditions -> conditions.test(event));
   }

   public static class Conditions extends class_195 {
      private final String event;

      public Conditions(class_5258 player, String event) {
         super(ChildAgeStateChangeCriterion.ID, player);
         this.event = event;
      }

      public boolean test(String event) {
         return this.event.equals(event);
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("state", new JsonPrimitive(this.event));
         return json;
      }
   }
}
