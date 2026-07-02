package fabric.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fabric.net.mca.MCA;
import net.minecraft.class_195;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_4558;
import net.minecraft.class_5257;
import net.minecraft.class_5258;
import net.minecraft.class_5267;
import net.minecraft.class_2096.class_2100;

public class HeartsCriterion extends class_4558<HeartsCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("hearts");

   public class_2960 method_794() {
      return ID;
   }

   public HeartsCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      class_2100 hearts = class_2100.method_9056(json.get("hearts"));
      class_2100 increase = class_2100.method_9056(json.get("increase"));
      String source = json.has("source") ? json.get("source").getAsString() : "";
      return new HeartsCriterion.Conditions(player, hearts, increase, source);
   }

   public void trigger(class_3222 player, int hearts, int increase, String source) {
      this.method_22510(player, conditions -> conditions.test(hearts, increase, source));
   }

   public static class Conditions extends class_195 {
      private final class_2100 hearts;
      private final class_2100 increase;
      private final String source;

      public Conditions(class_5258 player, class_2100 hearts, class_2100 increase, String source) {
         super(HeartsCriterion.ID, player);
         this.hearts = hearts;
         this.increase = increase;
         this.source = source;
      }

      public boolean test(int hearts, int increase, String source) {
         return this.hearts.method_9054(hearts) && this.increase.method_9054(increase) && (MCA.isBlankString(this.source) || this.source.equals(source));
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("hearts", this.hearts.method_9036());
         json.add("increase", this.increase.method_9036());
         json.add("source", new JsonPrimitive(this.source));
         return json;
      }
   }
}
