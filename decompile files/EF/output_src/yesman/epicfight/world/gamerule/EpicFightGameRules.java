package yesman.epicfight.world.gamerule;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;
import net.minecraft.world.level.GameRules.Type;
import net.minecraft.world.level.GameRules.Value;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeGamerule;

public class EpicFightGameRules {
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> GLOBAL_STUN = create(
      "globalStun", Category.MOBS, configBuilder -> configBuilder.define("default_gamerule.globalStun", true), EpicFightGameRules.RuleType.BOOLEAN, false
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> KEEP_SKILLS = create(
      "keepSkills", Category.PLAYER, configBuilder -> configBuilder.define("default_gamerule.keepSkills", true), EpicFightGameRules.RuleType.BOOLEAN, false
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> HAS_FALL_ANIMATION = create(
      "hasFallAnimation",
      Category.PLAYER,
      configBuilder -> configBuilder.define("default_gamerule.hasFallAnimation", true),
      EpicFightGameRules.RuleType.BOOLEAN,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> DISABLE_ENTITY_UI = create(
      "disableEntityUI",
      Category.MISC,
      configBuilder -> configBuilder.define("default_gamerule.disapleEntityUI", false),
      EpicFightGameRules.RuleType.BOOLEAN,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> CAN_SWITCH_PLAYER_MODE = create(
      "canSwitchPlayerMode",
      Category.PLAYER,
      configBuilder -> configBuilder.define("default_gamerule.canSwitchPlayerMode", true),
      EpicFightGameRules.RuleType.BOOLEAN,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> STIFF_COMBO_ATTACKS = create(
      "stiffComboAttacks",
      Category.PLAYER,
      configBuilder -> configBuilder.define("default_gamerule.stiffComboAttacks", true),
      EpicFightGameRules.RuleType.BOOLEAN,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> NO_MOBS_IN_BOSSFIGHT = create(
      "noMobsInBossfight",
      Category.SPAWNING,
      configBuilder -> configBuilder.define("default_gamerule.noMobsInBossfight", true),
      EpicFightGameRules.RuleType.BOOLEAN,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Integer, IntValue, IntegerValue> INITIAL_PLAYER_MODE = create(
      "initialMode",
      Category.PLAYER,
      configBuilder -> configBuilder.comment("0 = vanilla, 1 = epicfight").defineInRange("default_gamerule.initialMode", 1, 0, 1),
      EpicFightGameRules.RuleType.INTEGER,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Integer, IntValue, IntegerValue> WEIGHT_PENALTY = create(
      "weightPenalty",
      Category.PLAYER,
      configBuilder -> configBuilder.defineInRange("default_gamerule.weightPenalty", 100, 0, 100),
      EpicFightGameRules.RuleType.INTEGER,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> EPIC_DROP = create(
      "epicDrop", Category.DROPS, configBuilder -> configBuilder.define("default_gamerule.epicDrop", false), EpicFightGameRules.RuleType.BOOLEAN, true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Integer, IntValue, IntegerValue> SKILL_REPLACE_COOLDOWN = create(
      "skillReplaceCooldown",
      Category.PLAYER,
      configBuilder -> configBuilder.defineInRange("default_gamerule.skillReplaceCooldown", 6000, 0, Integer.MAX_VALUE),
      EpicFightGameRules.RuleType.INTEGER,
      true
   );
   public static final EpicFightGameRules.ConfigurableGameRule<Boolean, BooleanValue, net.minecraft.world.level.GameRules.BooleanValue> ALLOW_VANILLA_MELEE = create(
      "allowVanillaMelee",
      Category.PLAYER,
      configBuilder -> configBuilder.define("default_gamerule.allow_vanilla_melee", true),
      EpicFightGameRules.RuleType.BOOLEAN,
      true
   );
   public static final Map<String, EpicFightGameRules.ConfigurableGameRule<?, ?, ?>> GAME_RULES = ImmutableMap.builder()
      .put("globalStun", GLOBAL_STUN)
      .put("keepSkills", KEEP_SKILLS)
      .put("hasFallAnimation", HAS_FALL_ANIMATION)
      .put("disableEntityUI", DISABLE_ENTITY_UI)
      .put("canSwitchPlayerMode", CAN_SWITCH_PLAYER_MODE)
      .put("stiffComboAttacks", STIFF_COMBO_ATTACKS)
      .put("noMobsInBossfight", NO_MOBS_IN_BOSSFIGHT)
      .put("initialMode", INITIAL_PLAYER_MODE)
      .put("weightPenalty", WEIGHT_PENALTY)
      .put("epicDrop", EPIC_DROP)
      .put("skillReplaceCooldown", SKILL_REPLACE_COOLDOWN)
      .put("allowVanillaMelee", ALLOW_VANILLA_MELEE)
      .build();

   public static void registerGameRules() {
      GAME_RULES.values().forEach(gamerule -> gamerule.registerGameRule());
   }

   public static <Type, Config extends ConfigValue<Type>, RuleValue extends Value<RuleValue>> EpicFightGameRules.ConfigurableGameRule<Type, Config, RuleValue> create(
      String ruleName,
      Category ruleCategory,
      Function<Builder, Config> configDefinition,
      EpicFightGameRules.RuleType<Type, RuleValue> ruleType,
      boolean synchronize
   ) {
      return new EpicFightGameRules.ConfigurableGameRule<>(ruleName, ruleCategory, configDefinition, ruleType, synchronize);
   }

   public static class ConfigurableGameRule<Type, Config extends ConfigValue<Type>, RuleValue extends Value<RuleValue>> {
      final String ruleName;
      final Category ruleCategory;
      final Function<Builder, Config> configDefinition;
      final EpicFightGameRules.RuleType<Type, RuleValue> ruleType;
      final boolean synchronize;
      Config configValueHolder;
      Key<RuleValue> gameRuleKey;

      private ConfigurableGameRule(
         String ruleName,
         Category ruleCategory,
         Function<Builder, Config> configDefinition,
         EpicFightGameRules.RuleType<Type, RuleValue> ruleType,
         boolean synchronize
      ) {
         this.ruleName = ruleName;
         this.ruleCategory = ruleCategory;
         this.configDefinition = configDefinition;
         this.ruleType = ruleType;
         this.synchronize = synchronize;
      }

      public void registerGameRule() {
         if (this.synchronize) {
            this.gameRuleKey = GameRules.m_46189_(
               this.ruleName,
               this.ruleCategory,
               this.ruleType
                  .valueCreator
                  .apply(
                     (Type)this.configValueHolder.get(),
                     (server, value) -> EpicFightNetworkManager.sendToAll(new SPChangeGamerule<>(this, (Type)this.ruleType.getRule.apply(value)))
                  )
            );
         } else {
            this.gameRuleKey = GameRules.m_46189_(
               this.ruleName, this.ruleCategory, (Type)this.ruleType.valueCreatorUnsynchronized.apply(this.configValueHolder.get())
            );
         }
      }

      public boolean shouldSync() {
         return this.synchronize;
      }

      public SPChangeGamerule<?, ?, ?> getSyncPacket(ServerPlayer player) {
         return new SPChangeGamerule<>(this, this.getRuleValue(player.m_9236_()));
      }

      public void defineConfig(Builder configBuilder) {
         this.configValueHolder = (Config)this.configDefinition.apply(configBuilder);
      }

      public String getRuleName() {
         return this.ruleName;
      }

      public EpicFightGameRules.RuleType<Type, RuleValue> getRuleType() {
         return this.ruleType;
      }

      public Key<RuleValue> getRuleKey() {
         return this.gameRuleKey;
      }

      public Config getConfigHolder() {
         return this.configValueHolder;
      }

      public Type getRuleValue(Level level) {
         return (Type)this.ruleType.getRule.apply(level.m_46469_().m_46170_(this.gameRuleKey));
      }

      public void setRuleValue(Level level, Type value) {
         this.ruleType.setRule.accept((RuleValue)level.m_46469_().m_46170_(this.gameRuleKey), value);
      }
   }

   public record RuleType<Type, RuleValue extends Value<RuleValue>>(
      BiFunction<Type, BiConsumer<MinecraftServer, RuleValue>, Type<RuleValue>> valueCreator,
      Function<Type, Type<RuleValue>> valueCreatorUnsynchronized,
      Function<RuleValue, Type> getRule,
      BiConsumer<RuleValue, Type> setRule,
      PacketBufferCodec<Type> bufferCodec
   ) {
      private static final EpicFightGameRules.RuleType<Boolean, net.minecraft.world.level.GameRules.BooleanValue> BOOLEAN = new EpicFightGameRules.RuleType<>(
         net.minecraft.world.level.GameRules.BooleanValue::m_46252_,
         net.minecraft.world.level.GameRules.BooleanValue::m_46250_,
         net.minecraft.world.level.GameRules.BooleanValue::m_46223_,
         (ruleValue, value) -> ruleValue.m_46246_(value, null),
         PacketBufferCodec.BOOLEAN
      );
      private static final EpicFightGameRules.RuleType<Integer, IntegerValue> INTEGER = new EpicFightGameRules.RuleType<>(
         IntegerValue::m_46294_,
         IntegerValue::m_46312_,
         IntegerValue::m_46288_,
         (ruleValue, value) -> ruleValue.m_46314_(value.toString()),
         PacketBufferCodec.INTEGER
      );
   }
}
