package forge.net.mca.client.tts;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.client.tts.sound.CustomEntityBoundSoundInstance;
import forge.net.mca.client.tts.sound.SingleWeighedSoundEvents;
import forge.net.mca.entity.CommonSpeechManager;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.util.LimitedLinkedHashMap;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.Sound.Type;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.Entity;

public class SpeechManager {
   public static final SpeechManager INSTANCE = new SpeechManager();
   private final Minecraft client;
   private final LimitedLinkedHashMap<UUID, EntityBoundSoundInstance> currentlyPlaying = new LimitedLinkedHashMap<>(10);
   private final RealtimeSpeechManager realtimeSpeechManager = new RealtimeSpeechManager(Config.getInstance().onlineTTSServer);
   private final Player2SpeechManager player2SpeechManager = new Player2SpeechManager(Config.getInstance().player2Url);
   private final ElevenlabsSpeechManager elevenlabsSpeechManager = new ElevenlabsSpeechManager();
   private final OnlineSpeechManager onlineSpeechManager = new OnlineSpeechManager();
   private final RandomSource threadSafeRandom = RandomSource.m_216337_();
   private long lastHealthCheckTime = 0L;
   private boolean firstRun = true;

   public SpeechManager() {
      this.client = Minecraft.m_91087_();
   }

   public EntityBoundSoundInstance getSound(float pitch, Entity entity, ResourceLocation soundLocation) {
      Sound sound = new Sound(soundLocation.m_135815_(), ConstantFloat.m_146458_(1.0F), ConstantFloat.m_146458_(1.0F), 1, Type.FILE, true, false, 16);
      SingleWeighedSoundEvents weightedSoundEvents = new SingleWeighedSoundEvents(sound, soundLocation, "");
      return new CustomEntityBoundSoundInstance(
         weightedSoundEvents, SoundEvent.m_262824_(soundLocation), SoundSource.NEUTRAL, 1.0F, pitch, entity, this.threadSafeRandom.m_188505_()
      );
   }

   public void playSound(float pitch, Entity entity, ResourceLocation soundLocation) {
      this.client.execute(() -> this.client.m_91106_().m_120367_(INSTANCE.getSound(pitch, entity, soundLocation)));
   }

   public void onChatMessage(Component message, UUID sender) {
      ComponentContents content = message.m_214077_();
      if (CommonSpeechManager.INSTANCE.translations.containsKey(content)) {
         this.speak(CommonSpeechManager.INSTANCE.translations.get(content), sender, true);
      } else if (content instanceof LiteralContents literal) {
         this.speak(literal.f_237368_(), sender, false);
      }
   }

   private VillagerEntityMCA getSpeaker(Minecraft client, UUID sender) {
      if (client.f_91073_ != null) {
         for (Entity entity : client.f_91073_.m_104735_()) {
            if (entity instanceof VillagerEntityMCA v && entity.m_20148_().equals(sender)) {
               return v;
            }
         }
      }

      return null;
   }

   private void speak(String phrase, UUID sender, boolean translatable) {
      Minecraft client = Minecraft.m_91087_();
      if (client.f_91073_ != null) {
         if (!this.currentlyPlaying.containsKey(sender) || !client.m_91106_().m_120403_((SoundInstance)this.currentlyPlaying.get(sender))) {
            VillagerEntityMCA villager = this.getSpeaker(client, sender);
            if (villager != null) {
               if (!villager.isSpeechImpaired()) {
                  if (!villager.isToYoungToSpeak()) {
                     float pitch = villager.m_6100_();
                     float gene = villager.getGenetics().getGene(Genetics.VOICE_TONE);
                     String gender = villager.getGenetics().getGender().binary().getDataName();
                     if (Config.getInstance().enableOnlineTTS) {
                        if (translatable) {
                           if (!Language.m_128107_().m_6722_(phrase)) {
                              MCA.LOGGER.warn("Tried to play a TTS sound for a non-translatable phrase: {}", phrase);
                              return;
                           }

                           phrase = Language.m_128107_().m_6834_(phrase);
                        }

                        String gameLang = client.f_91066_.f_92075_;
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
                        ResourceLocation sound = new ResourceLocation("mca_voices", phrase.toLowerCase(Locale.ROOT) + "/" + gender + "_" + tone);
                        if (client.f_91073_ != null && client.f_91074_ != null) {
                           Collection<ResourceLocation> keys = client.m_91106_().m_120354_();
                           if (keys.contains(sound)) {
                              EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                                 SoundEvent.m_262824_(sound), SoundSource.NEUTRAL, 1.0F, pitch, villager, this.threadSafeRandom.m_188505_()
                              );
                              this.currentlyPlaying.put(sender, instance);
                              client.m_91106_().m_120367_(instance);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void tick(Minecraft client) {
      if (client.f_91073_ != null) {
         long time = client.f_91073_.m_46467_();
         if (Math.abs(time - this.lastHealthCheckTime) > 1200L) {
            boolean enabled = Config.getInstance().villagerChatAIModel.equals("player2");
            if (this.firstRun || enabled) {
               CompletableFuture.runAsync(() -> {
                  if (this.player2SpeechManager.checkHealth() && this.firstRun && !enabled) {
                     client.f_91065_.m_93076_().m_93785_(Component.m_237110_("command.chat_ai.player2.hint", new Object[]{"/mca chatAI player2"}));
                     this.firstRun = false;
                  }
               });
            }

            this.lastHealthCheckTime = time;
         }
      }
   }
}
