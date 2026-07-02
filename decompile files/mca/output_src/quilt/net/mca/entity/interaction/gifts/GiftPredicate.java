package quilt.net.mca.entity.interaction.gifts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import net.minecraft.class_161;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1856;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_3532;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Chore;
import quilt.net.mca.entity.ai.LongTermMemory;
import quilt.net.mca.entity.ai.Traits;
import quilt.net.mca.entity.ai.relationship.AgeState;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.entity.ai.relationship.Personality;
import quilt.net.mca.entity.interaction.Constraint;
import quilt.net.mca.resources.Rank;
import quilt.net.mca.resources.Tasks;

public class GiftPredicate {
   public static final Map<String, GiftPredicate.Factory<JsonElement>> CONDITION_TYPES = new HashMap<>();
   private final int satisfactionBoost;
   @Nullable
   private final GiftPredicate.Condition condition;
   List<String> conditionKeys;

   public static float divideAndAdd(JsonObject json, long value) {
      return class_3532.method_15363(
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
            satisfaction = class_3518.method_15257(entry.getValue(), entry.getKey());
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

   public float test(VillagerEntityMCA recipient, class_1799 stack, @Nullable class_3222 player) {
      return this.condition != null ? this.condition.test(recipient, stack, player) : 0.0F;
   }

   public int getSatisfactionFor(VillagerEntityMCA recipient, class_1799 stack, @Nullable class_3222 player) {
      return (int)(this.test(recipient, stack, player) * this.satisfactionBoost);
   }

   public List<String> getConditionKeys() {
      return this.conditionKeys;
   }

   static {
      register(
         "profession",
         (json, name) -> new class_2960(class_3518.method_15287(json, name)),
         profession -> (villager, stack, player) -> class_7923.field_41195.method_10221(villager.getProfession()).equals(profession) ? 1.0F : 0.0F
      );
      register(
         "age_group",
         (json, name) -> AgeState.valueOf(class_3518.method_15287(json, name).toUpperCase(Locale.ENGLISH)),
         group -> (villager, stack, player) -> villager.getAgeState() == group ? 1.0F : 0.0F
      );
      register(
         "gender",
         (json, name) -> Gender.valueOf(class_3518.method_15287(json, name).toUpperCase(Locale.ENGLISH)),
         gender -> (villager, stack, player) -> villager.getGenetics().getGender() == gender ? 1.0F : 0.0F
      );
      register("has_item", (json, name) -> class_1856.method_52177(json), item -> (villager, stack, player) -> {
         for (int i = 0; i < villager.method_35199().method_5439(); i++) {
            if (item.method_8093(villager.method_35199().method_5438(i))) {
               return 1.0F;
            }
         }

         return 0.0F;
      });
      register("min_health", class_3518::method_15269, health -> (villager, stack, player) -> villager.method_6032() > health ? 1.0F : 0.0F);
      register("is_married", class_3518::method_15268, married -> (villager, stack, player) -> villager.getRelationships().isMarried() == married ? 1.0F : 0.0F);
      register(
         "has_home", class_3518::method_15268, hasHome -> (villager, stack, player) -> villager.getResidency().getHome().isPresent() == hasHome ? 1.0F : 0.0F
      );
      register(
         "has_village",
         class_3518::method_15268,
         hasVillage -> (villager, stack, player) -> villager.getResidency().getHomeVillage().isPresent() == hasVillage ? 1.0F : 0.0F
      );
      register(
         "min_infection_progress", class_3518::method_15269, progress -> (villager, stack, player) -> villager.getInfectionProgress() > progress ? 1.0F : 0.0F
      );
      register(
         "mood",
         (json, name) -> class_3518.method_15287(json, name).toLowerCase(Locale.ENGLISH),
         mood -> (villager, stack, player) -> villager.getVillagerBrain().getMood().getName().equals(mood) ? 1.0F : 0.0F
      );
      register(
         "personality",
         (json, name) -> Personality.valueOf(class_3518.method_15287(json, name).toUpperCase(Locale.ENGLISH)),
         personality -> (villager, stack, player) -> villager.getVillagerBrain().getPersonality() == personality ? 1.0F : 0.0F
      );
      register(
         "is_pregnant",
         class_3518::method_15268,
         pregnant -> (villager, stack, player) -> villager.getRelationships().getPregnancy().isPregnant() == pregnant ? 1.0F : 0.0F
      );
      register(
         "min_pregnancy_progress",
         class_3518::method_15257,
         progress -> (villager, stack, player) -> villager.getRelationships().getPregnancy().getBabyAge() > progress ? 1.0F : 0.0F
      );
      register(
         "pregnancy_child_gender",
         (json, name) -> Gender.valueOf(class_3518.method_15287(json, name).toUpperCase(Locale.ENGLISH)),
         gender -> (villager, stack, player) -> villager.getRelationships().getPregnancy().getGender() == gender ? 1.0F : 0.0F
      );
      register(
         "current_chore",
         (json, name) -> Chore.valueOf(class_3518.method_15287(json, name).toUpperCase(Locale.ENGLISH)),
         chore -> (villager, stack, player) -> villager.getVillagerBrain().getCurrentJob() == chore ? 1.0F : 0.0F
      );
      register("item", (json, name) -> {
         class_2960 id = new class_2960(class_3518.method_15287(json, name));
         class_1792 item = (class_1792)class_7923.field_41178.method_17966(id).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + id + "'"));
         return class_1856.method_8101(new class_1799[]{new class_1799(item)});
      }, ingredient -> (villager, stack, player) -> ingredient.method_8093(stack) ? 1.0F : 0.0F);
      register("tag", (json, name) -> {
         class_2960 id = new class_2960(class_3518.method_15287(json, name));
         class_6862<class_1792> tag = class_6862.method_40092(class_7924.field_41197, id);
         if (tag == null) {
            throw new JsonSyntaxException("Unknown item tag '" + id + "'");
         } else {
            return class_1856.method_8106(tag);
         }
      }, ingredient -> (villager, stack, player) -> ingredient.method_8093(stack) ? 1.0F : 0.0F);
      register(
         "trait",
         (json, name) -> Traits.Trait.valueOf(class_3518.method_15287(json, name).toUpperCase(Locale.ENGLISH)),
         trait -> (villager, stack, player) -> villager.getTraits().hasTrait(trait) ? 1.0F : 0.0F
      );
      register("hearts_min", class_3518::method_15257, hearts -> (villager, stack, player) -> {
         assert player != null;
         int h = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         return h >= hearts ? 1.0F : 0.0F;
      });
      register("hearts_max", class_3518::method_15257, hearts -> (villager, stack, player) -> {
         assert player != null;
         int h = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         return h <= hearts ? 1.0F : 0.0F;
      });
      register("hearts", class_3518::method_15295, json -> (villager, stack, player) -> {
         assert player != null;
         int h = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         return divideAndAdd(json, h);
      });
      register("memory", class_3518::method_15295, json -> (villager, stack, player) -> {
         String id = LongTermMemory.parseId(json, player);
         long ticks = villager.getLongTermMemory().getMemory(id);
         return divideAndAdd(json, ticks);
      });
      register(
         "emeralds",
         class_3518::method_15257,
         amount -> (villager, stack, player) -> player != null && player.method_31548().method_18861(class_1802.field_8687) >= amount ? 1.0F : 0.0F
      );
      register(
         "village_has_building",
         class_3518::method_15287,
         name -> (villager, stack, player) -> villager.getResidency().getHomeVillage().filter(v -> v.hasBuilding(name)).isPresent() ? 1.0F : 0.0F
      );
      register(
         "rank",
         class_3518::method_15287,
         name -> (villager, stack, player) -> villager.getResidency().getHomeVillage().filter(v -> Tasks.getRank(v, player) == Rank.fromName(name)).isPresent()
            ? 1.0F
            : 0.0F
      );
      register("time_min", class_3518::method_15263, time -> (villager, stack, player) -> villager.method_37908().method_8532() % 24000L >= time ? 1.0F : 0.0F);
      register("time_max", class_3518::method_15263, time -> (villager, stack, player) -> villager.method_37908().method_8532() % 24000L <= time ? 1.0F : 0.0F);
      register(
         "biome",
         (json, name) -> new class_2960(class_3518.method_15287(json, name)),
         biome -> (villager, stack, player) -> villager.method_37908()
               .method_23753(villager.method_24515())
               .method_40229()
               .left()
               .filter(b -> b.method_29177().equals(biome))
               .isPresent()
            ? 1.0F
            : 0.0F
      );
      register("advancement", (json, name) -> new class_2960(class_3518.method_15287(json, name)), id -> (villager, stack, player) -> {
         assert player != null;
         class_161 advancement = Objects.requireNonNull(player.method_5682()).method_3851().method_12896(id);
         return advancement != null && player.method_14236().method_12882(advancement).method_740() ? 1.0F : 0.0F;
      });
      register("constraints", (json, name) -> Constraint.fromStringList(class_3518.method_15287(json, name)), constraints -> (villager, stack, player) -> {
         Set<Constraint> c = Constraint.allMatching(villager, player);
         return c.containsAll(constraints) ? 1.0F : 0.0F;
      });
   }

   public interface Condition {
      float test(VillagerEntityMCA var1, class_1799 var2, @Nullable class_3222 var3);

      default GiftPredicate.Condition and(GiftPredicate.Condition b) {
         GiftPredicate.Condition a = this;
         return (villager, stack, player) -> a.test(villager, stack, player) * b.test(villager, stack, player);
      }
   }

   public interface Factory<T> {
      GiftPredicate.Condition parse(T var1);
   }
}
