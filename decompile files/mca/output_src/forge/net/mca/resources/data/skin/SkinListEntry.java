package forge.net.mca.resources.data.skin;

import com.google.gson.JsonObject;
import forge.net.mca.entity.ai.relationship.Gender;
import java.io.Serializable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public abstract class SkinListEntry implements Serializable {
   protected final String identifier;
   protected final Gender gender;
   protected final float chance;

   public SkinListEntry(String identifier) {
      this(identifier, Gender.NEUTRAL, 1.0F);
   }

   public SkinListEntry(String identifier, Gender gender, float chance) {
      this.identifier = identifier;
      this.gender = gender;
      this.chance = chance;
   }

   public SkinListEntry(String identifier, JsonObject object) {
      this.identifier = identifier;
      this.gender = Gender.byId(GsonHelper.m_13824_(object, "gender", 0));
      this.chance = GsonHelper.m_13820_(object, "chance", 1.0F);
   }

   public String getPath() {
      return new ResourceLocation(this.identifier).m_135815_();
   }

   public JsonObject toJson() {
      JsonObject j = new JsonObject();
      j.addProperty("gender", this.gender == null ? Gender.NEUTRAL.getId() : this.gender.getId());
      j.addProperty("chance", this.chance);
      return j;
   }

   public String getIdentifier() {
      return this.identifier;
   }

   public Gender getGender() {
      return this.gender;
   }

   public float getChance() {
      return this.chance <= 0.0F ? 1.0F : this.chance;
   }
}
