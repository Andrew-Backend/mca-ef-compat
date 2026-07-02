package fabric.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import fabric.net.mca.MCA;
import net.minecraft.class_195;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_4558;
import net.minecraft.class_5257;
import net.minecraft.class_5258;
import net.minecraft.class_5267;
import net.minecraft.class_2096.class_2100;

public class BabyCriterion extends class_4558<BabyCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("baby");

   public class_2960 method_794() {
      return ID;
   }

   public BabyCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      class_2100 c = class_2100.method_9056(json.get("count"));
      return new BabyCriterion.Conditions(player, c);
   }

   public void trigger(class_3222 player, int c) {
      this.method_22510(player, conditions -> conditions.test(c));
   }

   public static class Conditions extends class_195 {
      private final class_2100 count;

      public Conditions(class_5258 player, class_2100 count) {
         super(BabyCriterion.ID, player);
         this.count = count;
      }

      public boolean test(int c) {
         return this.count.method_9054(c);
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("count", this.count.method_9036());
         return json;
      }
   }
}
