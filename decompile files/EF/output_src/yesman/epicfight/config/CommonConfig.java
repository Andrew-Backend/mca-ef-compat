package yesman.epicfight.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@EventBusSubscriber(modid = "epicfight", bus = Bus.MOD)
public class CommonConfig {
   private static final Builder BUILDER = new Builder();
   public static final IntValue SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER = BUILDER.defineInRange("loot.skill_book_mob_drop_chance_modifier", 0, -100, 100);
   public static final IntValue SKILL_BOOK_CHEST_LOOT_MODIFIER = BUILDER.defineInRange("loot.skill_book_chest_drop_chance_modifier", 0, -100, 100);
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   public static int skillBookMobDropChanceModifier;
   public static int skillBookChestLootModifier;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      if (event.getConfig().getType() == Type.COMMON) {
         skillBookMobDropChanceModifier = (Integer)SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER.get();
         skillBookChestLootModifier = (Integer)SKILL_BOOK_CHEST_LOOT_MODIFIER.get();
      }
   }

   static {
      EpicFightGameRules.GAME_RULES.values().forEach(configurableGameRule -> configurableGameRule.defineConfig(BUILDER));
   }
}
