package yesman.epicfight.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerModeCommand {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> builder = (LiteralArgumentBuilder<CommandSourceStack>)Commands.m_82127_("mode")
         .requires(commandSourceStack -> commandSourceStack.m_6761_(2));

      for (PlayerPatch.PlayerMode mode : PlayerPatch.PlayerMode.values()) {
         builder.then(
            ((LiteralArgumentBuilder)Commands.m_82127_(mode.name().toLowerCase(Locale.ROOT))
                  .executes(command -> setMode(command, Collections.singleton(((CommandSourceStack)command.getSource()).m_81375_()), mode)))
               .then(
                  Commands.m_82129_("target", EntityArgument.m_91470_())
                     .executes(p_137728_ -> setMode(p_137728_, EntityArgument.m_91477_(p_137728_, "target"), mode))
               )
         );
      }

      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("epicfight").then(builder));
   }

   private static int setMode(CommandContext<CommandSourceStack> command, Collection<ServerPlayer> players, PlayerPatch.PlayerMode playerMode) {
      int i = 0;

      for (ServerPlayer serverplayer : players) {
         ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
         if (playerpatch != null) {
            logGamemodeChange((CommandSourceStack)command.getSource(), serverplayer, playerMode);
            playerpatch.toMode(playerMode, true);
            i++;
         }
      }

      return i;
   }

   private static void logGamemodeChange(CommandSourceStack command, ServerPlayer serverPlayer, PlayerPatch.PlayerMode playerMode) {
      Component component = Component.m_237115_("gameMode.epicfight." + playerMode.name().toLowerCase(Locale.ROOT));
      if (command.m_81373_() == serverPlayer) {
         command.m_288197_(() -> Component.m_237110_("commands.gamemode.success.self", new Object[]{component}), true);
      } else {
         if (command.m_81372_().m_46469_().m_46207_(GameRules.f_46144_)) {
            serverPlayer.m_213846_(Component.m_237110_("gameMode.changed", new Object[]{component}));
         }

         command.m_288197_(() -> Component.m_237110_("commands.gamemode.success.other", new Object[]{serverPlayer.m_5446_(), component}), true);
      }
   }
}
