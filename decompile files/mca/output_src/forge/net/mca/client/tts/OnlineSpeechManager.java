package forge.net.mca.client.tts;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.client.tts.resources.OnlineLanguageMap;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class OnlineSpeechManager {
   public static final int TOTAL_VOICES = 10;
   private boolean warningIssued = false;

   public void play(String phrase, String gameLang, String gender, float pitch, float gene, Entity entity, boolean translatable) {
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
                     ResourceLocation soundLocation = MCA.locate("tts_cache/" + hash);
                     SpeechManager.INSTANCE.playSound(pitch, entity, soundLocation);
                  } else if (!this.warningIssued) {
                     this.warningIssued = true;
                     Minecraft.m_91087_()
                        .m_240442_()
                        .m_240494_(Component.m_237115_("command.tts_busy").m_130944_(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}), false);
                  }
               }
            );
         }
      }
   }

   public static void languageNotSupported() {
      Minecraft.m_91087_()
         .f_91065_
         .m_93076_()
         .m_93785_(
            Component.m_237115_("command.tts_unsupported_language")
               .m_130938_(
                  s -> s.m_131140_(ChatFormatting.RED)
                     .m_131142_(new ClickEvent(Action.OPEN_URL, "https://github.com/Luke100000/minecraft-comes-alive/wiki/TTS"))
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
