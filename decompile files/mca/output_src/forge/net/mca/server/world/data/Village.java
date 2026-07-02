package forge.net.mca.server.world.data;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.resources.API;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.server.world.data.villageComponents.VillageGuardsManager;
import forge.net.mca.server.world.data.villageComponents.VillageInnManager;
import forge.net.mca.server.world.data.villageComponents.VillageMarriageManager;
import forge.net.mca.server.world.data.villageComponents.VillageProcreationManager;
import forge.net.mca.server.world.data.villageComponents.VillageTaxesManager;
import forge.net.mca.util.BlockBoxExtended;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Village implements Iterable<Building> {
   private static final int MOVE_IN_COOLDOWN = 1200;
   public static final int PLAYER_BORDER_MARGIN = 32;
   public static final int BORDER_MARGIN = 48;
   public static final int MERGE_MARGIN = 64;
   private static final long BED_SYNC_TIME = 200L;
   private final ServerLevel world;
   private String name = API.getVillagePool().pickVillageName("village");
   public final List<ItemStack> storageBuffer = new LinkedList<>();
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

   public Village(int id, ServerLevel world) {
      this.id = id;
      this.world = world;
   }

   public Village(CompoundTag v, ServerLevel world) {
      this.id = v.m_128451_("id");
      this.name = v.m_128461_("name");
      this.taxes = v.m_128457_("taxesFloat");
      this.beds = v.m_128451_("beds");
      this.unspentHearts = NbtHelper.toMap(v.m_128469_("unspentHearts"), UUID::fromString, ix -> ((IntTag)ix).m_7047_());
      this.reputation = NbtHelper.toMap(
         v.m_128469_("reputation"), UUID::fromString, ix -> NbtHelper.toMap((CompoundTag)ix, UUID::fromString, i2 -> ((IntTag)i2).m_7047_())
      );
      this.residentNames = NbtHelper.toMap(v.m_128469_("residentNames"), UUID::fromString, Tag::m_7916_);
      this.residentHomes = NbtHelper.toMap(v.m_128469_("residentHomes"), UUID::fromString, ix -> ((LongTag)ix).m_7046_());
      this.unspentMood = v.m_128451_("unspentMood");
      if (v.m_128441_("populationThresholdFloat")) {
         this.populationThreshold = v.m_128457_("populationThresholdFloat");
      }

      if (v.m_128441_("marriageThresholdFloat")) {
         this.marriageThreshold = v.m_128457_("marriageThresholdFloat");
      }

      this.world = world;
      if (v.m_128441_("autoScan")) {
         this.autoScan = v.m_128471_("autoScan");
      } else {
         this.autoScan = true;
      }

      ListTag b = v.m_128437_("buildings", 10);

      for (int i = 0; i < b.size(); i++) {
         Building building = new Building(b.m_128728_(i));
         if (world == null || BuildingTypes.getInstance().getBuildingTypes().containsKey(building.getType())) {
            this.buildings.put(building.getId(), building);
         }
      }

      if (!this.buildings.isEmpty()) {
         this.calculateDimensions();
      }
   }

   public static Optional<Village> findNearest(Entity entity) {
      return VillageManager.get((ServerLevel)entity.m_9236_()).findNearestVillage(entity);
   }

   public boolean isWithinBorder(Entity entity) {
      return this.isWithinBorder(entity.m_20183_(), entity instanceof Player ? 32 : 48);
   }

   public boolean isWithinBorder(BlockPos pos, int margin) {
      return this.box.m_191961_(margin).m_71051_(pos);
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

   public Optional<Building> getBuildingAt(Vec3i pos) {
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
         ex = Math.max(building.getPos1().m_123341_(), ex);
         sx = Math.min(building.getPos0().m_123341_(), sx);
         ey = Math.max(building.getPos1().m_123342_(), ey);
         sy = Math.min(building.getPos0().m_123342_(), sy);
         ez = Math.max(building.getPos1().m_123343_(), ez);
         sz = Math.min(building.getPos0().m_123343_(), sz);
      }

      this.box = new BlockBoxExtended(sx, sy, sz, ex, ey, ez);
   }

   public Vec3i getCenter() {
      return this.box.m_162394_();
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
               .filter(e -> value.containsPos(BlockPos.m_122022_(e.getValue())))
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

   public boolean isPositionValidBed(BlockPos pos) {
      return this.getBuildingAt(pos).filter(b -> b.getBuildingType().noBeds()).isEmpty();
   }

   public List<VillagerEntityMCA> getResidents(ServerLevel world) {
      return this.getResidentsUUIDs()
         .<Entity>map(world::m_8791_)
         .filter(VillagerEntityMCA.class::isInstance)
         .map(VillagerEntityMCA.class::cast)
         .collect(Collectors.toList());
   }

   public void updateMaxPopulation() {
      if (this.world != null) {
         Vec3i dimensions = this.box.m_71053_();
         int radius = (int)Math.sqrt(
            dimensions.m_123341_() * dimensions.m_123341_() + dimensions.m_123342_() * dimensions.m_123342_() + dimensions.m_123343_() * dimensions.m_123343_()
         );
         this.beds = (int)this.world
            .m_8904_()
            .m_27138_(
               registryEntry -> registryEntry.m_203565_(PoiTypes.f_218060_),
               this::isPositionValidBed,
               new BlockPos(this.getCenter()),
               radius + 48,
               Occupancy.ANY
            )
            .count();
      }
   }

   public int getMaxPopulation() {
      if (this.world != null && this.world.m_46467_() - this.lastBedSync > 200L) {
         this.lastBedSync = this.world.m_46467_();
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

   public void tick(ServerLevel world, long time) {
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

   public void onEnter(ServerLevel world) {
      this.villageTaxesManager.deliverTaxes(world);
   }

   public void broadCastMessage(ServerLevel world, String event, VillagerEntityMCA suitor, VillagerEntityMCA mate) {
      world.m_6907_()
         .stream()
         .filter(
            p -> PlayerSaveData.get(p).getLastSeenVillageId().orElse(-2) == this.getId()
               || suitor.getVillagerBrain().getMemoriesForPlayer(p).getHearts() > Config.getInstance().heartsToBeConsideredAsFriend
               || mate.getVillagerBrain().getMemoriesForPlayer(p).getHearts() > Config.getInstance().heartsToBeConsideredAsFriend
         )
         .forEach(
            player -> player.m_5661_(Component.m_237110_(event, new Object[]{suitor.m_7755_(), mate.m_7755_()}), !Config.getInstance().showNotificationsAsChat)
         );
   }

   public void broadCastMessage(ServerLevel world, String event, String targetName) {
      world.m_6907_()
         .stream()
         .filter(p -> PlayerSaveData.get(p).getLastSeenVillageId().orElse(-2) == this.getId())
         .forEach(player -> player.m_5661_(Component.m_237110_(event, new Object[]{targetName}), !Config.getInstance().showNotificationsAsChat));
   }

   public void markDirty() {
      VillageManager.get(this.world).m_77762_();
   }

   public void cleanReputation() {
      Set<UUID> residents = this.getResidentsUUIDs().collect(Collectors.toSet());

      for (Map<UUID, Integer> map : this.reputation.values()) {
         for (UUID uuid : map.keySet().stream().filter(v -> !residents.contains(v)).collect(Collectors.toSet())) {
            map.remove(uuid);
         }
      }
   }

   public void setReputation(Player player, VillagerEntityMCA villager, int rep) {
      this.reputation.computeIfAbsent(player.m_20148_(), i -> new HashMap<>()).put(villager.m_20148_(), rep);
      this.markDirty();
   }

   public int getReputation(Player player) {
      return this.reputation.getOrDefault(player.m_20148_(), Collections.emptyMap()).values().stream().mapToInt(i -> i).sum()
         + this.unspentHearts.getOrDefault(player.m_20148_(), 0);
   }

   public void resetHearts(Player player) {
      this.unspentHearts.remove(player.m_20148_());
      this.markDirty();
   }

   public void pushHearts(Player player, int rep) {
      this.pushHearts(player.m_20148_(), rep);
      this.markDirty();
   }

   public void pushHearts(UUID player, int rep) {
      this.unspentHearts.put(player, this.unspentHearts.getOrDefault(player, 0) + rep);
      this.markDirty();
   }

   public int popHearts(Player player) {
      int v = this.unspentHearts.getOrDefault(player.m_20148_(), 0);
      int step = (int)Math.ceil(Math.abs((double)v / this.getPopulation()));
      if (v > 0) {
         v -= step;
         if (v == 0) {
            this.unspentHearts.remove(player.m_20148_());
         } else {
            this.unspentHearts.put(player.m_20148_(), v);
         }

         this.markDirty();
         return step;
      } else if (v < 0) {
         v += step;
         if (v == 0) {
            this.unspentHearts.remove(player.m_20148_());
         } else {
            this.unspentHearts.put(player.m_20148_(), v);
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

   public CompoundTag save() {
      CompoundTag v = new CompoundTag();
      v.m_128405_("id", this.id);
      v.m_128359_("name", this.name);
      v.m_128350_("taxesFloat", this.taxes);
      v.m_128405_("beds", this.beds);
      v.m_128365_("unspentHearts", NbtHelper.fromMap(new CompoundTag(), this.unspentHearts, UUID::toString, IntTag::m_128679_));
      v.m_128365_(
         "reputation",
         NbtHelper.fromMap(new CompoundTag(), this.reputation, UUID::toString, i -> NbtHelper.fromMap(new CompoundTag(), i, UUID::toString, IntTag::m_128679_))
      );
      v.m_128365_("residentNames", NbtHelper.fromMap(new CompoundTag(), this.residentNames, Object::toString, StringTag::m_129297_));
      v.m_128365_("residentHomes", NbtHelper.fromMap(new CompoundTag(), this.residentHomes, Object::toString, LongTag::m_128882_));
      v.m_128405_("unspentMood", this.unspentMood);
      v.m_128350_("populationThresholdFloat", this.populationThreshold);
      v.m_128350_("marriageThresholdFloat", this.marriageThreshold);
      v.m_128365_("buildings", NbtHelper.fromList(this.buildings.values(), Building::save));
      v.m_128379_("autoScan", this.autoScan);
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
      this.residentNames.put(e.m_20148_(), e.m_7755_().getString());
      Optional<GlobalPos> home = e.getResidency().getHome();
      if (home.isPresent()) {
         this.residentHomes.put(e.m_20148_(), home.get().m_122646_().m_121878_());
      } else {
         this.residentHomes.remove(e.m_20148_());
      }
   }

   public Map<UUID, String> getResidentNames() {
      return this.residentNames;
   }

   public boolean hasResident(UUID id) {
      return this.residentNames.containsKey(id);
   }

   public void removeResident(VillagerEntityMCA villager) {
      this.removeResident(villager.m_20148_());
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
