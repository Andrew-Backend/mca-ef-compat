package yesman.epicfight.data.loot.function;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import java.util.List;
import java.util.NoSuchElementException;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.skill.Skill;

public class SetSkillFunction implements LootItemFunction {
   private final List<FloatObjectPair<String>> skillsAndWeight;

   public SetSkillFunction(List<FloatObjectPair<String>> skillsAndWeight) {
      this.skillsAndWeight = skillsAndWeight;
   }

   private Skill getSkillForSeed(float seed) {
      for (FloatObjectPair<String> pair : this.skillsAndWeight) {
         if (seed < pair.firstFloat()) {
            return SkillManager.getSkill((String)pair.second());
         }
      }

      return this.skillsAndWeight.isEmpty() ? null : SkillManager.getSkill((String)this.skillsAndWeight.get(0).second());
   }

   public ItemStack apply(ItemStack itemstack, LootContext lootContext) {
      if (ModList.get().isLoaded("epicskills")) {
         return ItemStack.f_41583_;
      }

      float val = lootContext.m_230907_().m_188501_();
      Skill skill = this.getSkillForSeed(val);
      if (skill != null) {
         itemstack.m_41784_().m_128359_("skill", skill.toString());
      }

      return itemstack;
   }

   public static Builder builder(final String... skills) {
      return new Builder() {
         public LootItemFunction m_7453_() {
            List<FloatObjectPair<String>> list = Lists.newArrayList();
            float weight = 1.0F / skills.length;
            float weightSum = 0.0F;

            for (String skill : skills) {
               weightSum += weight;
               list.add(FloatObjectPair.of(weightSum, skill));
            }

            return new SetSkillFunction(list);
         }
      };
   }

   public static Builder builder(final Object... skillAndWeight) {
      return new Builder() {
         public LootItemFunction m_7453_() {
            List<FloatObjectPair<String>> list = Lists.newArrayList();
            float weightTotal = 0.0F;
            float weightSum = 0.0F;

            for (int i = 0; i < skillAndWeight.length / 2; i++) {
               weightTotal += skillAndWeight[i * 2];
            }

            for (int i = 0; i < skillAndWeight.length / 2; i++) {
               weightSum += skillAndWeight[i * 2];
               list.add(FloatObjectPair.of(weightSum / weightTotal, (String)skillAndWeight[i * 2 + 1]));
            }

            return new SetSkillFunction(list);
         }
      };
   }

   public LootItemFunctionType m_7162_() {
      return EpicFightLootTables.SET_SKILLBOOK_SKILL;
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SetSkillFunction> {
      public void serialize(JsonObject jsonObj, SetSkillFunction skillFunction, JsonSerializationContext jsonDeserializationContext) {
         JsonArray skillArray = new JsonArray();
         JsonArray weightArray = new JsonArray();

         for (FloatObjectPair<String> pair : skillFunction.skillsAndWeight) {
            skillArray.add((String)pair.second());
            weightArray.add(pair.firstFloat());
         }

         jsonObj.add("skills", skillArray);
         jsonObj.add("weights", weightArray);
      }

      public SetSkillFunction deserialize(JsonObject jsonObj, JsonDeserializationContext jsonDeserializationContext) {
         JsonArray skillArray = jsonObj.getAsJsonArray("skills");
         JsonArray weightArray = jsonObj.getAsJsonArray("weights");
         List<FloatObjectPair<String>> list = Lists.newArrayList();
         float totalWeights = 0.0F;

         for (int i = 0; i < skillArray.size(); i++) {
            totalWeights += weightArray.get(i).getAsFloat();
         }

         for (int i = 0; i < skillArray.size(); i++) {
            if (SkillManager.getSkill(skillArray.get(i).getAsString()) == null) {
               new NoSuchElementException("SetSkillFunction: There is no skill named " + skillArray.get(i).getAsString()).printStackTrace();
            } else {
               list.add(FloatObjectPair.of(weightArray.get(i).getAsFloat() / totalWeights, skillArray.get(i).getAsString()));
            }
         }

         return new SetSkillFunction(list);
      }
   }
}
