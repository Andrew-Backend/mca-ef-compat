package quilt.net.mca.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.class_124;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2186;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_2558.class_2559;
import quilt.net.mca.Config;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.chatAI.ChatAI;
import quilt.net.mca.entity.ai.chatAI.OpenAIChatAI;
import quilt.net.mca.network.s2c.OpenGuiRequest;
import quilt.net.mca.server.ServerInteractionManager;
import quilt.net.mca.server.world.data.PlayerSaveData;

public class Command {
   public static void register(CommandDispatcher<class_2168> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247(
                                                "mca"
                                             )
                                             .then(
                                                register("propose").then(class_2170.method_9244("target", class_2186.method_9305()).executes(Command::propose))
                                             ))
                                          .then(register("accept").then(class_2170.method_9244("target", class_2186.method_9305()).executes(Command::accept))))
                                       .then(register("proposals", Command::displayProposal)))
                                    .then(register("procreate", Command::procreate)))
                                 .then(register("separate", Command::separate)))
                              .then(register("reject").then(class_2170.method_9244("target", class_2186.method_9305()).executes(Command::reject))))
                           .then(register("editor", Command::editor)))
                        .then(register("destiny", Command::destiny)))
                     .then(register("mail", Command::mail)))
                  .then(register("verify").then(class_2170.method_9244("email", StringArgumentType.greedyString()).executes(Command::verify))))
               .then(
                  register("chatAI")
                     .requires(p -> p.method_9259(2) || p.method_9211().method_3724())
                     .executes(Command::chatAIHelp)
                     .then(class_2170.method_9247("disable").executes(Command::disableChatAI))
                     .then(class_2170.method_9247("default").executes(c -> enableChatAI(c, "default", (new Config()).villagerChatAIEndpoint, "")))
                     .then(class_2170.method_9247("player2").executes(Command::setupPlayer2))
                     .then(
                        register("inworldAI")
                           .requires(p -> p.method_9259(2) || p.method_9211().method_3724())
                           .then(
                              register("keys")
                                 .then(
                                    class_2170.method_9244("api_key", StringArgumentType.string())
                                       .executes(c -> inworldAIKey((String)c.getArgument("api_key", String.class)))
                                 )
                           )
                           .then(
                              register("addCharacter")
                                 .then(
                                    class_2170.method_9244("villager_name", StringArgumentType.string())
                                       .then(
                                          class_2170.method_9244("character_endpoint", StringArgumentType.string())
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
                        ((RequiredArgumentBuilder)class_2170.method_9244("model", StringArgumentType.string())
                              .executes(c -> enableChatAI(c, (String)c.getArgument("model", String.class), (new Config()).villagerChatAIEndpoint, "")))
                           .then(
                              ((RequiredArgumentBuilder)class_2170.method_9244("endpoint", StringArgumentType.string())
                                    .executes(
                                       c -> enableChatAI(c, (String)c.getArgument("model", String.class), (String)c.getArgument("endpoint", String.class), "")
                                    ))
                                 .then(
                                    class_2170.method_9244("token", StringArgumentType.string())
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
                  .requires(p -> p.method_9211().method_3724())
                  .then(class_2170.method_9247("default").executes(ctx -> ttsEnable(ctx, "default")))
                  .then(class_2170.method_9247("elevenlabs").executes(ctx -> ttsEnable(ctx, "elevenlabs")))
                  .then(class_2170.method_9247("realtime").executes(ctx -> ttsEnable(ctx, "realtime")))
                  .then(class_2170.method_9247("disable").executes(Command::ttsDisable))
            )
      );
   }

   private static int chatAIHelp(CommandContext<class_2168> ctx) {
      return enableChatAI(ctx, (new Config()).villagerChatAIModel, (new Config()).villagerChatAIEndpoint, (new Config()).villagerChatAIToken);
   }

   private static int inworldAIKey(String apiKey) {
      Config.getInstance().inworldAIToken = apiKey;
      Config.getInstance().save();
      return 0;
   }

   private static int inworldAICharacter(CommandContext<class_2168> ctx, String name, String endpoint) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      Optional<VillagerEntityMCA> optionalVillager = ChatAI.findVillagerInArea(player, name);
      optionalVillager.ifPresent(v -> {
         Config.getInstance().inworldAIResourceNames.put(v.method_5667(), endpoint);
         ChatAI.clearStrategy(v.method_5667());
         Config.getInstance().save();
      });
      return 0;
   }

   private static int enableChatAI(CommandContext<class_2168> ctx, String model, String endpoint, String token) {
      Config.getInstance().enableVillagerChatAI = true;
      Config.getInstance().villagerChatAIModel = model;
      Config.getInstance().villagerChatAIEndpoint = endpoint;
      Config.getInstance().villagerChatAIToken = token;
      Config.getInstance().save();
      if (model.equals("default")) {
         sendMessage(
            ctx,
            class_2561.method_43471("mca.ai_help")
               .method_27694(
                  s -> s.method_10958(
                     new class_2558(class_2559.field_11749, "https://github.com/Luke100000/minecraft-comes-alive/wiki/GPT3-based-conversations")
                  )
               )
         );
      } else {
         sendMessage(ctx, "command.chat_ai.enabled");
      }

      return 0;
   }

   private static int disableChatAI(CommandContext<class_2168> c) {
      Config.getInstance().enableVillagerChatAI = false;
      Config.getInstance().save();
      sendMessage(c, "command.chat_ai.disabled");
      return 0;
   }

   private static int setupPlayer2(CommandContext<class_2168> ctx) {
      Config.getInstance().enableVillagerChatAI = true;
      Config.getInstance().villagerChatAIModel = "player2";
      Config.getInstance().villagerChatAIEndpoint = "http://127.0.0.1:4315/v1/chat/completions";
      Config.getInstance().villagerChatAIToken = "";
      Config.getInstance().villagerChatAIUseTools = true;
      Config.getInstance().enableOnlineTTS = true;
      Config.getInstance().onlineTTSModel = "player2";
      Config.getInstance().save();
      sendMessage(
         ctx,
         class_2561.method_43471("command.chat_ai.player2").method_27694(s -> s.method_10958(new class_2558(class_2559.field_11749, "https://player2.game/")))
      );
      return 0;
   }

   private static int ttsEnable(CommandContext<class_2168> ctx, String model) {
      Config.getInstance().enableOnlineTTS = true;
      Config.getInstance().onlineTTSModel = model;
      Config.getInstance().save();
      sendMessage(ctx, class_2561.method_43471("command.tts.enabled." + model));
      return 0;
   }

   private static int ttsDisable(CommandContext<class_2168> ctx) {
      Config.getInstance().enableOnlineTTS = false;
      Config.getInstance().save();
      return 0;
   }

   private static int editor(CommandContext<class_2168> ctx) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 1;
      } else if (((class_2168)ctx.getSource()).method_9259(2) || Config.getInstance().allowFullPlayerEditor) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, player), player);
         return 0;
      } else if (Config.getInstance().allowLimitedPlayerEditor) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.LIMITED_VILLAGER_EDITOR, player), player);
         return 0;
      } else {
         sendMessage(ctx, class_2561.method_43471("command.no_permission").method_27692(class_124.field_1061));
         return 1;
      }
   }

   private static int destiny(CommandContext<class_2168> ctx) {
      if (!((class_2168)ctx.getSource()).method_9259(2) && !Config.getInstance().allowDestinyCommandOnce) {
         sendMessage(ctx, class_2561.method_43471("command.no_permission").method_27692(class_124.field_1061));
         return 1;
      } else {
         class_3222 player = ((class_2168)ctx.getSource()).method_44023();
         if ((player == null || PlayerSaveData.get(player).isEntityDataSet()) && !Config.getInstance().allowDestinyCommandMoreThanOnce) {
            sendMessage(ctx, class_2561.method_43471("command.only_one_destiny").method_27692(class_124.field_1061));
            return 1;
         } else {
            ServerInteractionManager.launchDestiny(player);
            return 0;
         }
      }
   }

   private static int mail(CommandContext<class_2168> ctx) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 1;
      }

      PlayerSaveData data = PlayerSaveData.get(player);
      if (data.hasMail()) {
         while (data.hasMail()) {
            player.method_31548().method_7398(data.getMail());
         }
      } else {
         sendMessage(ctx, "command.no_mail");
      }

      return 0;
   }

   private static int verify(CommandContext<class_2168> ctx) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      CompletableFuture.runAsync(
         () -> {
            Map<String, String> params = new HashMap<>();
            params.put("email", StringArgumentType.getString(ctx, "email"));
            assert player != null;
            params.put("player", player.method_5477().getString());
            String encodedURL = params.keySet()
               .stream()
               .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
               .collect(Collectors.joining("&", Config.getInstance().villagerChatAIEndpoint.replace("v1/mca/chat", "v1/mca/verify") + "?", ""));
            String request = OpenAIChatAI.verify(encodedURL);
            if (request.equals("success")) {
               sendMessage(ctx, class_2561.method_43471("command.verify.success").method_27692(class_124.field_1060));
            } else if (request.equals("failed")) {
               sendMessage(ctx, class_2561.method_43471("command.verify.failed").method_27692(class_124.field_1061));
            } else {
               sendMessage(ctx, class_2561.method_43471("command.verify.crashed").method_27692(class_124.field_1061));
            }
         }
      );
      return 0;
   }

   private static int propose(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_3222 target = class_2186.method_9315(ctx, "target");
      ServerInteractionManager.getInstance().sendProposal(((class_2168)ctx.getSource()).method_44023(), target);
      return 0;
   }

   private static int accept(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_3222 target = class_2186.method_9315(ctx, "target");
      ServerInteractionManager.getInstance().acceptProposal(((class_2168)ctx.getSource()).method_44023(), target);
      return 0;
   }

   private static int displayProposal(CommandContext<class_2168> ctx) {
      ServerInteractionManager.getInstance().listProposals(((class_2168)ctx.getSource()).method_44023());
      return 0;
   }

   private static int procreate(CommandContext<class_2168> ctx) {
      ServerInteractionManager.getInstance().procreate(((class_2168)ctx.getSource()).method_44023());
      return 0;
   }

   private static int separate(CommandContext<class_2168> ctx) {
      ServerInteractionManager.getInstance().endMarriage(((class_2168)ctx.getSource()).method_44023());
      return 0;
   }

   private static int reject(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_3222 target = class_2186.method_9315(ctx, "target");
      ServerInteractionManager.getInstance().rejectProposal(((class_2168)ctx.getSource()).method_44023(), target);
      return 0;
   }

   private static ArgumentBuilder<class_2168, ?> register(String name, com.mojang.brigadier.Command<class_2168> cmd) {
      return ((LiteralArgumentBuilder)class_2170.method_9247(name).requires(cs -> cs.method_9259(0))).executes(cmd);
   }

   private static ArgumentBuilder<class_2168, ?> register(String name) {
      return class_2170.method_9247(name).requires(cs -> cs.method_9259(0));
   }

   private static void sendMessage(CommandContext<class_2168> ctx, String message) {
      sendMessage(ctx, class_2561.method_43471(message));
   }

   private static void sendMessage(CommandContext<class_2168> ctx, class_2561 message) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      if (player != null) {
         player.method_43496(message);
      }
   }
}
