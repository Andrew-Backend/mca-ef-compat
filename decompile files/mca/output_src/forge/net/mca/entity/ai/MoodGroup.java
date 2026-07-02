package forge.net.mca.entity.ai;

import forge.net.mca.SoundsMCA;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;

public class MoodGroup {
   public static final MoodGroup INSTANCE = new MoodGroup(
      new MoodBuilder("depressed")
         .sounds(4, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY)
         .particles(20, ParticleTypes.f_123769_)
         .building("inn")
         .color(ChatFormatting.RED)
         .build(),
      new MoodBuilder("sad")
         .sounds(8, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY)
         .particles(50, ParticleTypes.f_123769_)
         .building("inn")
         .color(ChatFormatting.GOLD)
         .build(),
      new MoodBuilder("unhappy").build(),
      new MoodBuilder("passive").build(),
      new MoodBuilder("fine").build(),
      new MoodBuilder("happy").color(ChatFormatting.DARK_GREEN).build(),
      new MoodBuilder("overjoyed")
         .sounds(8, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH)
         .particles(50, ParticleTypes.f_123748_)
         .color(ChatFormatting.GREEN)
         .build()
   );
   public static final int NORMAL_MIN_LEVEL = -15;
   public static final int MAX_LEVEL = 15;
   private final List<Mood> moods;

   MoodGroup(Mood... m) {
      this.moods = Arrays.asList(m);
   }

   public static int clampMood(int moodPoints) {
      return Mth.m_14045_(moodPoints, -15, 15);
   }

   private int getLevel(int moodPoints) {
      return Mth.m_14045_((moodPoints - -15) * this.moods.size() / 30, 0, this.moods.size() - 1);
   }

   public Mood getMood(int moodPoints) {
      int level = this.getLevel(moodPoints);
      return this.moods.get(level);
   }
}
