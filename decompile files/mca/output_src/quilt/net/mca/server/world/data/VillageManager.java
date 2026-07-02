package quilt.net.mca.server.world.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1267;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1543;
import net.minecraft.class_18;
import net.minecraft.class_1948;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.class_3730;
import net.minecraft.class_7923;
import net.minecraft.class_1317.class_1319;
import net.minecraft.class_2902.class_2903;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.resources.BuildingTypes;
import quilt.net.mca.resources.data.BuildingType;
import quilt.net.mca.server.ReaperSpawner;
import quilt.net.mca.server.SpawnQueue;
import quilt.net.mca.util.NbtHelper;
import quilt.net.mca.util.WorldUtils;

public class VillageManager extends class_18 implements Iterable<Village> {
   private final Map<Integer, Village> villages = new HashMap<>();
   public final Set<class_2338> cache = ConcurrentHashMap.newKeySet();
   private final List<class_2338> buildingQueue = new LinkedList<>();
   private int lastBuildingId;
   private int lastVillageId;
   private final class_3218 world;
   private final ReaperSpawner reapers;
   private int buildingCooldown = 21;

   public static VillageManager get(class_3218 world) {
      return WorldUtils.loadData(world, nbt -> new VillageManager(world, nbt), VillageManager::new, "mca_villages");
   }

   VillageManager(class_3218 world) {
      this.world = world;
      this.reapers = new ReaperSpawner(this);
   }

   VillageManager(class_3218 world, class_2487 nbt) {
      this.world = world;
      this.lastBuildingId = nbt.method_10550("lastBuildingId");
      this.lastVillageId = nbt.method_10550("lastVillageId");
      this.reapers = nbt.method_10573("reapers", 10) ? new ReaperSpawner(this, nbt.method_10562("reapers")) : new ReaperSpawner(this);
      class_2499 villageList = nbt.method_10554("villages", 10);

      for (int i = 0; i < villageList.size(); i++) {
         Village village = new Village(villageList.method_10602(i), world);
         if (village.getBuildings().isEmpty()) {
            MCA.LOGGER.warn("Empty village detected (" + village.getName() + "), removing...");
            this.method_80();
         } else {
            this.villages.put(village.getId(), village);
         }
      }
   }

   public ReaperSpawner getReaperSpawner() {
      return this.reapers;
   }

   public Optional<Village> getOrEmpty(int id) {
      return Optional.ofNullable(this.villages.get(id));
   }

   public boolean removeVillage(int id) {
      if (this.villages.remove(id) != null) {
         this.cache.clear();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public Iterator<Village> iterator() {
      return this.villages.values().iterator();
   }

   public Stream<Village> findVillages(Predicate<Village> predicate) {
      return this.villages.values().stream().filter(predicate);
   }

   public Optional<Village> findNearestVillage(class_1297 entity) {
      class_2338 p = entity.method_24515();
      return this.findVillages(v -> v.isWithinBorder(entity)).min((a, b) -> (int)(a.getCenter().method_10262(p) - b.getCenter().method_10262(p)));
   }

   public Optional<Village> findNearestVillage(class_2338 p, int margin) {
      return this.findVillages(v -> v.isWithinBorder(p, margin)).min((a, b) -> (int)(a.getCenter().method_10262(p) - b.getCenter().method_10262(p)));
   }

   public boolean isWithinHorizontalBoundaries(class_2338 p) {
      return this.villages.values().stream().anyMatch(v -> v.getBox().expand(0, 1000, 0).method_14662(p));
   }

   public class_2487 method_75(class_2487 nbt) {
      nbt.method_10569("lastBuildingId", this.lastBuildingId);
      nbt.method_10569("lastVillageId", this.lastVillageId);
      nbt.method_10566("villages", NbtHelper.fromList(this.villages.values(), Village::save));
      nbt.method_10566("reapers", this.reapers.writeNbt());
      return nbt;
   }

   public void tick() {
      if (this.world.method_8532() % 100L == 0L) {
         this.world.method_18456().forEach(player -> PlayerSaveData.get(player).updateLastSeenVillage(this, player));
      }

      if (this.world.method_8532() % (Config.getInstance().bountyHunterInterval / 10) == 0L && this.world.method_8407() != class_1267.field_5801) {
         this.world
            .method_18456()
            .forEach(
               player -> {
                  if (this.world.field_9229.method_43048(10) == 0 && !this.isWithinHorizontalBoundaries(player.method_24515()) && !player.method_7337()) {
                     this.villages
                        .values()
                        .stream()
                        .filter(vx -> vx.getPopulation() >= 3)
                        .filter(vx -> vx.getReputation(player) < Config.getInstance().bountyHunterHearts)
                        .min(Comparator.comparingInt(vx -> vx.getReputation(player)))
                        .ifPresent(buildings -> this.startBountyHunterWave(player, buildings));
                  }
               }
            );
      }

      long time = this.world.method_8510();

      for (Village v : this) {
         v.tick(this.world, time);
      }

      if (time % this.buildingCooldown == 0L && !this.buildingQueue.isEmpty()) {
         this.processBuilding(this.buildingQueue.remove(0));
      }

      this.reapers.tick(this.world);
      SpawnQueue.getInstance().tick();
   }

   private void startBountyHunterWave(class_3222 player, Village sender) {
      int count = Math.min(30, -sender.getReputation(player) / 100 + 2);
      if (sender.getPopulation() == 0) {
         sender.cleanReputation();
         sender.resetHearts(player);
         count *= 2;
      } else {
         sender.pushHearts(player, count * 50);
      }

      CriterionMCA.GENERIC_EVENT_CRITERION.trigger(player, "bounty_hunter");

      for (int c = 0; c < count; c++) {
         if (this.world.field_9229.method_43056()) {
            this.spawnBountyHunter(class_1299.field_6105, player);
         } else {
            this.spawnBountyHunter(class_1299.field_6117, player);
         }
      }

      player.method_7353(
         class_2561.method_43469(sender.getPopulation() == 0 ? "events.bountyHuntersFinal" : "events.bountyHunters", new Object[]{sender.getName()})
            .method_27692(class_124.field_1061),
         false
      );
      sender.getCivilRegistry().ifPresent(r -> r.addText(class_2561.method_43469("civil_registry.bounty_hunters", new Object[]{player.method_5477()})));
   }

   private <T extends class_1543> void spawnBountyHunter(class_1299<T> t, class_3222 player) {
      class_1543 pillager = (class_1543)t.method_5883(this.world);
      if (pillager != null) {
         for (int attempt = 0; attempt < 32; attempt++) {
            float f = this.world.field_9229.method_43057() * (float) (Math.PI * 2);
            int x = (int)(player.method_23317() + class_3532.method_15362(f) * 32.0F);
            int z = (int)(player.method_23321() + class_3532.method_15374(f) * 32.0F);
            int y = this.world.method_8624(class_2903.field_13202, x, z);
            class_2338 pos = new class_2338(x, y, z);
            if (class_1948.method_8660(class_1319.field_6317, this.world, pos, t)) {
               pillager.method_5814(x, y, z);
               pillager.method_5980(player);
               WorldUtils.spawnEntity(this.world, pillager, class_3730.field_16467);
               break;
            }
         }
      }
   }

   public void reportBuilding(class_2338 pos) {
      this.cache.add(pos);
      this.buildingQueue.add(pos);
   }

   public Building.validationResult processBuilding(class_2338 pos) {
      return this.processBuilding(pos, false, true);
   }

   private BuildingType getGroupedBuildingType(class_2338 pos) {
      class_2248 block = this.world.method_8320(pos).method_26204();
      class_2960 blockId = class_7923.field_41175.method_10221(block);

      for (BuildingType bt : BuildingTypes.getInstance()) {
         if (bt.grouped() && bt.matchesBlock(blockId)) {
            return bt;
         }
      }

      return null;
   }

   private Set<class_2338> getBlockedSet(Village village) {
      return village.getBuildings().values().stream().filter(b -> !b.getBuildingType().grouped()).map(Building::getSourceBlock).collect(Collectors.toSet());
   }

   public Building.validationResult processBuilding(class_2338 pos, boolean enforce, boolean strictScan) {
      Optional<Village> optionalVillage = this.findNearestVillage(pos, 64);
      BuildingType groupedBuildingType = this.getGroupedBuildingType(pos);
      Set<class_2338> blocked = new HashSet<>();
      boolean found = false;
      List<Integer> toRemove = new LinkedList<>();
      if (optionalVillage.isPresent()) {
         Village village = optionalVillage.get();
         blocked = this.getBlockedSet(village);
         if (groupedBuildingType != null) {
            String name = groupedBuildingType.name();
            double range = groupedBuildingType.mergeRange() * groupedBuildingType.mergeRange();
            Optional<Building> building = village.getBuildings()
               .values()
               .stream()
               .filter(b -> b.getType().equals(name))
               .min((a, b) -> (int)(a.getCenter().method_10262(pos) - b.getCenter().method_10262(pos)))
               .filter(b -> b.getCenter().method_10262(pos) < range);
            if (building.isPresent()) {
               found = true;
               building.get().addPOI(this.world, pos);
               this.method_80();
            }
         } else {
            for (Building b : village.getBuildings().values()) {
               if (b.containsPos(pos)) {
                  if (!enforce) {
                     found = true;
                  }

                  if ((enforce || this.world.method_8510() - b.getLastScan() > 4800L)
                     && b.validateBuilding(this.world, blocked) != Building.validationResult.SUCCESS) {
                     toRemove.add(b.getId());
                  }
               }
            }
         }

         for (int id : toRemove) {
            village.removeBuilding(id);
            this.method_80();
         }

         if (village.getBuildings().isEmpty()) {
            this.villages.remove(village.getId());
            optionalVillage = Optional.empty();
            this.method_80();
         }
      }

      if (!found && !blocked.contains(pos)) {
         Village village = optionalVillage.orElse(new Village(this.lastVillageId++, this.world));
         Building building = new Building(pos, strictScan);
         if (groupedBuildingType != null) {
            building.setType(groupedBuildingType.name());
            building.addPOI(this.world, pos);
         } else {
            Building.validationResult result = building.validateBuilding(this.world, blocked);
            if (result != Building.validationResult.SUCCESS) {
               return result;
            }

            if (village.getBuildings().values().stream().anyMatch(b -> b.isIdentical(building))) {
               return Building.validationResult.IDENTICAL;
            }
         }

         this.villages.put(village.getId(), village);
         building.setId(this.lastBuildingId++);
         village.getBuildings().put(building.getId(), building);
         village.calculateDimensions();
         this.villages
            .values()
            .stream()
            .filter(v -> v != village)
            .filter(v -> v.getBox().method_35410(64).method_14657(village.getBox()))
            .findAny()
            .ifPresent(v -> {
               if (v.getPopulation() > village.getPopulation()) {
                  this.merge(v, village);
                  this.villages.remove(village.getId());
               } else {
                  this.merge(village, v);
                  this.villages.remove(v.getId());
               }
            });
         this.method_80();
      }

      return Building.validationResult.SUCCESS;
   }

   public void setBuildingCooldown(int buildingCooldown) {
      this.buildingCooldown = buildingCooldown;
   }

   public void merge(Village into, Village from) {
      into.merge(from);
   }
}
