package forge.net.mca.server.world.data;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.resources.data.BuildingType;
import forge.net.mca.server.ReaperSpawner;
import forge.net.mca.server.SpawnQueue;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.saveddata.SavedData;

public class VillageManager extends SavedData implements Iterable<Village> {
   private final Map<Integer, Village> villages = new HashMap<>();
   public final Set<BlockPos> cache = ConcurrentHashMap.newKeySet();
   private final List<BlockPos> buildingQueue = new LinkedList<>();
   private int lastBuildingId;
   private int lastVillageId;
   private final ServerLevel world;
   private final ReaperSpawner reapers;
   private int buildingCooldown = 21;

   public static VillageManager get(ServerLevel world) {
      return WorldUtils.loadData(world, nbt -> new VillageManager(world, nbt), VillageManager::new, "mca_villages");
   }

   VillageManager(ServerLevel world) {
      this.world = world;
      this.reapers = new ReaperSpawner(this);
   }

   VillageManager(ServerLevel world, CompoundTag nbt) {
      this.world = world;
      this.lastBuildingId = nbt.m_128451_("lastBuildingId");
      this.lastVillageId = nbt.m_128451_("lastVillageId");
      this.reapers = nbt.m_128425_("reapers", 10) ? new ReaperSpawner(this, nbt.m_128469_("reapers")) : new ReaperSpawner(this);
      ListTag villageList = nbt.m_128437_("villages", 10);

      for (int i = 0; i < villageList.size(); i++) {
         Village village = new Village(villageList.m_128728_(i), world);
         if (village.getBuildings().isEmpty()) {
            MCA.LOGGER.warn("Empty village detected (" + village.getName() + "), removing...");
            this.m_77762_();
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

   public Optional<Village> findNearestVillage(Entity entity) {
      BlockPos p = entity.m_20183_();
      return this.findVillages(v -> v.isWithinBorder(entity)).min((a, b) -> (int)(a.getCenter().m_123331_(p) - b.getCenter().m_123331_(p)));
   }

   public Optional<Village> findNearestVillage(BlockPos p, int margin) {
      return this.findVillages(v -> v.isWithinBorder(p, margin)).min((a, b) -> (int)(a.getCenter().m_123331_(p) - b.getCenter().m_123331_(p)));
   }

   public boolean isWithinHorizontalBoundaries(BlockPos p) {
      return this.villages.values().stream().anyMatch(v -> v.getBox().expand(0, 1000, 0).m_71051_(p));
   }

   public CompoundTag m_7176_(CompoundTag nbt) {
      nbt.m_128405_("lastBuildingId", this.lastBuildingId);
      nbt.m_128405_("lastVillageId", this.lastVillageId);
      nbt.m_128365_("villages", NbtHelper.fromList(this.villages.values(), Village::save));
      nbt.m_128365_("reapers", this.reapers.writeNbt());
      return nbt;
   }

   public void tick() {
      if (this.world.m_46468_() % 100L == 0L) {
         this.world.m_6907_().forEach(player -> PlayerSaveData.get(player).updateLastSeenVillage(this, player));
      }

      if (this.world.m_46468_() % (Config.getInstance().bountyHunterInterval / 10) == 0L && this.world.m_46791_() != Difficulty.PEACEFUL) {
         this.world
            .m_6907_()
            .forEach(
               player -> {
                  if (this.world.f_46441_.m_188503_(10) == 0 && !this.isWithinHorizontalBoundaries(player.m_20183_()) && !player.m_7500_()) {
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

      long time = this.world.m_46467_();

      for (Village v : this) {
         v.tick(this.world, time);
      }

      if (time % this.buildingCooldown == 0L && !this.buildingQueue.isEmpty()) {
         this.processBuilding(this.buildingQueue.remove(0));
      }

      this.reapers.tick(this.world);
      SpawnQueue.getInstance().tick();
   }

   private void startBountyHunterWave(ServerPlayer player, Village sender) {
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
         if (this.world.f_46441_.m_188499_()) {
            this.spawnBountyHunter(EntityType.f_20513_, player);
         } else {
            this.spawnBountyHunter(EntityType.f_20493_, player);
         }
      }

      player.m_5661_(
         Component.m_237110_(sender.getPopulation() == 0 ? "events.bountyHuntersFinal" : "events.bountyHunters", new Object[]{sender.getName()})
            .m_130940_(ChatFormatting.RED),
         false
      );
      sender.getCivilRegistry().ifPresent(r -> r.addText(Component.m_237110_("civil_registry.bounty_hunters", new Object[]{player.m_7755_()})));
   }

   private <T extends AbstractIllager> void spawnBountyHunter(EntityType<T> t, ServerPlayer player) {
      AbstractIllager pillager = (AbstractIllager)t.m_20615_(this.world);
      if (pillager != null) {
         for (int attempt = 0; attempt < 32; attempt++) {
            float f = this.world.f_46441_.m_188501_() * (float) (Math.PI * 2);
            int x = (int)(player.m_20185_() + Mth.m_14089_(f) * 32.0F);
            int z = (int)(player.m_20189_() + Mth.m_14031_(f) * 32.0F);
            int y = this.world.m_6924_(Types.WORLD_SURFACE, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (NaturalSpawner.m_47051_(Type.ON_GROUND, this.world, pos, t)) {
               pillager.m_6034_(x, y, z);
               pillager.m_6710_(player);
               WorldUtils.spawnEntity(this.world, pillager, MobSpawnType.EVENT);
               break;
            }
         }
      }
   }

   public void reportBuilding(BlockPos pos) {
      this.cache.add(pos);
      this.buildingQueue.add(pos);
   }

   public Building.validationResult processBuilding(BlockPos pos) {
      return this.processBuilding(pos, false, true);
   }

   private BuildingType getGroupedBuildingType(BlockPos pos) {
      Block block = this.world.m_8055_(pos).m_60734_();
      ResourceLocation blockId = BuiltInRegistries.f_256975_.m_7981_(block);

      for (BuildingType bt : BuildingTypes.getInstance()) {
         if (bt.grouped() && bt.matchesBlock(blockId)) {
            return bt;
         }
      }

      return null;
   }

   private Set<BlockPos> getBlockedSet(Village village) {
      return village.getBuildings().values().stream().filter(b -> !b.getBuildingType().grouped()).map(Building::getSourceBlock).collect(Collectors.toSet());
   }

   public Building.validationResult processBuilding(BlockPos pos, boolean enforce, boolean strictScan) {
      Optional<Village> optionalVillage = this.findNearestVillage(pos, 64);
      BuildingType groupedBuildingType = this.getGroupedBuildingType(pos);
      Set<BlockPos> blocked = new HashSet<>();
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
               .min((a, b) -> (int)(a.getCenter().m_123331_(pos) - b.getCenter().m_123331_(pos)))
               .filter(b -> b.getCenter().m_123331_(pos) < range);
            if (building.isPresent()) {
               found = true;
               building.get().addPOI(this.world, pos);
               this.m_77762_();
            }
         } else {
            for (Building b : village.getBuildings().values()) {
               if (b.containsPos(pos)) {
                  if (!enforce) {
                     found = true;
                  }

                  if ((enforce || this.world.m_46467_() - b.getLastScan() > 4800L)
                     && b.validateBuilding(this.world, blocked) != Building.validationResult.SUCCESS) {
                     toRemove.add(b.getId());
                  }
               }
            }
         }

         for (int id : toRemove) {
            village.removeBuilding(id);
            this.m_77762_();
         }

         if (village.getBuildings().isEmpty()) {
            this.villages.remove(village.getId());
            optionalVillage = Optional.empty();
            this.m_77762_();
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
         this.villages.values().stream().filter(v -> v != village).filter(v -> v.getBox().m_191961_(64).m_71049_(village.getBox())).findAny().ifPresent(v -> {
            if (v.getPopulation() > village.getPopulation()) {
               this.merge(v, village);
               this.villages.remove(village.getId());
            } else {
               this.merge(village, v);
               this.villages.remove(v.getId());
            }
         });
         this.m_77762_();
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
