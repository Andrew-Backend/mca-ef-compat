package forge.net.mca.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import forge.net.mca.Config;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.chatAI.ChatAI;
import forge.net.mca.entity.ai.chatAI.OpenAIChatAI;
import forge.net.mca.network.s2c.OpenGuiRequest;
import forge.net.mca.server.ServerInteractionManager;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.level.ServerPlayer;

public class Command {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_(
                                                "mca"
                                             )
                                             .then(register("propose").then(Commands.m_82129_("target", EntityArgument.m_91466_()).executes(Command::propose))))
                                          .then(register("accept").then(Commands.m_82129_("target", EntityArgument.m_91466_()).executes(Command::accept))))
                                       .then(register("proposals", Command::displayProposal)))
                                    .then(register("procreate", Command::procreate)))
                                 .then(register("separate", Command::separate)))
                              .then(register("reject").then(Commands.m_82129_("target", EntityArgument.m_91466_()).executes(Command::reject))))
                           .then(register("editor", Command::editor)))
                        .then(register("destiny", Command::destiny)))
                     .then(register("mail", Command::mail)))
                  .then(register("verify").then(Commands.m_82129_("email", StringArgumentType.greedyString()).executes(Command::verify))))
               .then(
                  register("chatAI")
                     .requires(p -> p.m_6761_(2) || p.m_81377_().m_129792_())
                     .executes(Command::chatAIHelp)
                     .then(Commands.m_82127_("disable").executes(Command::disableChatAI))
                     .then(Commands.m_82127_("default").executes(c -> enableChatAI(c, "default", (new Config()).villagerChatAIEndpoint, "")))
                     .then(Commands.m_82127_("player2").executes(Command::setupPlayer2))
                     .then(
                        register("inworldAI")
                           .requires(p -> p.m_6761_(2) || p.m_81377_().m_129792_())
                           .then(
                              register("keys")
                                 .then(
                                    Commands.m_82129_("api_key", StringArgumentType.string())
                                       .executes(c -> inworldAIKey((String)c.getArgument("api_key", String.class)))
                                 )
                           )
                           .then(
                              register("addCharacter")
                                 .then(
                                    Commands.m_82129_("villager_name", StringArgumentType.string())
                                       .then(
                                          Commands.m_82129_("character_endpoint", StringArgumentType.string())
                                             .executes(
                                                c -> inworldAICharacter(
                                                   c,
                                                   (String)c.getArgument("villager_name", String.class),
                                                   (String)c.getArgument("character_endpoint", String.class)
                                                )
                                             )
                                       )
                                 )
                           )
                     )
                     .then(
                        ((RequiredArgumentBuilder)Commands.m_82129_("model", StringArgumentType.string())
                              .executes(c -> enableChatAI(c, (String)c.getArgument("model", String.class), (new Config()).villagerChatAIEndpoint, "")))
                           .then(
                              ((RequiredArgumentBuilder)Commands.m_82129_("endpoint", StringArgumentType.string())
                                    .executes(
                                       c -> enableChatAI(c, (String)c.getArgument("model", String.class), (String)c.getArgument("endpoint", String.class), "")
                                    ))
                                 .then(
                                    Commands.m_82129_("token", StringArgumentType.string())
                                       .executes(
                                          c -> enableChatAI(
                                             c,
                                             (String)c.getArgument("model", String.class),
                                             (String)c.getArgument("endpoint", String.class),
                                             (String)c.getArgument("token", String.class)
                                          )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               register("tts")
                  .requires(p -> p.m_81377_().m_129792_())
                  .then(Commands.m_82127_("default").executes(ctx -> ttsEnable(ctx, "default")))
                  .then(Commands.m_82127_("elevenlabs").executes(ctx -> ttsEnable(ctx, "elevenlabs")))
                  .then(Commands.m_82127_("realtime").executes(ctx -> ttsEnable(ctx, "realtime")))
                  .then(Commands.m_82127_("disable").executes(Command::ttsDisable))
            )
      );
   }

   private static int chatAIHelp(CommandContext<CommandSourceStack> ctx) {
      return enableChatAI(ctx, (new Config()).villagerChatAIModel, (new Config()).villagerChatAIEndpoint, (new Config()).villagerChatAIToken);
   }

   private static int inworldAIKey(String apiKey) {
      Config.getInstance().inworldAIToken = apiKey;
      Config.getInstance().save();
      return 0;
   }

   private static int inworldAICharacter(CommandContext<CommandSourceStack> ctx, String name, String endpoint) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      Optional<VillagerEntityMCA> optionalVillager = ChatAI.findVillagerInArea(player, name);
      optionalVillager.ifPresent(v -> {
         Config.getInstance().inworldAIResourceNames.put(v.m_20148_(), endpoint);
         ChatAI.clearStrategy(v.m_20148_());
         Config.getInstance().save();
      });
      return 0;
   }

   private static int enableChatAI(CommandContext<CommandSourceStack> ctx, String model, String endpoint, String token) {
      Config.getInstance().enableVillagerChatAI = true;
      Config.getInstance().villagerChatAIModel = model;
      Config.getInstance().villagerChatAIEndpoint = endpoint;
      Config.getInstance().villagerChatAIToken = token;
      Config.getInstance().save();
      if (model.equals("default")) {
         sendMessage(
            ctx,
            Component.m_237115_("mca.ai_help")
               .m_130938_(
                  s -> s.m_131142_(new ClickEvent(Action.OPEN_URL, "https://github.com/Luke100000/minecraft-comes-alive/wiki/GPT3-based-conversations"))
               )
         );
      } else {
         sendMessage(ctx, "command.chat_ai.enabled");
      }

      return 0;
   }

   private static int disableChatAI(CommandContext<CommandSourceStack> c) {
      Config.getInstance().enableVillagerChatAI = false;
      Config.getInstance().save();
      sendMessage(c, "command.chat_ai.disabled");
      return 0;
   }

   private static int setupPlayer2(CommandContext<CommandSourceStack> ctx) {
      Config.getInstance().enableVillagerChatAI = true;
      Config.getInstance().villagerChatAIModel = "player2";
      Config.getInstance().villagerChatAIEndpoint = "http://127.0.0.1:4315/v1/chat/completions";
      Config.getInstance().villagerChatAIToken = "";
      Config.getInstance().villagerChatAIUseTools = true;
      Config.getInstance().enableOnlineTTS = true;
      Config.getInstance().onlineTTSModel = "player2";
      Config.getInstance().save();
      sendMessage(ctx, Component.m_237115_("command.chat_ai.player2").m_130938_(s -> s.m_131142_(new ClickEvent(Action.OPEN_URL, "https://player2.game/"))));
      return 0;
   }

   private static int ttsEnable(CommandContext<CommandSourceStack> ctx, String model) {
      Config.getInstance().enableOnlineTTS = true;
      Config.getInstance().onlineTTSModel = model;
      Config.getInstance().save();
      sendMessage(ctx, Component.m_237115_("command.tts.enabled." + model));
      return 0;
   }

   private static int ttsDisable(CommandContext<CommandSourceStack> ctx) {
      Config.getInstance().enableOnlineTTS = false;
      Config.getInstance().save();
      return 0;
   }

   private static int editor(CommandContext<CommandSourceStack> ctx) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 1;
      } else if (((CommandSourceStack)ctx.getSource()).m_6761_(2) || Config.getInstance().allowFullPlayerEditor) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, player), player);
         return 0;
      } else if (Config.getInstance().allowLimitedPlayerEditor) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.LIMITED_VILLAGER_EDITOR, player), player);
         return 0;
      } else {
         sendMessage(ctx, Component.m_237115_("command.no_permission").m_130940_(ChatFormatting.RED));
         return 1;
      }
   }

   private static int destiny(CommandContext<CommandSourceStack> ctx) {
      if (!((CommandSourceStack)ctx.getSource()).m_6761_(2) && !Config.getInstance().allowDestinyCommandOnce) {
         sendMessage(ctx, Component.m_237115_("command.no_permission").m_130940_(ChatFormatting.RED));
         return 1;
      } else {
         ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
         if ((player == null || PlayerSaveData.get(player).isEntityDataSet()) && !Config.getInstance().allowDestinyCommandMoreThanOnce) {
            sendMessage(ctx, Component.m_237115_("command.only_one_destiny").m_130940_(ChatFormatting.RED));
            return 1;
         } else {
            ServerInteractionManager.launchDestiny(player);
            return 0;
         }
      }
   }

   private static int mail(CommandContext<CommandSourceStack> ctx) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 1;
      }

      PlayerSaveData data = PlayerSaveData.get(player);
      if (data.hasMail()) {
         while (data.hasMail()) {
            player.m_150109_().m_150079_(data.getMail());
         }
      } else {
         sendMessage(ctx, "command.no_mail");
      }

      return 0;
   }

   private static int verify(CommandContext<CommandSourceStack> ctx) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      CompletableFuture.runAsync(
         () -> {
            Map<String, String> params = new HashMap<>();
            params.put("email", StringArgumentType.getString(ctx, "email"));
            assert player != null;
            params.put("player", player.m_7755_().getString());
            String encodedURL = params.keySet()
               .stream()
               .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
               .collect(Collectors.joining("&", Config.getInstance().villagerChatAIEndpoint.replace("v1/mca/chat", "v1/mca/verify") + "?", ""));
            String request = OpenAIChatAI.verify(encodedURL);
            if (request.equals("success")) {
               sendMessage(ctx, Component.m_237115_("command.verify.success").m_130940_(ChatFormatting.GREEN));
            } else if (request.equals("failed")) {
               sendMessage(ctx, Component.m_237115_("command.verify.failed").m_130940_(ChatFormatting.RED));
            } else {
               sendMessage(ctx, Component.m_237115_("command.verify.crashed").m_130940_(ChatFormatting.RED));
            }
         }
      );
      return 0;
   }

   private static int propose(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer target = EntityArgument.m_91474_(ctx, "target");
      ServerInteractionManager.getInstance().sendProposal(((CommandSourceStack)ctx.getSource()).m_230896_(), target);
      return 0;
   }

   private static int accept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer target = EntityArgument.m_91474_(ctx, "target");
      ServerInteractionManager.getInstance().acceptProposal(((CommandSourceStack)ctx.getSource()).m_230896_(), target);
      return 0;
   }

   private static int displayProposal(CommandContext<CommandSourceStack> ctx) {
      ServerInteractionManager.getInstance().listProposals(((CommandSourceStack)ctx.getSource()).m_230896_());
      return 0;
   }

   private static int procreate(CommandContext<CommandSourceStack> ctx) {
      ServerInteractionManager.getInstance().procreate(((CommandSourceStack)ctx.getSource()).m_230896_());
      return 0;
   }

   private static int separate(CommandContext<CommandSourceStack> ctx) {
      ServerInteractionManager.getInstance().endMarriage(((CommandSourceStack)ctx.getSource()).m_230896_());
      return 0;
   }

   private static int reject(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer target = EntityArgument.m_91474_(ctx, "target");
      ServerInteractionManager.getInstance().rejectProposal(((CommandSourceStack)ctx.getSource()).m_230896_(), target);
      return 0;
   }

   private static ArgumentBuilder<CommandSourceStack, ?> register(String name, com.mojang.brigadier.Command<CommandSourceStack> cmd) {
      return ((LiteralArgumentBuilder)Commands.m_82127_(name).requires(cs -> cs.m_6761_(0))).executes(cmd);
   }

   private static ArgumentBuilder<CommandSourceStack, ?> register(String name) {
      return Commands.m_82127_(name).requires(cs -> cs.m_6761_(0));
   }

   private static void sendMessage(CommandContext<CommandSourceStack> ctx, String message) {
      sendMessage(ctx, Component.m_237115_(message));
   }

   private static void sendMessage(CommandContext<CommandSourceStack> ctx, Component message) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player != null) {
         player.m_213846_(message);
      }
   }
}
