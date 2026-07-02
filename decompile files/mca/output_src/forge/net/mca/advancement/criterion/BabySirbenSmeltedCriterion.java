package forge.net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BabySirbenSmeltedCriterion extends SimpleCriterionTrigger<BabySirbenSmeltedCriterion.Conditions> {
   private static final ResourceLocation ID = MCA.locate("baby_sirben_smelted");

   public ResourceLocation m_7295_() {
      return ID;
   }

   public BabySirbenSmeltedCriterion.Conditions conditionsFromJson(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializer) {
      Ints c = Ints.m_55386_(json.get("count").getAsInt());
      return new BabySirbenSmeltedCriterion.Conditions(player, c);
   }

   public void trigger(ServerPlayer player, int c) {
      this.m_66234_(player, conditions -> conditions.test(c));
   }

   public static class Conditions extends AbstractCriterionTriggerInstance {
      private final Ints count;

      public Conditions(ContextAwarePredicate player, Ints count) {
         super(BabySirbenSmeltedCriterion.ID, player);
         this.count = count;
      }

      public boolean test(int c) {
         return this.count.m_55390_(c);
      }

      public JsonObject m_7683_(SerializationContext serializer) {
         JsonObject json = super.m_7683_(serializer);
         json.add("count", this.count.m_55328_());
         return json;
      }
   }
}
