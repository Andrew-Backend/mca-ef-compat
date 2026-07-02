package fabric.net.mca.entity.ai.relationship;

import fabric.net.mca.Config;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ZombieVillagerEntityMCA;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1299;
import net.minecraft.class_2561;
import net.minecraft.class_5819;

public enum Gender {
   UNASSIGNED(16777215, "unassigned"),
   MALE(108266, "male"),
   FEMALE(10897828, "female"),
   NEUTRAL(16777215, "neutral");

   private static final class_5819 RNG = class_5819.method_43047();
   private static final Gender[] VALUES = values();
   private static final Map<String, Gender> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(Enum::name, Function.identity()));
   private final int color;
   private final String dataName;

   Gender(int color, String dataName) {
      this.color = color;
      this.dataName = dataName;
   }

   public class_1299<VillagerEntityMCA> getVillagerType() {
      return this == FEMALE ? (class_1299)EntitiesMCA.FEMALE_VILLAGER.get() : (class_1299)EntitiesMCA.MALE_VILLAGER.get();
   }

   public class_1299<ZombieVillagerEntityMCA> getZombieType() {
      return this == FEMALE ? (class_1299)EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get() : (class_1299)EntitiesMCA.MALE_ZOMBIE_VILLAGER.get();
   }

   public int getColor() {
      return this.color;
   }

   public int getId() {
      return this.ordinal();
   }

   public String getDataName() {
      return this.dataName;
   }

   public Gender binary() {
      return this == FEMALE ? FEMALE : MALE;
   }

   public Gender opposite() {
      return this == FEMALE ? MALE : FEMALE;
   }

   public static Gender byId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : UNASSIGNED;
   }

   public static Gender getRandom() {
      return RNG.method_43056() ? MALE : FEMALE;
   }

   public static Gender byName(String name) {
      return REGISTRY.getOrDefault(name.toUpperCase(Locale.ENGLISH), UNASSIGNED);
   }

   public float getHorizontalScaleFactor() {
      return this == FEMALE
         ? Config.getInstance().femaleVillagerWidthFactor
         : (
            this == MALE
               ? Config.getInstance().maleVillagerWidthFactor
               : (Config.getInstance().femaleVillagerWidthFactor + Config.getInstance().maleVillagerWidthFactor) * 0.5F
         );
   }

   public float getScaleFactor() {
      return this == FEMALE
         ? Config.getInstance().femaleVillagerHeightFactor
         : (
            this == MALE
               ? Config.getInstance().maleVillagerHeightFactor
               : (Config.getInstance().femaleVillagerHeightFactor + Config.getInstance().maleVillagerHeightFactor) * 0.5F
         );
   }

   public static class_2561 getText(Gender t) {
      return class_2561.method_43471("gui.villager_editor." + t.name().toLowerCase(Locale.ROOT));
   }
}
