package forge.net.mca.client.tts;

import forge.net.mca.Config;
import java.io.OutputStream;
import java.util.List;
import net.minecraft.world.entity.Entity;

public class ElevenlabsSpeechManager extends RealtimeSpeechManager {
   public ElevenlabsSpeechManager() {
      super("https://api.elevenlabs.io/v1/text-to-speech/");
   }

   public void play(String text, String gender, float pitch, float gene, Entity entity, boolean cacheable) {
      List<String> voices = gender.equals("male") ? Config.getInstance().elevenlabsMaleVoices : Config.getInstance().elevenlabsFemaleVoices;
      if (!voices.isEmpty()) {
         int tone = Math.min(voices.size() - 1, (int)Math.floor(gene * voices.size()));
         this.play(text, pitch, entity, cacheable, voices.get(tone));
      }
   }

   @Override
   public void downloadAudio(OutputStream output, String voiceId, String text) {
      String payload = String.format(
         "{\"text\": \"%s\", \"model_id\": \"%s\", \"voice_settings\": {\"stability\": 0.3, \"similarity_boost\": 0.5, \"style\": 0.05, \"use_speaker_boost\": true}}",
         text,
         Config.getInstance().elevenlabsModel
      );
      RealtimeSpeechManager.download(output, this.url + voiceId + "?output_format=pcm_22050", payload, Config.getInstance().elevenlabsPrivateAPIkey);
   }
}
