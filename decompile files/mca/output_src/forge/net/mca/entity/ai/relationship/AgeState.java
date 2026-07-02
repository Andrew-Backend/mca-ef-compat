package forge.net.mca.entity.ai.relationship;

import forge.net.mca.Config;
import forge.net.mca.resources.API;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public enum AgeState implements VillagerDimensions {
   UNASSIGNED(1.0F, 0.9F, 1.0F, 1.0F, 1.0F, 1.0F),
   BABY(0.45F, 0.4F, 0.0F, 1.5F, 0.0F, 1.6F),
   TODDLER(0.6F, 0.55F, 0.0F, 1.3F, 0.65F, 1.4F),
   CHILD(0.7F, 0.65F, 0.0F, 1.2F, 0.9F, 1.2F),
   TEEN(0.85F, 0.85F, 0.5F, 1.0F, 1.05F, 1.0F),
   ADULT(1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);

   private static final AgeState[] VALUES = values();
   private final float width;
   private final float height;
   private final float breasts;
   private final float head;
   private final float speed;
   private final float pitch;

   public static int getMaxAge() {
      return Config.getServerConfig().villagerMaxAgeTime;
   }

   public static int getStageDuration() {
      return getMaxAge() / 4;
   }

   AgeState(float width, float height, float breasts, float head, float speed, float pitch) {
      this.width = width;
      this.height = height;
      this.breasts = breasts;
      this.head = head;
      this.speed = speed;
      this.pitch = pitch;
   }

   public Component getName() {
      return Component.m_237115_("enum.agestate." + this.name().toLowerCase(Locale.ENGLISH));
   }

   @Override
   public float getWidth() {
      return this.width;
   }

   @Override
   public float getHeight() {
      return this.height;
   }

   @Override
   public float getBreasts() {
      return this.breasts;
   }

   public float getPitch() {
      return this.pitch;
   }

   @Override
   public float getHead() {
      return this.head;
   }

   public float getSpeed() {
      return this.speed;
   }

   public AgeState getNext() {
      return this == ADULT ? this : byId(this.ordinal() + 1);
   }

   public static AgeState byId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : UNASSIGNED;
   }

   public static AgeState random() {
      return byCurrentAge((int)(-API.getRng().m_188501_() * getMaxAge()));
   }

   public static float getDelta(float age) {
      return 1.0F - -age % getStageDuration() / getStageDuration();
   }

   public static int getId(int age) {
      return Mth.m_14045_(1 + (age + getMaxAge()) / getStageDuration(), 0, 5);
   }

   public static AgeState byCurrentAge(int age) {
      return byId(getId(age));
   }

   public int toAge() {
      return (this.ordinal() - 1) * getStageDuration() - getMaxAge();
   }
}
