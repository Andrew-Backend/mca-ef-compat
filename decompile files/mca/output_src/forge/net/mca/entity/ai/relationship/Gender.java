package forge.net.mca.entity.ai.relationship;

import forge.net.mca.Config;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ZombieVillagerEntityMCA;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

public enum Gender {
   UNASSIGNED(16777215, "unassigned"),
   MALE(108266, "male"),
   FEMALE(10897828, "female"),
   NEUTRAL(16777215, "neutral");

   private static final RandomSource RNG = RandomSource.m_216327_();
   private static final Gender[] VALUES = values();
   private static final Map<String, Gender> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(Enum::name, Function.identity()));
   private final int color;
   private final String dataName;

   Gender(int color, String dataName) {
      this.color = color;
      this.dataName = dataName;
   }

   public EntityType<VillagerEntityMCA> getVillagerType() {
      return this == FEMALE ? (EntityType)EntitiesMCA.FEMALE_VILLAGER.get() : (EntityType)EntitiesMCA.MALE_VILLAGER.get();
   }

   public EntityType<ZombieVillagerEntityMCA> getZombieType() {
      return this == FEMALE ? (EntityType)EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get() : (EntityType)EntitiesMCA.MALE_ZOMBIE_VILLAGER.get();
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
      return RNG.m_188499_() ? MALE : FEMALE;
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

   public static Component getText(Gender t) {
      return Component.m_237115_("gui.villager_editor." + t.name().toLowerCase(Locale.ROOT));
   }
}
