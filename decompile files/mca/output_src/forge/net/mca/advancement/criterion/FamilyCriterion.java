package forge.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FamilyCriterion extends SimpleCriterionTrigger<FamilyCriterion.Conditions> {
   private static final ResourceLocation ID = MCA.locate("family");

   public ResourceLocation m_7295_() {
      return ID;
   }

   public FamilyCriterion.Conditions conditionsFromJson(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializer) {
      Ints c = Ints.m_55373_(json.get("children"));
      Ints gc = Ints.m_55373_(json.get("grandchildren"));
      return new FamilyCriterion.Conditions(player, c, gc);
   }

   public void trigger(ServerPlayer player) {
      FamilyTreeNode familyTree = FamilyTree.get(player.m_284548_()).getOrCreate(player);
      long c = familyTree.getRelatives(0, 1).count();
      long gc = familyTree.getRelatives(0, 2).count() - c;
      this.m_66234_(player, condition -> condition.test((int)c, (int)gc));
   }

   public static class Conditions extends AbstractCriterionTriggerInstance {
      private final Ints children;
      private final Ints grandchildren;

      public Conditions(ContextAwarePredicate player, Ints children, Ints grandchildren) {
         super(FamilyCriterion.ID, player);
         this.children = children;
         this.grandchildren = grandchildren;
      }

      public boolean test(int c, int gc) {
         return this.children.m_55390_(c) && this.grandchildren.m_55390_(gc);
      }

      public JsonObject m_7683_(SerializationContext serializer) {
         JsonObject json = super.m_7683_(serializer);
         json.add("children", this.children.m_55328_());
         json.add("grandchildren", this.grandchildren.m_55328_());
         return json;
      }
   }
}
