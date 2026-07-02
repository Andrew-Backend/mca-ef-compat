package fabric.net.mca;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonConfig implements Serializable {
   private static final long serialVersionUID = -8238866449153504236L;
   public int babyItemGrowUpTime = 24000;
   public int villagerMaxAgeTime = 384000;
   public boolean allowEveryoneToAddContentGlobally = false;
   public boolean allowPlayerSizeAdjustment = true;
   public boolean allowBodyCustomizationInDestiny = true;
   public boolean allowTraitCustomizationInDestiny = true;
   public List<String> destinySpawnLocations = List.of(
      "somewhere",
      "minecraft:shipwreck_beached",
      "minecraft:village_desert",
      "minecraft:village_taiga",
      "minecraft:village_snowy",
      "minecraft:village_plains",
      "minecraft:village_savanna",
      "minecraft:ancient_city"
   );
   public Map<String, String> destinyLocationsToTranslationMap = Map.of(
      "default", "destiny.story.travelling", "minecraft:shipwreck_beached", "destiny.story.sailing"
   );
   public Map<String, Boolean> enabledTraits = new HashMap<>();

   public CommonConfig() {
   }

   public CommonConfig(CommonConfig config) {
      this.babyItemGrowUpTime = config.babyItemGrowUpTime;
      this.villagerMaxAgeTime = config.villagerMaxAgeTime;
      this.allowEveryoneToAddContentGlobally = config.allowEveryoneToAddContentGlobally;
      this.allowPlayerSizeAdjustment = config.allowPlayerSizeAdjustment;
      this.allowBodyCustomizationInDestiny = config.allowBodyCustomizationInDestiny;
      this.allowTraitCustomizationInDestiny = config.allowTraitCustomizationInDestiny;
      this.destinySpawnLocations = config.destinySpawnLocations;
      this.destinyLocationsToTranslationMap = config.destinyLocationsToTranslationMap;
      this.enabledTraits = config.enabledTraits;
   }
}
