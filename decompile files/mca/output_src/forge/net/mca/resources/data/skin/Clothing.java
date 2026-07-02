package forge.net.mca.resources.data.skin;

import com.google.gson.JsonObject;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.util.GsonHelper;
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
      this.profession = object.get("profession").isJsonNull() ? null : GsonHelper.m_13851_(object, "profession", null);
      this.exclude = GsonHelper.m_13855_(object, "exclude", false);
      this.temperature = GsonHelper.m_13824_(object, "temperature", 0);
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
