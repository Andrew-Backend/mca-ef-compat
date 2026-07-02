package fabric.net.mca.resources.data.skin;

import com.google.gson.JsonObject;
import fabric.net.mca.entity.ai.relationship.Gender;
import net.minecraft.class_3518;
import org.jetbrains.annotations.Nullable;

public class Clothing extends SkinListEntry {
   @Nullable
   public final String profession;
   public final int temperature;
   public final boolean exclude;

   public Clothing(String identifier, @Nullable String profession, int temperature, boolean exclude, Gender gender) {
      super(identifier, gender, 1.0F);
      this.profession = profession;
      this.temperature = temperature;
      this.exclude = exclude;
   }

   public Clothing(String identifier, JsonObject object) {
      super(identifier, object);
      this.profession = object.get("profession").isJsonNull() ? null : class_3518.method_15253(object, "profession", null);
      this.exclude = class_3518.method_15258(object, "exclude", false);
      this.temperature = class_3518.method_15282(object, "temperature", 0);
   }

   @Override
   public JsonObject toJson() {
      JsonObject j = super.toJson();
      j.addProperty("profession", this.profession);
      j.addProperty("exclude", this.exclude);
      j.addProperty("temperature", this.temperature);
      return j;
   }
}
