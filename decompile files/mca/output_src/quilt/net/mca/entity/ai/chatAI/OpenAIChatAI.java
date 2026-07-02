package quilt.net.mca.entity.ai.chatAI;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_3222;
import net.minecraft.class_3545;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.chatAI.modules.EnvironmentModule;
import quilt.net.mca.entity.ai.chatAI.modules.PersonalityModule;
import quilt.net.mca.entity.ai.chatAI.modules.PlayerModule;
import quilt.net.mca.entity.ai.chatAI.modules.RelationModule;
import quilt.net.mca.entity.ai.chatAI.modules.TraitsModule;
import quilt.net.mca.entity.ai.chatAI.modules.VillageModule;

public class OpenAIChatAI implements ChatAIStrategy {
   private static final int MAX_MEMORY = 500;
   private static final int MAX_MEMORY_TIME = 54000;
   private static final Map<UUID, List<class_3545<String, String>>> memory = new HashMap<>();
   private static final Map<UUID, Long> lastInteractions = new HashMap<>();

   public static String translate(String phrase) {
      return phrase.replace("_", " ").toLowerCase(Locale.ROOT).replace("mca.", "");
   }

   private static HttpURLConnection getHttpURLConnection(String url, String token) throws IOException {
      HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Accept", "application/json");
      con.setRequestProperty("Authorization", "Bearer " + token);
      con.setDoOutput(true);
      return con;
   }

   private static OpenAIChatAI.Answer parseAnswer(String body) {
      JsonObject map = JsonParser.parseString(body).getAsJsonObject();
      String message = map.has("choices")
         ? map.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").getAsJsonPrimitive("content").getAsString()
         : null;
      String error = map.has("error") ? map.get("error").getAsString().trim().replace("\n", " ") : null;
      if (message != null) {
         message = message.replaceAll("```", "");
         int bracketStart = message.indexOf("{");
         int bracketEnd = message.lastIndexOf("}");
         if (bracketEnd > bracketStart && bracketStart != -1) {
            message = message.substring(bracketStart, bracketEnd + 1);
         }
      }

      OpenAIChatAI.StructuredResponse structuredReply;
      try {
         structuredReply = (OpenAIChatAI.StructuredResponse)new Gson().fromJson(message, OpenAIChatAI.StructuredResponse.class);
      } catch (JsonSyntaxException e) {
         MCA.LOGGER.warn("Error parsing answer: {} ({})", message, e.getMessage());
         structuredReply = new OpenAIChatAI.StructuredResponse(cleanupAnswer(message), "");
      }

      return new OpenAIChatAI.Answer(structuredReply, error);
   }

   public static OpenAIChatAI.Answer post(String url, String requestBody, String token) {
      try {
         HttpURLConnection con = getHttpURLConnection(url, token);

         try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(requestBody.getBytes(StandardCharsets.UTF_8));
            wr.flush();
         }

         InputStream response = con.getInputStream();
         String body = IOUtils.toString(response, StandardCharsets.UTF_8);
         return parseAnswer(body);
      } catch (Exception e) {
         MCA.LOGGER.error(e);
         return new OpenAIChatAI.Answer(null, "Unknown error, check log!");
      }
   }

   public static String verify(String encodedURL) {
      try {
         HttpURLConnection con = (HttpURLConnection)new URL(encodedURL).openConnection();
         con.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
         InputStream response = con.getInputStream();
         String body = IOUtils.toString(response, StandardCharsets.UTF_8);
         JsonObject map = JsonParser.parseString(body).getAsJsonObject();
         return map.has("answer") ? map.get("answer").getAsString().trim().replace("\n", " ") : "";
      } catch (Exception e) {
         MCA.LOGGER.error(e);
         return "error";
      }
   }

   @Override
   public Optional<String> answer(class_3222 player, VillagerEntityMCA villager, String msg) {
      try {
         Config config = Config.getInstance();
         boolean isInHouse = config.villagerChatAIEndpoint.contains("conczin.net");
         String playerName = player.method_5477().getString();
         String villagerName = villager.method_5477().getString();
         long time = villager.method_37908().method_8510();
         if (time > lastInteractions.getOrDefault(villager.method_5667(), 0L) + 54000L) {
            memory.remove(villager.method_5667());
         }

         lastInteractions.put(villager.method_5667(), time);
         List<class_3545<String, String>> pastDialogue = memory.computeIfAbsent(villager.method_5667(), key -> new LinkedList<>());

         while (pastDialogue.stream().mapToInt(v -> ((String)v.method_15441()).length() / 4).sum() > 500) {
            pastDialogue.remove(0);
         }

         List<String> input = new LinkedList<>();
         PersonalityModule.apply(input, villager, player);
         TraitsModule.apply(input, villager, player);
         RelationModule.apply(input, villager, player);
         VillageModule.apply(input, villager, player);
         EnvironmentModule.apply(input, villager, player);
         PlayerModule.apply(input, villager, player);
         Map<String, String> variables = Map.of("player", playerName, "villager", villagerName);
         StringBuilder sb = new StringBuilder();
         if (isInHouse || config.villagerChatAIIncludeSessionInformation) {
            long seed = player.method_51469().method_8412();
            sb.append("[world_id:").append(seed).append("]");
            sb.append("[player_id:").append(player.method_5667()).append("]");
            sb.append("[character_id:").append(villager.method_5667()).append("]");
            if (config.villagerChatAIUseLongTermMemory) {
               sb.append("[use_memory:true]");
            }

            if (config.villagerChatAIUseSharedLongTermMemory) {
               sb.append("[shared_memory:true]");
            }
         }

         if (!config.villagerChatAISystemPrompt.isEmpty()) {
            sb.append(config.villagerChatAISystemPrompt);
            sb.append("\n");
         } else if (!isInHouse) {
            String defaultPrompt = "You are a Minecraft villager, fully immersed in their virtual world, unaware of its artificial nature. You respond based on your description, your role, and your knowledge of the world. You have no knowledge of the real world, and do not realize that you are within Minecraft. You are no assistant! You can be sarcastic, funny, or even rude when appropriate.";
            sb.append(defaultPrompt);
            sb.append("\n");
         }

         for (String s : input) {
            for (Entry<String, String> entry : variables.entrySet()) {
               s = s.replaceAll("\\$" + entry.getKey(), entry.getValue());
            }

            sb.append(s);
         }

         if (MCA.language != null) {
            sb.append("Match the language of the player, and use ").append(MCA.language).append(" when unsure.");
         }

         List<TriggerCommandInfo> validCommands;
         if (config.villagerChatAIUseTools) {
            validCommands = TriggerCommandInfos.triggerCommands.stream().filter(c -> c.isActive == null || c.isActive.test(player, villager)).toList();
            MCA.LOGGER.info("Valid commands: {}", validCommands.stream().map(c -> c.command).toList());
         } else {
            validCommands = List.of();
         }

         if (!validCommands.isEmpty()) {
            String structureExample = new Gson().toJson(new OpenAIChatAI.StructuredResponse("example message to say", validCommands.get(0).command));
            sb.append("\n\n");
            sb.append("The reply MUST be in this JSON format: ").append(structureExample).append("\n");
            sb.append("The following commands are valid:\n");

            for (TriggerCommandInfo command : validCommands) {
               sb.append("  * ").append(command.command).append(": ").append(command.description).append("\n");
            }

            sb.append("Only use a command when the player asks for it.");
         }

         String system = sb.toString();
         StringBuilder body = new StringBuilder();
         body.append("{");
         body.append("\"model\": \"").append(config.villagerChatAIModel).append("\",");
         body.append("\"messages\": [");
         body.append("{\"role\": \"system\", \"content\": ").append(jsonStringQuote(system)).append("},");

         for (class_3545<String, String> pair : pastDialogue) {
            String role = (String)pair.method_15442();
            String content = (String)pair.method_15441();
            String name = role.equals("user") ? playerName : villagerName;
            body.append("{\"role\": \"")
               .append(role)
               .append("\", \"name\": \"")
               .append(name)
               .append("\", \"content\": ")
               .append(jsonStringQuote(content))
               .append("},");
         }

         body.append("{\"role\": \"user\", \"name\": \"").append(playerName).append("\", \"content\": ").append(jsonStringQuote(msg)).append("}");
         body.append("]");
         body.append("}");
         String token = config.villagerChatAIToken;
         if (token.isEmpty() || config.villagerChatAIEndpoint.contains("conczin.net")) {
            token = player.method_5477().getString();
         }

         OpenAIChatAI.Answer message = post(config.villagerChatAIEndpoint, body.toString(), token);
         if (message.error == null) {
            if (message.answer != null) {
               pastDialogue.add(new class_3545("user", msg));
               pastDialogue.add(new class_3545("assistant", message.answer.message != null ? message.answer.message : "..."));
               if (message.answer.optionalCommand() != null && !message.answer.optionalCommand().isEmpty()) {
                  Optional<TriggerCommandInfo> command = TriggerCommandInfos.findCommand(message.answer.optionalCommand(), player, villager);
                  command.ifPresent(triggerCommandInfo -> triggerCommandInfo.call.accept(player, villager));
               }
            }

            return Optional.ofNullable(message.answer != null ? message.answer.message : null);
         }

         if (message.error.equals("invalid_model")) {
            player.method_7353(class_2561.method_43470("Invalid model!").method_27692(class_124.field_1061), false);
         } else if (message.error.equals("limit")) {
            class_5250 styled = class_2561.method_43471("mca.limit.patreon")
               .method_27694(
                  s -> s.method_10977(class_124.field_1065)
                     .method_10958(
                        new class_2558(
                           class_2559.field_11749,
                           "https://github.com/Luke100000/minecraft-comes-alive/wiki/GPT3-based-conversations#increase-conversation-limit"
                        )
                     )
                     .method_10949(new class_2568(class_5247.field_24342, class_2561.method_43471("mca.limit.patreon.hover")))
               );
            player.method_7353(styled, false);
         } else if (message.error.equals("limit_premium")) {
            player.method_7353(class_2561.method_43471("mca.limit.premium").method_27692(class_124.field_1061), false);
         } else {
            player.method_7353(class_2561.method_43470(message.error).method_27692(class_124.field_1061), false);
         }
      } catch (Exception e) {
         MCA.LOGGER.error("Failed to parse LLM response!", e);
         player.method_7353(class_2561.method_43471("mca.ai_broken").method_27692(class_124.field_1061), false);
      }

      return Optional.empty();
   }

   static String jsonStringQuote(String string) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: invalid constant type: Ljava/io/Serializable; with value \b
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.toJava(ConstExprent.java:364)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchExprent.toJava(SwitchExprent.java:152)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1054)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:1151)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.toJava(InvocationExprent.java:921)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:925)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:87)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:149)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1351)
      //
      // Bytecode:
      // 00: new java/lang/StringBuilder
      // 03: dup
      // 04: ldc_w "\""
      // 07: invokespecial java/lang/StringBuilder.<init> (Ljava/lang/String;)V
      // 0a: astore 1
      // 0b: aload 0
      // 0c: invokevirtual java/lang/String.toCharArray ()[C
      // 0f: astore 2
      // 10: aload 2
      // 11: arraylength
      // 12: istore 3
      // 13: bipush 0
      // 14: istore 4
      // 16: iload 4
      // 18: iload 3
      // 19: if_icmpge c6
      // 1c: aload 2
      // 1d: iload 4
      // 1f: caload
      // 20: istore 5
      // 22: aload 1
      // 23: iload 5
      // 25: lookupswitch 115 8 8 85 9 91 10 97 12 103 13 109 34 75 47 75 92 75
      // 70: iload 5
      // 72: invokedynamic makeConcatWithConstants (C)Ljava/lang/String; bsm=java/lang/invoke/StringConcatFactory.makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ "\\\u0001" ]
      // 77: goto bc
      // 7a: ldc_w "\\b"
      // 7d: goto bc
      // 80: ldc_w "\\t"
      // 83: goto bc
      // 86: ldc_w "\\n"
      // 89: goto bc
      // 8c: ldc_w "\\f"
      // 8f: goto bc
      // 92: ldc_w "\\r"
      // 95: goto bc
      // 98: iload 5
      // 9a: bipush 32
      // 9c: if_icmpge b7
      // 9f: getstatic java/util/Locale.ROOT Ljava/util/Locale;
      // a2: ldc_w "\\u%04x"
      // a5: bipush 1
      // a6: anewarray 4
      // a9: dup
      // aa: bipush 0
      // ab: iload 5
      // ad: invokestatic java/lang/Character.valueOf (C)Ljava/lang/Character;
      // b0: aastore
      // b1: invokestatic java/lang/String.format (Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
      // b4: goto bc
      // b7: iload 5
      // b9: invokestatic java/lang/Character.valueOf (C)Ljava/lang/Character;
      // bc: invokevirtual java/lang/StringBuilder.append (Ljava/lang/Object;)Ljava/lang/StringBuilder;
      // bf: pop
      // c0: iinc 4 1
      // c3: goto 16
      // c6: aload 1
      // c7: bipush 34
      // c9: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // cc: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // cf: areturn
   }

   static String cleanupAnswer(String answer) {
      if (answer == null) {
         return null;
      }

      answer = answer.replace("\"", "");
      answer = answer.replace("\n", " ");
      String[] parts = answer.split(":", 2);
      return parts[parts.length - 1].strip();
   }

   public record Answer(OpenAIChatAI.StructuredResponse answer, String error) {
   }

   public record StructuredResponse(@Nullable String message, String optionalCommand) {
   }
}
