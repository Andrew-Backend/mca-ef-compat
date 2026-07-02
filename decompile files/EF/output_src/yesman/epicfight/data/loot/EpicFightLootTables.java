package yesman.epicfight.data.loot;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.item.EpicFightItems;

@EventBusSubscriber(modid = "epicfight")
public class EpicFightLootTables {
   public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(
      Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "epicfight"
   );
   public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SKILLS = LOOT_MODIFIERS.register(
      "skillbook_loot_table_modifier", SkillBookLootModifier.SKILL_CODEC
   );
   public static final LootItemFunctionType SET_SKILLBOOK_SKILL = new LootItemFunctionType(new SetSkillFunction.Serializer());

   public static void registerLootItemFunctionType() {
      Registry.m_122965_(BuiltInRegistries.f_256753_, EpicFightMod.identifier("set_skill"), SET_SKILLBOOK_SKILL);
   }

   @SubscribeEvent
   public static void modifyVanillaLootPools(LootTableLoadEvent event) {
      int modifier = CommonConfig.skillBookChestLootModifier;
      int dropChance = 100 + modifier;
      int antiDropChance = 100 - modifier;
      float dropChanceModifier = (float)dropChance / (antiDropChance + dropChance);
      if (event.getName().equals(BuiltInLootTables.f_78764_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 2.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll",
                              "epicfight:phantom_ascent"
                           )
                        )
                        .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier))
                  )
                  .m_79082_()
            );
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.25F))
                  .m_79076_(LootItem.m_79579_((ItemLike)EpicFightItems.UCHIGATANA.get()))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78686_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 2.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll",
                              "epicfight:phantom_ascent"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier))
                  .m_79082_()
            );
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(0.25F))
                  .m_79076_(LootItem.m_79579_((ItemLike)EpicFightItems.UCHIGATANA.get()))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78742_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 3.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78759_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 3.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78696_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 3.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78691_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 3.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll",
                              "epicfight:phantom_ascent"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78693_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 2.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78761_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 5.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:hypervitality",
                              "epicfight:forbidden_strength",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll",
                              "epicfight:phantom_ascent"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78689_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 5.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:hypervitality",
                              "epicfight:forbidden_strength",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll",
                              "epicfight:phantom_ascent"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }

      if (event.getName().equals(BuiltInLootTables.f_78698_)) {
         event.getTable()
            .addPool(
               LootPool.m_79043_()
                  .m_165133_(UniformGenerator.m_165780_(1.0F, 4.0F))
                  .m_79076_(
                     LootItem.m_79579_((ItemLike)EpicFightItems.SKILLBOOK.get())
                        .m_79078_(
                           SetSkillFunction.builder(
                              "epicfight:berserker",
                              "epicfight:stamina_pillager",
                              "epicfight:technician",
                              "epicfight:swordmaster",
                              "epicfight:hypervitality",
                              "epicfight:forbidden_strength",
                              "epicfight:guard",
                              "epicfight:step",
                              "epicfight:roll",
                              "epicfight:phantom_ascent"
                           )
                        )
                  )
                  .m_79080_(LootItemRandomChanceCondition.m_81927_(dropChanceModifier * 0.3F))
                  .m_79082_()
            );
      }
   }
}
