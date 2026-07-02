package forge.net.mca.resources;

import com.google.gson.JsonElement;
import forge.net.mca.MCA;
import forge.net.mca.resources.data.BuildingType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class BuildingTypes extends SimpleJsonResourceReloadListener implements Iterable<BuildingType> {
   protected static final ResourceLocation ID = MCA.locate("building_types");
   private final Map<String, BuildingType> buildingTypes = new HashMap<>();
   private final Map<String, BuildingType> buildingTypesClient = new HashMap<>();
   private static BuildingTypes INSTANCE = new BuildingTypes();

   public BuildingTypes() {
      super(Resources.GSON, ID.m_135815_());
      INSTANCE = this;
   }

   protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
      for (Entry<ResourceLocation, JsonElement> pair : prepared.entrySet()) {
         String name = pair.getKey().m_135815_();
         this.buildingTypes.put(name, new BuildingType(name, pair.getValue().getAsJsonObject()));
      }

      this.setBuildingTypes(this.buildingTypes);
   }

   public void setBuildingTypes(Map<String, BuildingType> buildingTypes) {
      this.buildingTypesClient.clear();
      this.buildingTypesClient.putAll(buildingTypes);
   }

   public Map<String, BuildingType> getServerBuildingTypes() {
      return this.buildingTypes;
   }

   public Map<String, BuildingType> getBuildingTypes() {
      return this.buildingTypesClient;
   }

   public BuildingType getBuildingType(String type) {
      return this.buildingTypesClient.containsKey(type) ? this.buildingTypesClient.get(type) : new BuildingType();
   }

   public static BuildingTypes getInstance() {
      return INSTANCE;
   }

   @Override
   public Iterator<BuildingType> iterator() {
      return this.buildingTypesClient.values().iterator();
   }
}
