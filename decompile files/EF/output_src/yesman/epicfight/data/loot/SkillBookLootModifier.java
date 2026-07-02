package yesman.epicfight.data.loot;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.ModLoader;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.forgeevent.SkillLootTableRegistryEvent;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.world.item.EpicFightItems;

public class SkillBookLootModifier extends LootModifier {
   public static final Supplier<Codec<SkillBookLootModifier>> SKILL_CODEC = Suppliers.memoize(
      () -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SkillBookLootModifier::new))
   );
   public static final Map<EntityType<?>, LootTable> SKILL_LOOT_TABLE = Maps.newHashMap();

   public SkillBookLootModifier(LootItemCondition[] lootItemConditions) {
      super(lootItemConditions);
   }

   public static void createSkillLootTable() {
      Map<EntityType<?>, Builder> builders = Maps.newHashMap();
      SkillLootTableRegistryEvent lootTableRegistryEvent = new SkillLootTableRegistryEvent(builders);
      int modifier = CommonConfig.skillBookMobDropChanceModifier;
      int dropChance = 100 + modifier;
      int antiDropChance = 100 - modifier;
      float dropChanceModifier = antiDropChance == 0 ? Float.MAX_VALUE : (float)dropChance / antiDropChance;
      lootTableRegistryEvent.put(
         EntityType.f_20501_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              1.0F,
                              "epicfight:berserker",
                              1.0F,
                              "epicfight:stamina_pillager",
                              1.0F,
                              "epicfight:roll",
                              1.0F,
                              "epicfight:step",
                              1.0F,
                              "epicfight:guard",
                              0.5F,
                              "epicfight:endurance"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20458_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              1.0F,
                              "epicfight:berserker",
                              1.0F,
                              "epicfight:stamina_pillager",
                              1.0F,
                              "epicfight:roll",
                              1.0F,
                              "epicfight:step",
                              1.0F,
                              "epicfight:guard",
                              0.5F,
                              "epicfight:endurance"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20562_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              1.0F,
                              "epicfight:berserker",
                              1.0F,
                              "epicfight:stamina_pillager",
                              1.0F,
                              "epicfight:roll",
                              1.0F,
                              "epicfight:step",
                              1.0F,
                              "epicfight:guard",
                              0.5F,
                              "epicfight:endurance"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20524_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              1.0F,
                              "epicfight:swordmaster",
                              1.0F,
                              "epicfight:technician",
                              1.0F,
                              "epicfight:roll",
                              1.0F,
                              "epicfight:step",
                              1.0F,
                              "epicfight:guard",
                              0.5F,
                              "epicfight:emergency_escape"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20481_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              1.0F,
                              "epicfight:swordmaster",
                              1.0F,
                              "epicfight:technician",
                              1.0F,
                              "epicfight:roll",
                              1.0F,
                              "epicfight:step",
                              1.0F,
                              "epicfight:guard",
                              0.5F,
                              "epicfight:emergency_escape"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20479_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:roll", "epicfight:step", "epicfight:guard"))
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20554_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:roll", "epicfight:step", "epicfight:guard"))
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20558_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:hypervitality", "epicfight:impact_guard"))
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20566_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:hypervitality",
                              "epicfight:forbidden_strength",
                              "epicfight:endurance",
                              "epicfight:emergency_escape",
                              "epicfight:parrying",
                              "epicfight:impact_guard"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20493_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder("epicfight:hypervitality", "epicfight:berserker", "epicfight:guard", "epicfight:step", "epicfight:roll")
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20513_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:hypervitality", "epicfight:stamina_pillager", "epicfight:guard", "epicfight:step", "epicfight:roll"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20495_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:forbidden_strength", "epicfight:berserker"))
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20568_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:parrying", "epicfight:impact_guard"))
                  )
            )
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.1F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:death_harvest", "epicfight:emergency_escape"))
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20511_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:swordmaster", "epicfight:stamina_pillager", "epicfight:guard", "epicfight:step", "epicfight:roll"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20512_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(SetSkillFunction.builder("epicfight:hypervitality", "epicfight:parrying", "epicfight:endurance", "epicfight:impact_guard"))
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20531_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder("epicfight:berserker", "epicfight:stamina_pillager", "epicfight:guard", "epicfight:step", "epicfight:roll")
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20497_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.025F * dropChanceModifier))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              1.0F,
                              "epicfight:swordmaster",
                              1.0F,
                              "epicfight:stamina_pillager",
                              1.0F,
                              "epicfight:guard",
                              1.0F,
                              "epicfight:step",
                              1.0F,
                              "epicfight:roll",
                              0.75F,
                              "epicfight:death_harvest"
                           )
                        )
                  )
            )
      );
      lootTableRegistryEvent.put(
         EntityType.f_20496_,
         LootTable.m_79147_()
            .m_79161_(
               LootPool.m_79043_()
                  .m_165133_(ConstantValue.m_165692_(1.0F))
                  .m_79076_(LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get()).m_79078_(SetSkillFunction.builder("epicfight:death_harvest")))
            )
      );
      ModLoader.get().postEvent(lootTableRegistryEvent);
      SKILL_LOOT_TABLE.clear();
      builders.forEach((k, v) -> SKILL_LOOT_TABLE.put((EntityType<?>)k, v.m_79167_()));
   }

   @NotNull
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
      Entity entity = (Entity)context.m_78953_(LootContextParams.f_81455_);
      if (entity != null && SKILL_LOOT_TABLE.containsKey(entity.m_6095_())) {
         SKILL_LOOT_TABLE.get(entity.m_6095_()).m_79131_(context, generatedLoot::add);
      }

      return generatedLoot;
   }

   public Codec<? extends IGlobalLootModifier> codec() {
      return SKILL_CODEC.get();
   }
}
