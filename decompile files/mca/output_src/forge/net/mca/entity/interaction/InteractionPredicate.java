package forge.net.mca.entity.interaction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.interaction.gifts.GiftPredicate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
            chance = GsonHelper.m_13897_(entry.getValue(), entry.getKey());
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

   public float test(VillagerEntityMCA villager, ServerPlayer player) {
      return this.condition != null ? this.condition.test(villager, ItemStack.f_41583_, player) : 0.0F;
   }

   public int getChance() {
      return this.chance;
   }

   public List<String> getConditionKeys() {
      return this.conditionKeys;
   }
}
