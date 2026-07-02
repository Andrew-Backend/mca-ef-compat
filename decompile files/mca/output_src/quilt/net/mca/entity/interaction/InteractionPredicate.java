package quilt.net.mca.entity.interaction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.class_1799;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.interaction.gifts.GiftPredicate;

public class InteractionPredicate {
   private final int chance;
   @Nullable
   private final GiftPredicate.Condition condition;
   final List<String> conditionKeys;

   public static InteractionPredicate fromJson(JsonObject json) {
      int chance = 0;
      GiftPredicate.Condition condition = null;
      List<String> conditionKeys = new LinkedList<>();

      for (Entry<String, JsonElement> entry : json.entrySet()) {
         if ("chance".equals(entry.getKey())) {
            chance = class_3518.method_15257(entry.getValue(), entry.getKey());
         } else if (GiftPredicate.CONDITION_TYPES.containsKey(entry.getKey())) {
            GiftPredicate.Condition parsed = GiftPredicate.CONDITION_TYPES.get(entry.getKey()).parse(entry.getValue());
            conditionKeys.add(entry.getKey());
            if (condition == null) {
               condition = parsed;
            } else {
               condition = condition.and(parsed);
            }
         } else {
            MCA.LOGGER.warn("Interaction predicate " + entry.getKey() + " does not exist!");
         }
      }

      return new InteractionPredicate(chance, condition, conditionKeys);
   }

   public InteractionPredicate(int chance, @Nullable GiftPredicate.Condition condition, List<String> conditionKeys) {
      this.chance = chance;
      this.condition = condition;
      this.conditionKeys = conditionKeys;
   }

   public float test(VillagerEntityMCA villager, class_3222 player) {
      return this.condition != null ? this.condition.test(villager, class_1799.field_8037, player) : 0.0F;
   }

   public int getChance() {
      return this.chance;
   }

   public List<String> getConditionKeys() {
      return this.conditionKeys;
   }
}
