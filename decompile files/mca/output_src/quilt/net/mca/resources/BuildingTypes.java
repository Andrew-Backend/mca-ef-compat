package quilt.net.mca.resources;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4309;
import quilt.net.mca.MCA;
import quilt.net.mca.resources.data.BuildingType;

public class BuildingTypes extends class_4309 implements Iterable<BuildingType> {
   protected static final class_2960 ID = MCA.locate("building_types");
   private final Map<String, BuildingType> buildingTypes = new HashMap<>();
   private final Map<String, BuildingType> buildingTypesClient = new HashMap<>();
   private static BuildingTypes INSTANCE = new BuildingTypes();

   public BuildingTypes() {
      super(Resources.GSON, ID.method_12832());
      INSTANCE = this;
   }

   protected void apply(Map<class_2960, JsonElement> prepared, class_3300 manager, class_3695 profiler) {
      for (Entry<class_2960, JsonElement> pair : prepared.entrySet()) {
         String name = pair.getKey().method_12832();
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
