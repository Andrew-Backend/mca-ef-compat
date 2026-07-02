package yesman.epicfight.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerStaminaCommand {
   private static final SimpleCommandExceptionType ERROR_MODIFYING_FAILED = new SimpleCommandExceptionType(
      Component.m_237115_("commands.epicfight.stamina.success.failed")
   );

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> builder = (LiteralArgumentBuilder<CommandSourceStack>)Commands.m_82127_("stamina")
         .requires(commandSourceStack -> commandSourceStack.m_6761_(2));
      builder.then(
         Commands.m_82127_("get")
            .then(
               Commands.m_82129_("target", EntityArgument.m_91466_())
                  .executes(command -> getStamina((CommandSourceStack)command.getSource(), EntityArgument.m_91474_(command, "target")))
            )
      );

      for (PlayerStaminaCommand.Operation operation : PlayerStaminaCommand.Operation.values()) {
         builder.then(
            ((LiteralArgumentBuilder)Commands.m_82127_(operation.name().toLowerCase(Locale.ROOT))
                  .then(
                     Commands.m_82129_("value", DoubleArgumentType.doubleArg())
                        .executes(
                           command -> setStamina(
                              command,
                              Collections.singleton(((CommandSourceStack)command.getSource()).m_81375_()),
                              operation,
                              DoubleArgumentType.getDouble(command, "value")
                           )
                        )
                  ))
               .then(
                  Commands.m_82129_("target", EntityArgument.m_91470_())
                     .then(
                        Commands.m_82129_("value", DoubleArgumentType.doubleArg())
                           .executes(
                              command -> setStamina(
                                 command, EntityArgument.m_91477_(command, "target"), operation, DoubleArgumentType.getDouble(command, "value")
                              )
                           )
                     )
               )
         );
      }

      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("epicfight").then(builder));
   }

   private static int getStamina(CommandSourceStack command, ServerPlayer serverplayer) {
      ServerPlayerPatch serverplayerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
      if (serverplayerpatch == null) {
         command.m_81352_(Component.m_237115_("commands.epicfight.stamina.failed.no_stamina"));
         return 0;
      } else {
         double stamina = serverplayerpatch.getStamina();
         command.m_288197_(
            () -> Component.m_237110_("commands.epicfight.stamina.value.get.success", new Object[]{serverplayer.m_7755_(), ItemStack.f_41584_.format(stamina)}),
            false
         );
         return (int)stamina;
      }
   }

   private static int setStamina(
      CommandContext<CommandSourceStack> command, Collection<ServerPlayer> players, PlayerStaminaCommand.Operation operation, double value
   ) {
      int i = 0;
      double returnVal = 0.0;

      for (ServerPlayer serverplayer : players) {
         ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
         if (playerpatch != null) {
            double stamina = operation.func.apply((double)playerpatch.getStamina(), value);
            playerpatch.resetActionTick();
            playerpatch.setStamina((float)stamina);
            returnVal = playerpatch.getStamina();
            i++;
         }
      }

      if (i == 0) {
         ERROR_MODIFYING_FAILED.create();
      } else if (i == 1) {
         ((CommandSourceStack)command.getSource())
            .m_288197_(
               wrap(
                  Component.m_237110_(
                     "commands.epicfight.stamina.success.self", new Object[]{players.iterator().next().m_5446_(), ItemStack.f_41584_.format(returnVal)}
                  )
               ),
               true
            );
      } else {
         for (ServerPlayer serverplayer : players) {
            if (((CommandSourceStack)command.getSource()).m_81372_().m_46469_().m_46207_(GameRules.f_46144_)) {
               serverplayer.m_213846_(Component.m_237110_("commands.epicfight.stamina.success.other", new Object[]{String.valueOf(i)}));
            }
         }

         ((CommandSourceStack)command.getSource())
            .m_288197_(wrap(Component.m_237110_("commands.epicfight.stamina.success.other", new Object[]{String.valueOf(i)})), true);
      }

      return i;
   }

   private static <T> Supplier<T> wrap(T value) {
      return () -> value;
   }

   private enum Operation {
      ADD((value, operand) -> value + operand),
      SUBTRACT((value, operand) -> value - operand),
      SET((value, operand) -> operand);

      BiFunction<Double, Double, Double> func;

      Operation(BiFunction<Double, Double, Double> func) {
         this.func = func;
      }
   }
}
