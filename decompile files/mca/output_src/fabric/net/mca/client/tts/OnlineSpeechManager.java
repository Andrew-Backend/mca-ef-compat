package fabric.net.mca.client.tts;

import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.client.tts.resources.OnlineLanguageMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_2558.class_2559;

public class OnlineSpeechManager {
   public static final int TOTAL_VOICES = 10;
   private boolean warningIssued = false;

   public void play(String phrase, String gameLang, String gender, float pitch, float gene, class_1297 entity, boolean translatable) {
      if (translatable) {
         String text = cleanPhrase(phrase);
         String language = OnlineLanguageMap.LANGUAGE_MAP.getOrDefault(gameLang, "");
         if (language.isEmpty()) {
            languageNotSupported();
         } else {
            int tone = Math.min(9, (int)Math.floor(gene * 10.0F));
            String voice = gender + "_" + tone;
            CompletableFuture.runAsync(
               () -> {
                  String hash = language + "-" + voice + "/" + AudioCache.getHash(text) + ".ogg";
                  if (AudioCache.cachedRetrieve(hash, output -> this.downloadAudio(output, language, voice, text))) {
                     class_2960 soundLocation = MCA.locate("tts_cache/" + hash);
                     SpeechManager.INSTANCE.playSound(pitch, entity, soundLocation);
                  } else if (!this.warningIssued) {
                     this.warningIssued = true;
                     class_310.method_1551()
                        .method_44714()
                        .method_44736(
                           class_2561.method_43471("command.tts_busy").method_27695(new class_124[]{class_124.field_1056, class_124.field_1080}), false
                        );
                  }
               }
            );
         }
      }
   }

   public static void languageNotSupported() {
      class_310.method_1551()
         .field_1705
         .method_1743()
         .method_1812(
            class_2561.method_43471("command.tts_unsupported_language")
               .method_27694(
                  s -> s.method_10977(class_124.field_1061)
                     .method_10958(new class_2558(class_2559.field_11749, "https://github.com/Luke100000/minecraft-comes-alive/wiki/TTS"))
               )
         );
   }

   public void downloadAudio(OutputStream output, String language, String voice, String text) {
      Map<String, String> params = Map.of(
         "text",
         text,
         "language",
         language,
         "speaker",
         voice,
         "file_format",
         "ogg",
         "cache",
         "true",
         "prepare_speakers",
         String.valueOf(10),
         "load_async",
         "true"
      );
      String url = params.keySet()
         .stream()
         .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
         .collect(Collectors.joining("&", Config.getInstance().onlineTTSServer + "v1/tts/xtts-v2?", ""));

      try {
         HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
         connection.setRequestMethod("POST");
         connection.setDoOutput(true);

         try (InputStream input = connection.getInputStream()) {
            byte[] buffer = new byte[4096];

            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
               output.write(buffer, 0, bytesRead);
            }
         }

         connection.disconnect();
      } catch (IOException e) {
         MCA.LOGGER.warn("Failed to download {}: {}", url, e.getMessage());
      }
   }

   public static String cleanPhrase(String p) {
      p = p.replaceAll("\\*.*\\*", "");
      p = p.replace("%supporter%", "someone");
      p = p.replace("%Supporter%", "someone");
      p = p.replace("some %2$s", "something");
      p = p.replace("at %2$s", "somewhere here");
      p = p.replace("At %2$s", "Somewhere here");
      p = p.replace(" to %2$s", " to here");
      p = p.replace(", %1$s.", ".");
      p = p.replace(", %1$s!", "!");
      p = p.replace(" %1$s!", "!");
      p = p.replace(", %1$s.", ".");
      p = p.replace("%1$s!", " ");
      p = p.replace("%1$s, ", " ");
      p = p.replace("%1$s", " ");
      p = p.replace("avoid %2$s", "avoid that location");
      p = p.replace(" Should be around %2$s.", "");
      p = p.replace("  ", " ");
      p = p.replace(" ,", ",");
      p = p.replace("Bahaha! ", "");
      p = p.replace("Run awaaaaaay! ", "Run!");
      p = p.replace("Aaaaaaaahhh! ", "");
      p = p.replace("Aaaaaaahhh! ", "");
      p = p.replace("Aaaaaaaaaaahhh! ", "");
      p = p.replace("AAAAAAAAAAAAAAAAAAAHHHHHH!!!!!! ", "");
      return p.trim();
   }
}
