package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules.Value;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class SPChangeGamerule<Type, Config extends ConfigValue<Type>, RuleValue extends Value<RuleValue>> {
   private final EpicFightGameRules.ConfigurableGameRule<Type, Config, RuleValue> gamerule;
   private final Type value;

   public SPChangeGamerule() {
      this.gamerule = null;
      this.value = null;
   }

   public SPChangeGamerule(EpicFightGameRules.ConfigurableGameRule<Type, Config, RuleValue> gamerule, Type object) {
      this.gamerule = gamerule;
      this.value = object;
   }

   public static <Type, Config extends ConfigValue<Type>, RuleValue extends Value<RuleValue>> SPChangeGamerule<Type, Config, RuleValue> fromBytes(
      FriendlyByteBuf buf
   ) {
      EpicFightGameRules.ConfigurableGameRule<Type, Config, RuleValue> gamerule = (EpicFightGameRules.ConfigurableGameRule<Type, Config, RuleValue>)EpicFightGameRules.GAME_RULES
         .get(buf.m_130277_());
      Type value = gamerule.getRuleType().bufferCodec().decode(buf);
      return new SPChangeGamerule<>(gamerule, value);
   }

   public static <Type, Config extends ConfigValue<Type>, RuleValue extends Value<RuleValue>> void toBytes(
      SPChangeGamerule<Type, Config, RuleValue> msg, FriendlyByteBuf buf
   ) {
      buf.m_130070_(msg.gamerule.getRuleName());
      msg.gamerule.getRuleType().bufferCodec().encode(msg.value, buf);
   }

   public static <Type, Config extends ConfigValue<Type>, RuleValue extends Value<RuleValue>> void handle(
      SPChangeGamerule<Type, Config, RuleValue> msg, Supplier<Context> ctx
   ) {
      ctx.get().enqueueWork(() -> {
         RuleValue ruleValue = (RuleValue)Minecraft.m_91087_().f_91073_.m_46469_().m_46170_(msg.gamerule.getRuleKey());
         msg.gamerule.getRuleType().setRule().accept(ruleValue, msg.value);
      });
      ctx.get().setPacketHandled(true);
   }
}
