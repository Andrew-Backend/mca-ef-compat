package fabric.net.mca.client.tts;

import fabric.net.mca.MCA;
import fabric.net.mca.client.tts.resources.Player2LanguageMap;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Player2SpeechManager extends RealtimeSpeechManager {
   public Player2SpeechManager(String url) {
      super(url);
   }

   public void play(String text, String gender, String language, float pitch, float gene) {
      CompletableFuture.runAsync(() -> {
         List<String> voices = this.getVoices(language, gender);
         if (voices != null) {
            if (voices.isEmpty()) {
               OnlineSpeechManager.languageNotSupported();
            } else {
               int tone = Math.min(voices.size() - 1, (int)Math.floor(gene * voices.size()));
               this.playInApp(text, pitch, voices.get(tone));
            }
         }
      });
   }

   public void playInApp(String text, float pitch, String voiceId) {
      String url = this.url + "v1/tts/speak";
      String payload = String.format("{\"play_in_app\": true, \"speed\": %s, \"text\": \"%s\", \"voice_ids\": [\"%s\"]}", pitch, text, voiceId);
      download(null, url, payload, "");
   }

   @Override
   public List<String> getVoices(String languageCode, String gender) {
      if (this.voiceInfoMap == null) {
         this.voiceInfoMap = this.fetchVoices(this.url + "v1/tts/voices");
      }

      if (this.voiceInfoMap == null) {
         return Collections.emptyList();
      }

      String language = Player2LanguageMap.LANGUAGE_MAP.getOrDefault(languageCode, languageCode);
      return this.voiceInfoMap.values().stream().filter(info -> info.language.equals(language) && info.gender.equals(gender)).map(info -> info.id).toList();
   }

   public boolean checkHealth() {
      try {
         HttpURLConnection connection = (HttpURLConnection)new URL(this.url + "v1/health").openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("player2-game-key", "minecraft-comes-alive-reborn");
         if (connection.getResponseCode() == 200) {
            return true;
         }

         MCA.LOGGER.warn("Failed to check player2 health: {} - {}", connection.getResponseCode(), connection.getResponseMessage());
      } catch (IOException e) {
         MCA.LOGGER.debug("Failed to check player2 health: {}", e.getMessage());
      }

      return false;
   }
}
