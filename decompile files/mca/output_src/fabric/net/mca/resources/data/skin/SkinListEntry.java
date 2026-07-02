package fabric.net.mca.resources.data.skin;

import com.google.gson.JsonObject;
import fabric.net.mca.entity.ai.relationship.Gender;
import java.io.Serializable;
import net.minecraft.class_2960;
import net.minecraft.class_3518;

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
      this.gender = Gender.byId(class_3518.method_15282(object, "gender", 0));
      this.chance = class_3518.method_15277(object, "chance", 1.0F);
   }

   public String getPath() {
      return new class_2960(this.identifier).method_12832();
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
