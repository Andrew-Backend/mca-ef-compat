package fabric.net.mca.client.resources;

import fabric.net.mca.entity.ai.relationship.Gender;

public class SkinMeta {
   private final String profession;
   private final int temperature;
   private final int gender;
   private final float chance;

   public SkinMeta(String profession, int temperature, int gender, float chance) {
      this.profession = profession;
      this.temperature = temperature;
      this.gender = gender;
      this.chance = chance;
   }

   public String getProfession() {
      return this.profession;
   }

   public int getTemperature() {
      return this.temperature;
   }

   public Gender getGender() {
      return Gender.byId(this.gender);
   }

   public float getChance() {
      return this.chance;
   }
}
