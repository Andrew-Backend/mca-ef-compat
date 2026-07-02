package quilt.net.mca.client.tts;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1106;
import net.minecraft.class_1111;
import net.minecraft.class_1113;
import net.minecraft.class_1297;
import net.minecraft.class_2477;
import net.minecraft.class_2561;
import net.minecraft.class_2585;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_5819;
import net.minecraft.class_5862;
import net.minecraft.class_7417;
import net.minecraft.class_1111.class_1112;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.client.tts.sound.CustomEntityBoundSoundInstance;
import quilt.net.mca.client.tts.sound.SingleWeighedSoundEvents;
import quilt.net.mca.entity.CommonSpeechManager;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Genetics;
import quilt.net.mca.util.LimitedLinkedHashMap;

public class SpeechManager {
   public static final SpeechManager INSTANCE = new SpeechManager();
   private final class_310 client;
   private final LimitedLinkedHashMap<UUID, class_1106> currentlyPlaying = new LimitedLinkedHashMap<>(10);
   private final RealtimeSpeechManager realtimeSpeechManager = new RealtimeSpeechManager(Config.getInstance().onlineTTSServer);
   private final Player2SpeechManager player2SpeechManager = new Player2SpeechManager(Config.getInstance().player2Url);
   private final ElevenlabsSpeechManager elevenlabsSpeechManager = new ElevenlabsSpeechManager();
   private final OnlineSpeechManager onlineSpeechManager = new OnlineSpeechManager();
   private final class_5819 threadSafeRandom = class_5819.method_43050();
   private long lastHealthCheckTime = 0L;
   private boolean firstRun = true;

   public SpeechManager() {
      this.client = class_310.method_1551();
   }

   public class_1106 getSound(float pitch, class_1297 entity, class_2960 soundLocation) {
      class_1111 sound = new class_1111(
         soundLocation.method_12832(), class_5862.method_33908(1.0F), class_5862.method_33908(1.0F), 1, class_1112.field_5474, true, false, 16
      );
      SingleWeighedSoundEvents weightedSoundEvents = new SingleWeighedSoundEvents(sound, soundLocation, "");
      return new CustomEntityBoundSoundInstance(
         weightedSoundEvents, class_3414.method_47908(soundLocation), class_3419.field_15254, 1.0F, pitch, entity, this.threadSafeRandom.method_43055()
      );
   }

   public void playSound(float pitch, class_1297 entity, class_2960 soundLocation) {
      this.client.execute(() -> this.client.method_1483().method_4873(INSTANCE.getSound(pitch, entity, soundLocation)));
   }

   public void onChatMessage(class_2561 message, UUID sender) {
      class_7417 content = message.method_10851();
      if (CommonSpeechManager.INSTANCE.translations.containsKey(content)) {
         this.speak(CommonSpeechManager.INSTANCE.translations.get(content), sender, true);
      } else if (content instanceof class_2585 literal) {
         this.speak(literal.comp_737(), sender, false);
      }
   }

   private VillagerEntityMCA getSpeaker(class_310 client, UUID sender) {
      if (client.field_1687 != null) {
         for (class_1297 entity : client.field_1687.method_18112()) {
            if (entity instanceof VillagerEntityMCA v && entity.method_5667().equals(sender)) {
               return v;
            }
         }
      }

      return null;
   }

   private void speak(String phrase, UUID sender, boolean translatable) {
      class_310 client = class_310.method_1551();
      if (client.field_1687 != null) {
         if (!this.currentlyPlaying.containsKey(sender) || !client.method_1483().method_4877((class_1113)this.currentlyPlaying.get(sender))) {
            VillagerEntityMCA villager = this.getSpeaker(client, sender);
            if (villager != null) {
               if (!villager.isSpeechImpaired()) {
                  if (!villager.isToYoungToSpeak()) {
                     float pitch = villager.method_6017();
                     float gene = villager.getGenetics().getGene(Genetics.VOICE_TONE);
                     String gender = villager.getGenetics().getGender().binary().getDataName();
                     if (Config.getInstance().enableOnlineTTS) {
                        if (translatable) {
                           if (!class_2477.method_10517().method_4678(phrase)) {
                              MCA.LOGGER.warn("Tried to play a TTS sound for a non-translatable phrase: {}", phrase);
                              return;
                           }

                           phrase = class_2477.method_10517().method_48307(phrase);
                        }

                        String gameLang = client.field_1690.field_1883;
                        switch (Config.getInstance().onlineTTSModel) {
                           case "realtime":
                              this.realtimeSpeechManager.play(phrase, gender, gameLang, pitch, gene, villager, translatable);
                              break;
                           case "player2":
                              this.player2SpeechManager.play(phrase, gender, gameLang, pitch, gene);
                              break;
                           case "elevenlabs":
                              this.elevenlabsSpeechManager.play(phrase, gender, pitch, gene, villager, translatable);
                              break;
                           default:
                              this.onlineSpeechManager.play(phrase, gameLang, gender, pitch, gene, villager, translatable);
                        }
                     } else if (translatable) {
                        int tone = Math.min(9, (int)Math.floor(gene * 10.0F));
                        class_2960 sound = new class_2960("mca_voices", phrase.toLowerCase(Locale.ROOT) + "/" + gender + "_" + tone);
                        if (client.field_1687 != null && client.field_1724 != null) {
                           Collection<class_2960> keys = client.method_1483().method_4864();
                           if (keys.contains(sound)) {
                              class_1106 instance = new class_1106(
                                 class_3414.method_47908(sound), class_3419.field_15254, 1.0F, pitch, villager, this.threadSafeRandom.method_43055()
                              );
                              this.currentlyPlaying.put(sender, instance);
                              client.method_1483().method_4873(instance);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void tick(class_310 client) {
      if (client.field_1687 != null) {
         long time = client.field_1687.method_8510();
         if (Math.abs(time - this.lastHealthCheckTime) > 1200L) {
            boolean enabled = Config.getInstance().villagerChatAIModel.equals("player2");
            if (this.firstRun || enabled) {
               CompletableFuture.runAsync(() -> {
                  if (this.player2SpeechManager.checkHealth() && this.firstRun && !enabled) {
                     client.field_1705.method_1743().method_1812(class_2561.method_43469("command.chat_ai.player2.hint", new Object[]{"/mca chatAI player2"}));
                     this.firstRun = false;
                  }
               });
            }

            this.lastHealthCheckTime = time;
         }
      }
   }
}
