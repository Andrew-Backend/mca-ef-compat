package quilt.net.mca.server.world.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_2487;
import net.minecraft.class_2497;
import net.minecraft.class_2499;
import net.minecraft.class_2503;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_4208;
import net.minecraft.class_7477;
import net.minecraft.class_4153.class_4155;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.resources.API;
import quilt.net.mca.resources.BuildingTypes;
import quilt.net.mca.server.world.data.villageComponents.VillageGuardsManager;
import quilt.net.mca.server.world.data.villageComponents.VillageInnManager;
import quilt.net.mca.server.world.data.villageComponents.VillageMarriageManager;
import quilt.net.mca.server.world.data.villageComponents.VillageProcreationManager;
import quilt.net.mca.server.world.data.villageComponents.VillageTaxesManager;
import quilt.net.mca.util.BlockBoxExtended;
import quilt.net.mca.util.NbtHelper;
import quilt.net.mca.util.WorldUtils;

public class Village implements Iterable<Building> {
   private static final int MOVE_IN_COOLDOWN = 1200;
   public static final int PLAYER_BORDER_MARGIN = 32;
   public static final int BORDER_MARGIN = 48;
   public static final int MERGE_MARGIN = 64;
   private static final long BED_SYNC_TIME = 200L;
   private final class_3218 world;
   private String name = API.getVillagePool().pickVillageName("village");
   public final List<class_1799> storageBuffer = new LinkedList<>();
   private final Map<Integer, Building> buildings = new HashMap<>();
   private Map<UUID, Integer> unspentHearts = new HashMap<>();
   private Map<UUID, Map<UUID, Integer>> reputation = new HashMap<>();
   private int unspentMood = 0;
   private int beds;
   private long lastBedSync;
   private Map<UUID, String> residentNames = new HashMap<>();
   private Map<UUID, Long> residentHomes = new HashMap<>();
   public long lastMoveIn;
   private final int id;
   private float taxes = 0.0F;
   private float populationThreshold = 0.75F;
   private float marriageThreshold = 0.5F;
   private boolean autoScan = Config.getInstance().enableAutoScanByDefault;
   private BlockBoxExtended box = new BlockBoxExtended(0, 0, 0, 0, 0, 0);
   private final VillageGuardsManager villageGuardsManager = new VillageGuardsManager(this);
   private final VillageInnManager villageInnManager = new VillageInnManager(this);
   private final VillageMarriageManager villageMarriageManager = new VillageMarriageManager(this);
   private final VillageProcreationManager villageProcreationManager = new VillageProcreationManager(this);
   private final VillageTaxesManager villageTaxesManager = new VillageTaxesManager(this);

   public Village(int id, class_3218 world) {
      this.id = id;
      this.world = world;
   }

   public Village(class_2487 v, class_3218 world) {
      this.id = v.method_10550("id");
      this.name = v.method_10558("name");
      this.taxes = v.method_10583("taxesFloat");
      this.beds = v.method_10550("beds");
      this.unspentHearts = NbtHelper.toMap(v.method_10562("unspentHearts"), UUID::fromString, ix -> ((class_2497)ix).method_10701());
      this.reputation = NbtHelper.toMap(
         v.method_10562("reputation"), UUID::fromString, ix -> NbtHelper.toMap((class_2487)ix, UUID::fromString, i2 -> ((class_2497)i2).method_10701())
      );
      this.residentNames = NbtHelper.toMap(v.method_10562("residentNames"), UUID::fromString, class_2520::method_10714);
      this.residentHomes = NbtHelper.toMap(v.method_10562("residentHomes"), UUID::fromString, ix -> ((class_2503)ix).method_10699());
      this.unspentMood = v.method_10550("unspentMood");
      if (v.method_10545("populationThresholdFloat")) {
         this.populationThreshold = v.method_10583("populationThresholdFloat");
      }

      if (v.method_10545("marriageThresholdFloat")) {
         this.marriageThreshold = v.method_10583("marriageThresholdFloat");
      }

      this.world = world;
      if (v.method_10545("autoScan")) {
         this.autoScan = v.method_10577("autoScan");
      } else {
         this.autoScan = true;
      }

      class_2499 b = v.method_10554("buildings", 10);

      for (int i = 0; i < b.size(); i++) {
         Building building = new Building(b.method_10602(i));
         if (world == null || BuildingTypes.getInstance().getBuildingTypes().containsKey(building.getType())) {
            this.buildings.put(building.getId(), building);
         }
      }

      if (!this.buildings.isEmpty()) {
         this.calculateDimensions();
      }
   }

   public static Optional<Village> findNearest(class_1297 entity) {
      return VillageManager.get((class_3218)entity.method_37908()).findNearestVillage(entity);
   }

   public boolean isWithinBorder(class_1297 entity) {
      return this.isWithinBorder(entity.method_24515(), entity instanceof class_1657 ? 32 : 48);
   }

   public boolean isWithinBorder(class_2338 pos, int margin) {
      return this.box.method_35410(margin).method_14662(pos);
   }

   @Override
   public Iterator<Building> iterator() {
      return this.buildings.values().iterator();
   }

   public void removeBuilding(int id) {
      this.buildings.remove(id);
      if (!this.buildings.isEmpty()) {
         this.calculateDimensions();
      }

      this.markDirty();
   }

   public Stream<Building> getBuildingsOfType(String type) {
      return this.getBuildings().values().stream().filter(b -> b.getType().equals(type));
   }

   public Optional<Building> getBuildingAt(class_2382 pos) {
      return this.getBuildings().values().stream().filter(b -> b.containsPos(pos)).findAny();
   }

   public void calculateDimensions() {
      int sx = Integer.MAX_VALUE;
      int sy = Integer.MAX_VALUE;
      int sz = Integer.MAX_VALUE;
      int ex = Integer.MIN_VALUE;
      int ey = Integer.MIN_VALUE;
      int ez = Integer.MIN_VALUE;

      for (Building building : this.buildings.values()) {
         ex = Math.max(building.getPos1().method_10263(), ex);
         sx = Math.min(building.getPos0().method_10263(), sx);
         ey = Math.max(building.getPos1().method_10264(), ey);
         sy = Math.min(building.getPos0().method_10264(), sy);
         ez = Math.max(building.getPos1().method_10260(), ez);
         sz = Math.min(building.getPos0().method_10260(), sz);
      }

      this.box = new BlockBoxExtended(sx, sy, sz, ex, ey, ez);
   }

   public class_2382 getCenter() {
      return this.box.method_22874();
   }

   public BlockBoxExtended getBox() {
      return this.box;
   }

   public List<String> getResidents(int building) {
      return this.getBuilding(building)
         .map(
            value -> this.residentHomes
               .entrySet()
               .stream()
               .filter(e -> value.containsPos(class_2338.method_10092(e.getValue())))
               .map(k -> this.residentNames.getOrDefault(k.getKey(), "Unknown"))
               .collect(Collectors.toList())
         )
         .orElseGet(List::of);
   }

   public float getTaxes() {
      return this.taxes;
   }

   public void setTaxes(float taxes) {
      this.taxes = taxes;
   }

   public float getPopulationThreshold() {
      return this.populationThreshold;
   }

   public void setPopulationThreshold(float populationThreshold) {
      this.populationThreshold = populationThreshold;
   }

   public float getMarriageThreshold() {
      return this.marriageThreshold;
   }

   public void setMarriageThreshold(float marriageThreshold) {
      this.marriageThreshold = marriageThreshold;
   }

   public boolean isAutoScan() {
      return this.autoScan;
   }

   public void setAutoScan(boolean autoScan) {
      this.autoScan = autoScan;
   }

   public void toggleAutoScan() {
      this.setAutoScan(!this.isAutoScan());
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Map<Integer, Building> getBuildings() {
      return this.buildings;
   }

   public Optional<Building> getBuilding(int id) {
      return Optional.ofNullable(this.buildings.get(id));
   }

   public int getId() {
      return this.id;
   }

   public boolean hasSpace() {
      return this.getPopulation() < this.getMaxPopulation();
   }

   public int getPopulation() {
      return this.residentNames.size();
   }

   public Stream<UUID> getResidentsUUIDs() {
      return this.residentNames.keySet().stream();
   }

   public boolean isPositionValidBed(class_2338 pos) {
      return this.getBuildingAt(pos).filter(b -> b.getBuildingType().noBeds()).isEmpty();
   }

   public List<VillagerEntityMCA> getResidents(class_3218 world) {
      return this.getResidentsUUIDs()
         .<class_1297>map(world::method_14190)
         .filter(VillagerEntityMCA.class::isInstance)
         .map(VillagerEntityMCA.class::cast)
         .collect(Collectors.toList());
   }

   public void updateMaxPopulation() {
      if (this.world != null) {
         class_2382 dimensions = this.box.method_14659();
         int radius = (int)Math.sqrt(
            dimensions.method_10263() * dimensions.method_10263()
               + dimensions.method_10264() * dimensions.method_10264()
               + dimensions.method_10260() * dimensions.method_10260()
         );
         this.beds = (int)this.world
            .method_19494()
            .method_21647(
               registryEntry -> registryEntry.method_40225(class_7477.field_39291),
               this::isPositionValidBed,
               new class_2338(this.getCenter()),
               radius + 48,
               class_4155.field_18489
            )
            .count();
      }
   }

   public int getMaxPopulation() {
      if (this.world != null && this.world.method_8510() - this.lastBedSync > 200L) {
         this.lastBedSync = this.world.method_8510();
         this.updateMaxPopulation();
      }

      return this.beds;
   }

   public boolean hasStoredResource() {
      return !this.storageBuffer.isEmpty();
   }

   public boolean hasBuilding(String building) {
      return this.buildings.values().stream().anyMatch(b -> b.getType().equals(building) && b.isComplete());
   }

   public void tick(class_3218 world, long time) {
      time += this.getId();
      boolean isTaxSeason = time % Config.getInstance().taxSeason == 0L;
      boolean isVillageUpdateTime = time % 1200L == 0L;
      if (isTaxSeason && this.hasBuilding("storage")) {
         this.villageTaxesManager.taxes(world);
      }

      if (time % 24000L == 0L) {
         this.cleanReputation();
      }

      if (isVillageUpdateTime && this.lastMoveIn + 1200L < time && WorldUtils.isChunkLoaded(world, this.getCenter())) {
         this.villageGuardsManager.spawnGuards(world);
         this.villageInnManager.updateInn(world);
         this.villageMarriageManager.marry(world);
         this.villageProcreationManager.procreate(world);
      }
   }

   public void onEnter(class_3218 world) {
      this.villageTaxesManager.deliverTaxes(world);
   }

   public void broadCastMessage(class_3218 world, String event, VillagerEntityMCA suitor, VillagerEntityMCA mate) {
      world.method_18456()
         .stream()
         .filter(
            p -> PlayerSaveData.get(p).getLastSeenVillageId().orElse(-2) == this.getId()
               || suitor.getVillagerBrain().getMemoriesForPlayer(p).getHearts() > Config.getInstance().heartsToBeConsideredAsFriend
               || mate.getVillagerBrain().getMemoriesForPlayer(p).getHearts() > Config.getInstance().heartsToBeConsideredAsFriend
         )
         .forEach(
            player -> player.method_7353(
               class_2561.method_43469(event, new Object[]{suitor.method_5477(), mate.method_5477()}), !Config.getInstance().showNotificationsAsChat
            )
         );
   }

   public void broadCastMessage(class_3218 world, String event, String targetName) {
      world.method_18456()
         .stream()
         .filter(p -> PlayerSaveData.get(p).getLastSeenVillageId().orElse(-2) == this.getId())
         .forEach(player -> player.method_7353(class_2561.method_43469(event, new Object[]{targetName}), !Config.getInstance().showNotificationsAsChat));
   }

   public void markDirty() {
      VillageManager.get(this.world).method_80();
   }

   public void cleanReputation() {
      Set<UUID> residents = this.getResidentsUUIDs().collect(Collectors.toSet());

      for (Map<UUID, Integer> map : this.reputation.values()) {
         for (UUID uuid : map.keySet().stream().filter(v -> !residents.contains(v)).collect(Collectors.toSet())) {
            map.remove(uuid);
         }
      }
   }

   public void setReputation(class_1657 player, VillagerEntityMCA villager, int rep) {
      this.reputation.computeIfAbsent(player.method_5667(), i -> new HashMap<>()).put(villager.method_5667(), rep);
      this.markDirty();
   }

   public int getReputation(class_1657 player) {
      return this.reputation.getOrDefault(player.method_5667(), Collections.emptyMap()).values().stream().mapToInt(i -> i).sum()
         + this.unspentHearts.getOrDefault(player.method_5667(), 0);
   }

   public void resetHearts(class_1657 player) {
      this.unspentHearts.remove(player.method_5667());
      this.markDirty();
   }

   public void pushHearts(class_1657 player, int rep) {
      this.pushHearts(player.method_5667(), rep);
      this.markDirty();
   }

   public void pushHearts(UUID player, int rep) {
      this.unspentHearts.put(player, this.unspentHearts.getOrDefault(player, 0) + rep);
      this.markDirty();
   }

   public int popHearts(class_1657 player) {
      int v = this.unspentHearts.getOrDefault(player.method_5667(), 0);
      int step = (int)Math.ceil(Math.abs((double)v / this.getPopulation()));
      if (v > 0) {
         v -= step;
         if (v == 0) {
            this.unspentHearts.remove(player.method_5667());
         } else {
            this.unspentHearts.put(player.method_5667(), v);
         }

         this.markDirty();
         return step;
      } else if (v < 0) {
         v += step;
         if (v == 0) {
            this.unspentHearts.remove(player.method_5667());
         } else {
            this.unspentHearts.put(player.method_5667(), v);
         }

         this.markDirty();
         return -step;
      } else {
         return 0;
      }
   }

   public void pushMood(int m) {
      this.unspentMood += m;
      this.markDirty();
   }

   public int popMood() {
      int step = (int)Math.ceil(Math.abs((double)this.unspentMood / this.getPopulation()));
      if (this.unspentMood > 0) {
         this.unspentMood -= step;
         this.markDirty();
         return step;
      } else if (this.unspentMood < 0) {
         this.unspentMood += step;
         this.markDirty();
         return -step;
      } else {
         return 0;
      }
   }

   public class_2487 save() {
      class_2487 v = new class_2487();
      v.method_10569("id", this.id);
      v.method_10582("name", this.name);
      v.method_10548("taxesFloat", this.taxes);
      v.method_10569("beds", this.beds);
      v.method_10566("unspentHearts", NbtHelper.fromMap(new class_2487(), this.unspentHearts, UUID::toString, class_2497::method_23247));
      v.method_10566(
         "reputation",
         NbtHelper.fromMap(
            new class_2487(), this.reputation, UUID::toString, i -> NbtHelper.fromMap(new class_2487(), i, UUID::toString, class_2497::method_23247)
         )
      );
      v.method_10566("residentNames", NbtHelper.fromMap(new class_2487(), this.residentNames, Object::toString, class_2519::method_23256));
      v.method_10566("residentHomes", NbtHelper.fromMap(new class_2487(), this.residentHomes, Object::toString, class_2503::method_23251));
      v.method_10569("unspentMood", this.unspentMood);
      v.method_10548("populationThresholdFloat", this.populationThreshold);
      v.method_10548("marriageThresholdFloat", this.marriageThreshold);
      v.method_10566("buildings", NbtHelper.fromList(this.buildings.values(), Building::save));
      v.method_10556("autoScan", this.autoScan);
      return v;
   }

   public void merge(Village village) {
      this.buildings.putAll(village.buildings);
      this.unspentMood = this.unspentMood + village.unspentMood;
      this.calculateDimensions();
   }

   public boolean isVillage() {
      return this.getBuildings().size() >= Config.getInstance().minimumBuildingsToBeConsideredAVillage;
   }

   public void updateResident(VillagerEntityMCA e) {
      this.residentNames.put(e.method_5667(), e.method_5477().getString());
      Optional<class_4208> home = e.getResidency().getHome();
      if (home.isPresent()) {
         this.residentHomes.put(e.method_5667(), home.get().method_19446().method_10063());
      } else {
         this.residentHomes.remove(e.method_5667());
      }
   }

   public Map<UUID, String> getResidentNames() {
      return this.residentNames;
   }

   public boolean hasResident(UUID id) {
      return this.residentNames.containsKey(id);
   }

   public void removeResident(VillagerEntityMCA villager) {
      this.removeResident(villager.method_5667());
   }

   public void removeResident(UUID uuid) {
      this.residentNames.remove(uuid);
      this.residentHomes.remove(uuid);
      this.cleanReputation();
      this.markDirty();
   }

   public VillageGuardsManager getVillageGuardsManager() {
      return this.villageGuardsManager;
   }

   public Optional<CivilRegistryManager> getCivilRegistry() {
      return this.world != null ? Optional.of(CivilRegistryManager.get(this.world, this)) : Optional.empty();
   }
}
