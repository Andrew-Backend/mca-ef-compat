package forge.net.mca.entity.interaction.gifts;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import forge.net.mca.MCA;
import forge.net.mca.resources.Resources;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class GiftLoader extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = new ResourceLocation("mca", "gifts");

   public GiftLoader() {
      super(Resources.GSON, "gifts");
   }

   protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      GiftType.REGISTRY.clear();
      data.forEach((id, json) -> {
         try {
            GiftType.REGISTRY.add(GiftType.fromJson(id, GsonHelper.m_13918_(json, "root")));
         } catch (JsonParseException e) {
            MCA.LOGGER.error("Could not load gift type for id {}", id, e);
         }
      });

      for (GiftType type : GiftType.REGISTRY) {
         if (!type.getId().m_135827_().equals("mca") && type.getConditions().isEmpty()) {
            for (GiftType extendingType : GiftType.REGISTRY) {
               if (extendingType.getId().m_135827_().equals("mca") && extendingType.getId().m_135815_().equals(type.getId().m_135815_())) {
                  type.extendFrom(extendingType);
                  break;
               }
            }
         }
      }
   }
}
