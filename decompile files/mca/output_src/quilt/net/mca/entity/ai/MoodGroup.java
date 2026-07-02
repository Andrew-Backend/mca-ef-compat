package quilt.net.mca.entity.ai;

import java.util.Arrays;
import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2398;
import net.minecraft.class_3532;
import quilt.net.mca.SoundsMCA;

public class MoodGroup {
   public static final MoodGroup INSTANCE = new MoodGroup(
      new MoodBuilder("depressed")
         .sounds(4, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY)
         .particles(20, class_2398.field_11202)
         .building("inn")
         .color(class_124.field_1061)
         .build(),
      new MoodBuilder("sad")
         .sounds(8, SoundsMCA.VILLAGER_MALE_CRY, SoundsMCA.VILLAGER_FEMALE_CRY)
         .particles(50, class_2398.field_11202)
         .building("inn")
         .color(class_124.field_1065)
         .build(),
      new MoodBuilder("unhappy").build(),
      new MoodBuilder("passive").build(),
      new MoodBuilder("fine").build(),
      new MoodBuilder("happy").color(class_124.field_1077).build(),
      new MoodBuilder("overjoyed")
         .sounds(8, SoundsMCA.VILLAGER_MALE_LAUGH, SoundsMCA.VILLAGER_FEMALE_LAUGH)
         .particles(50, class_2398.field_11211)
         .color(class_124.field_1060)
         .build()
   );
   public static final int NORMAL_MIN_LEVEL = -15;
   public static final int MAX_LEVEL = 15;
   private final List<Mood> moods;

   MoodGroup(Mood... m) {
      this.moods = Arrays.asList(m);
   }

   public static int clampMood(int moodPoints) {
      return class_3532.method_15340(moodPoints, -15, 15);
   }

   private int getLevel(int moodPoints) {
      return class_3532.method_15340((moodPoints - -15) * this.moods.size() / 30, 0, this.moods.size() - 1);
   }

   public Mood getMood(int moodPoints) {
      int level = this.getLevel(moodPoints);
      return this.moods.get(level);
   }
}
