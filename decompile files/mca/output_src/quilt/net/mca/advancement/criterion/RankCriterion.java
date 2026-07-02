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
import quilt.net.mca.resources.Rank;

public class RankCriterion extends class_4558<RankCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("rank");

   public class_2960 method_794() {
      return ID;
   }

   public RankCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      Rank rank = Rank.fromName(json.get("rank").getAsString());
      return new RankCriterion.Conditions(player, rank);
   }

   public void trigger(class_3222 player, Rank rank) {
      this.method_22510(player, conditions -> conditions.test(rank));
   }

   public static class Conditions extends class_195 {
      private final Rank rank;

      public Conditions(class_5258 player, Rank rank) {
         super(RankCriterion.ID, player);
         this.rank = rank;
      }

      public boolean test(Rank rank) {
         return this.rank == rank;
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("rank", new JsonPrimitive(this.rank.name()));
         return json;
      }
   }
}
