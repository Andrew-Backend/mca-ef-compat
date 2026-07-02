package fabric.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fabric.net.mca.MCA;
import fabric.net.mca.resources.Rank;
import net.minecraft.class_195;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_4558;
import net.minecraft.class_5257;
import net.minecraft.class_5258;
import net.minecraft.class_5267;

public class VillagerFateCriterion extends class_4558<VillagerFateCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("villager_fate");

   public class_2960 method_794() {
      return ID;
   }

   public VillagerFateCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      Rank userRelation = Rank.fromName(json.get("user_relation").getAsString());
      class_2960 cause = class_2960.method_12829(json.get("cause").getAsString());
      return new VillagerFateCriterion.Conditions(player, cause, userRelation);
   }

   public void trigger(class_3222 player, class_2960 cause, Rank userRelation) {
      this.method_22510(player, conditions -> conditions.test(cause, userRelation));
   }

   public static class Conditions extends class_195 {
      private final Rank userRelation;
      private final class_2960 cause;

      public Conditions(class_5258 player, class_2960 cause, Rank userRelation) {
         super(VillagerFateCriterion.ID, player);
         this.userRelation = userRelation;
         this.cause = cause;
      }

      public boolean test(class_2960 cause, Rank userRelation) {
         return this.cause.toString().equals(cause.toString()) && userRelation.isAtLeast(this.userRelation);
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("cause", new JsonPrimitive(this.cause.toString()));
         json.add("user_relation", new JsonPrimitive(this.userRelation.name()));
         return json;
      }
   }
}
