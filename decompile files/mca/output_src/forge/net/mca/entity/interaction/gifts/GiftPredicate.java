package forge.net.mca.entity.interaction.gifts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.LongTermMemory;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.Personality;
import forge.net.mca.entity.interaction.Constraint;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.Tasks;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class GiftPredicate {
   public static final Map<String, GiftPredicate.Factory<JsonElement>> CONDITION_TYPES = new HashMap<>();
   private final int satisfactionBoost;
   @Nullable
   private final GiftPredicate.Condition condition;
   List<String> conditionKeys;

   public static float divideAndAdd(JsonObject json, long value) {
      return Mth.m_14036_(
         (float)value / (json.has("dividend") ? json.get("dividend").getAsFloat() : 1.0F) + (json.has("add") ? json.get("add").getAsFloat() : 0.0F),
         0.0F,
         json.has("max") ? json.get("max").getAsFloat() : 1.0F
      );
   }

   public static <T> void register(String name, BiFunction<JsonElement, String, T> jsonParser, GiftPredicate.Factory<T> predicate) {
      CONDITION_TYPES.put(name, json -> predicate.parse(jsonParser.apply(json, name)));
   }

   public static GiftPredicate fromJson(JsonObject json) {
      int satisfaction = 0;
      GiftPredicate.Condition condition = null;
      List<String> conditionKeys = new LinkedList<>();

      for (Entry<String, JsonElement> entry : json.entrySet()) {
         if ("satisfaction_boost".equals(entry.getKey())) {
            satisfaction = GsonHelper.m_13897_(entry.getValue(), entry.getKey());
         } else if (CONDITION_TYPES.containsKey(entry.getKey())) {
            GiftPredicate.Condition parsed = CONDITION_TYPES.get(entry.getKey()).parse(entry.getValue());
            conditionKeys.add(entry.getKey());
            if (condition == null) {
               condition = parsed;
            } else {
               condition = condition.and(parsed);
            }
         }
      }

      return new GiftPredicate(satisfaction, condition, conditionKeys);
   }

   public GiftPredicate(int satisfactionBoost, @Nullable GiftPredicate.Condition condition, List<String> conditionKeys) {
      this.satisfactionBoost = satisfactionBoost;
      this.condition = condition;
      this.conditionKeys = conditionKeys;
   }

   public float test(VillagerEntityMCA recipient, ItemStack stack, @Nullable ServerPlayer player) {
      return this.condition != null ? this.condition.test(recipient, stack, player) : 0.0F;
   }

   public int getSatisfactionFor(VillagerEntityMCA recipient, ItemStack stack, @Nullable ServerPlayer player) {
      return (int)(this.test(recipient, stack, player) * this.satisfactionBoost);
   }

   public List<String> getConditionKeys() {
      return this.conditionKeys;
   }

   static {
      register(
         "profession",
         (json, name) -> new ResourceLocation(GsonHelper.m_13805_(json, name)),
         profession -> (villager, stack, player) -> BuiltInRegistries.f_256735_.m_7981_(villager.getProfession()).equals(profession) ? 1.0F : 0.0F
      );
      register(
         "age_group",
         (json, name) -> AgeState.valueOf(GsonHelper.m_13805_(json, name).toUpperCase(Locale.ENGLISH)),
         group -> (villager, stack, player) -> villager.getAgeState() == group ? 1.0F : 0.0F
      );
      register(
         "gender",
         (json, name) -> Gender.valueOf(GsonHelper.m_13805_(json, name).toUpperCase(Locale.ENGLISH)),
         gender -> (villager, stack, player) -> villager.getGenetics().getGender() == gender ? 1.0F : 0.0F
      );
      register("has_item", (json, name) -> Ingredient.m_43917_(json), item -> (villager, stack, player) -> {
         for (int i = 0; i < villager.m_35311_().m_6643_(); i++) {
            if (item.test(villager.m_35311_().m_8020_(i))) {
               return 1.0F;
            }
         }

         return 0.0F;
      });
      register("min_health", GsonHelper::m_13888_, health -> (villager, stack, player) -> villager.m_21223_() > health ? 1.0F : 0.0F);
      register("is_married", GsonHelper::m_13877_, married -> (villager, stack, player) -> villager.getRelationships().isMarried() == married ? 1.0F : 0.0F);
      register("has_home", GsonHelper::m_13877_, hasHome -> (villager, stack, player) -> villager.getResidency().getHome().isPresent() == hasHome ? 1.0F : 0.0F);
      register(
         "has_village",
         GsonHelper::m_13877_,
         hasVillage -> (villager, stack, player) -> villager.getResidency().getHomeVillage().isPresent() == hasVillage ? 1.0F : 0.0F
      );
      register(
         "min_infection_progress", GsonHelper::m_13888_, progress -> (villager, stack, player) -> villager.getInfectionProgress() > progress ? 1.0F : 0.0F
      );
      register(
         "mood",
         (json, name) -> GsonHelper.m_13805_(json, name).toLowerCase(Locale.ENGLISH),
         mood -> (villager, stack, player) -> villager.getVillagerBrain().getMood().getName().equals(mood) ? 1.0F : 0.0F
      );
      register(
         "personality",
         (json, name) -> Personality.valueOf(GsonHelper.m_13805_(json, name).toUpperCase(Locale.ENGLISH)),
         personality -> (villager, stack, player) -> villager.getVillagerBrain().getPersonality() == personality ? 1.0F : 0.0F
      );
      register(
         "is_pregnant",
         GsonHelper::m_13877_,
         pregnant -> (villager, stack, player) -> villager.getRelationships().getPregnancy().isPregnant() == pregnant ? 1.0F : 0.0F
      );
      register(
         "min_pregnancy_progress",
         GsonHelper::m_13897_,
         progress -> (villager, stack, player) -> villager.getRelationships().getPregnancy().getBabyAge() > progress ? 1.0F : 0.0F
      );
      register(
         "pregnancy_child_gender",
         (json, name) -> Gender.valueOf(GsonHelper.m_13805_(json, name).toUpperCase(Locale.ENGLISH)),
         gender -> (villager, stack, player) -> villager.getRelationships().getPregnancy().getGender() == gender ? 1.0F : 0.0F
      );
      register(
         "current_chore",
         (json, name) -> Chore.valueOf(GsonHelper.m_13805_(json, name).toUpperCase(Locale.ENGLISH)),
         chore -> (villager, stack, player) -> villager.getVillagerBrain().getCurrentJob() == chore ? 1.0F : 0.0F
      );
      register("item", (json, name) -> {
         ResourceLocation id = new ResourceLocation(GsonHelper.m_13805_(json, name));
         Item item = (Item)BuiltInRegistries.f_257033_.m_6612_(id).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + id + "'"));
         return Ingredient.m_43927_(new ItemStack[]{new ItemStack(item)});
      }, ingredient -> (villager, stack, player) -> ingredient.test(stack) ? 1.0F : 0.0F);
      register("tag", (json, name) -> {
         ResourceLocation id = new ResourceLocation(GsonHelper.m_13805_(json, name));
         TagKey<Item> tag = TagKey.m_203882_(Registries.f_256913_, id);
         if (tag == null) {
            throw new JsonSyntaxException("Unknown item tag '" + id + "'");
         } else {
            return Ingredient.m_204132_(tag);
         }
      }, ingredient -> (villager, stack, player) -> ingredient.test(stack) ? 1.0F : 0.0F);
      register(
         "trait",
         (json, name) -> Traits.Trait.valueOf(GsonHelper.m_13805_(json, name).toUpperCase(Locale.ENGLISH)),
         trait -> (villager, stack, player) -> villager.getTraits().hasTrait(trait) ? 1.0F : 0.0F
      );
      register("hearts_min", GsonHelper::m_13897_, hearts -> (villager, stack, player) -> {
         assert player != null;
         int h = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         return h >= hearts ? 1.0F : 0.0F;
      });
      register("hearts_max", GsonHelper::m_13897_, hearts -> (villager, stack, player) -> {
         assert player != null;
         int h = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         return h <= hearts ? 1.0F : 0.0F;
      });
      register("hearts", GsonHelper::m_13918_, json -> (villager, stack, player) -> {
         assert player != null;
         int h = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         return divideAndAdd(json, h);
      });
      register("memory", GsonHelper::m_13918_, json -> (villager, stack, player) -> {
         String id = LongTermMemory.parseId(json, player);
         long ticks = villager.getLongTermMemory().getMemory(id);
         return divideAndAdd(json, ticks);
      });
      register(
         "emeralds",
         GsonHelper::m_13897_,
         amount -> (villager, stack, player) -> player != null && player.m_150109_().m_18947_(Items.f_42616_) >= amount ? 1.0F : 0.0F
      );
      register(
         "village_has_building",
         GsonHelper::m_13805_,
         name -> (villager, stack, player) -> villager.getResidency().getHomeVillage().filter(v -> v.hasBuilding(name)).isPresent() ? 1.0F : 0.0F
      );
      register(
         "rank",
         GsonHelper::m_13805_,
         name -> (villager, stack, player) -> villager.getResidency().getHomeVillage().filter(v -> Tasks.getRank(v, player) == Rank.fromName(name)).isPresent()
            ? 1.0F
            : 0.0F
      );
      register("time_min", GsonHelper::m_13891_, time -> (villager, stack, player) -> villager.m_9236_().m_46468_() % 24000L >= time ? 1.0F : 0.0F);
      register("time_max", GsonHelper::m_13891_, time -> (villager, stack, player) -> villager.m_9236_().m_46468_() % 24000L <= time ? 1.0F : 0.0F);
      register(
         "biome",
         (json, name) -> new ResourceLocation(GsonHelper.m_13805_(json, name)),
         biome -> (villager, stack, player) -> villager.m_9236_()
               .m_204166_(villager.m_20183_())
               .m_203439_()
               .left()
               .filter(b -> b.m_135782_().equals(biome))
               .isPresent()
            ? 1.0F
            : 0.0F
      );
      register("advancement", (json, name) -> new ResourceLocation(GsonHelper.m_13805_(json, name)), id -> (villager, stack, player) -> {
         assert player != null;
         Advancement advancement = Objects.requireNonNull(player.m_20194_()).m_129889_().m_136041_(id);
         return advancement != null && player.m_8960_().m_135996_(advancement).m_8193_() ? 1.0F : 0.0F;
      });
      register("constraints", (json, name) -> Constraint.fromStringList(GsonHelper.m_13805_(json, name)), constraints -> (villager, stack, player) -> {
         Set<Constraint> c = Constraint.allMatching(villager, player);
         return c.containsAll(constraints) ? 1.0F : 0.0F;
      });
   }

   public interface Condition {
      float test(VillagerEntityMCA var1, ItemStack var2, @Nullable ServerPlayer var3);

      default GiftPredicate.Condition and(GiftPredicate.Condition b) {
         GiftPredicate.Condition a = this;
         return (villager, stack, player) -> a.test(villager, stack, player) * b.test(villager, stack, player);
      }
   }

   public interface Factory<T> {
      GiftPredicate.Condition parse(T var1);
   }
}
