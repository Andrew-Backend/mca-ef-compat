package fabric.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import fabric.net.mca.MCA;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import net.minecraft.class_195;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_4558;
import net.minecraft.class_5257;
import net.minecraft.class_5258;
import net.minecraft.class_5267;
import net.minecraft.class_2096.class_2100;

public class FamilyCriterion extends class_4558<FamilyCriterion.Conditions> {
   private static final class_2960 ID = MCA.locate("family");

   public class_2960 method_794() {
      return ID;
   }

   public FamilyCriterion.Conditions conditionsFromJson(JsonObject json, class_5258 player, class_5257 deserializer) {
      class_2100 c = class_2100.method_9056(json.get("children"));
      class_2100 gc = class_2100.method_9056(json.get("grandchildren"));
      return new FamilyCriterion.Conditions(player, c, gc);
   }

   public void trigger(class_3222 player) {
      FamilyTreeNode familyTree = FamilyTree.get(player.method_51469()).getOrCreate(player);
      long c = familyTree.getRelatives(0, 1).count();
      long gc = familyTree.getRelatives(0, 2).count() - c;
      this.method_22510(player, condition -> condition.test((int)c, (int)gc));
   }

   public static class Conditions extends class_195 {
      private final class_2100 children;
      private final class_2100 grandchildren;

      public Conditions(class_5258 player, class_2100 children, class_2100 grandchildren) {
         super(FamilyCriterion.ID, player);
         this.children = children;
         this.grandchildren = grandchildren;
      }

      public boolean test(int c, int gc) {
         return this.children.method_9054(c) && this.grandchildren.method_9054(gc);
      }

      public JsonObject method_807(class_5267 serializer) {
         JsonObject json = super.method_807(serializer);
         json.add("children", this.children.method_9036());
         json.add("grandchildren", this.grandchildren.method_9036());
         return json;
      }
   }
}
