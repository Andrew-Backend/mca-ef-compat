package forge.net.mca.entity.ai;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.server.world.data.GraveyardManager;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillageManager;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CParameter;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;

public class Residency {
   private static final CDataParameter<Integer> VILLAGE = CParameter.create("buildings", -1);
   private final VillagerEntityMCA entity;

   public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return builder.addAll(VILLAGE);
   }

   public Residency(VillagerEntityMCA entity) {
      this.entity = entity;
   }

   public BlockPos getWorkplace() {
      return this.entity.m_6274_().m_257414_(MemoryModuleType.f_26360_).<BlockPos>map(GlobalPos::m_122646_).orElse(BlockPos.f_121853_);
   }

   public void setWorkplace(ServerPlayer player) {
      PoiManager pointOfInterestStorage = ((ServerLevel)player.m_9236_()).m_8904_();
      pointOfInterestStorage.m_148658_(VillagerProfession.f_35585_.f_219629_(), a -> true, this.entity.m_20183_(), 8, Occupancy.HAS_SPACE)
         .ifPresentOrElse(
            blockPos -> pointOfInterestStorage.m_27177_(blockPos)
               .ifPresent(
                  pointOfInterestType -> {
                     pointOfInterestStorage.m_217946_(
                        VillagerProfession.f_35585_.f_219629_(), (registryEntry, blockPos2) -> blockPos2.equals(blockPos), blockPos, 1
                     );
                     this.entity.m_35428_(MemoryModuleType.f_26361_);
                     this.entity.m_6274_().m_21936_(MemoryModuleType.f_26361_);
                     this.entity.m_35428_(MemoryModuleType.f_26360_);
                     this.entity.m_6274_().m_21936_(MemoryModuleType.f_26360_);
                     GlobalPos globalPos = GlobalPos.m_122643_(player.m_9236_().m_46472_(), blockPos);
                     this.entity.m_6274_().m_21879_(MemoryModuleType.f_26360_, globalPos);
                     player.m_9236_().m_7605_(this.entity, (byte)14);
                     MinecraftServer minecraftServer = player.m_9236_().m_7654_();
                     Optional.ofNullable(minecraftServer.m_129880_(globalPos.m_122640_()))
                        .flatMap(world -> world.m_8904_().m_27177_(globalPos.m_122646_()))
                        .flatMap(
                           registryEntry -> BuiltInRegistries.f_256735_
                              .m_123024_()
                              .filter(profession -> profession.f_219628_().test(registryEntry))
                              .findFirst()
                        )
                        .ifPresent(profession -> {
                           VillagerProfession oldProfession = this.entity.m_7141_().m_35571_();
                           if (oldProfession != profession) {
                              int level = this.entity.m_7141_().m_35576_();
                              this.entity.m_34375_(this.entity.m_7141_().m_35565_(profession).m_35561_(1));
                              this.entity.m_35476_(null);
                              this.entity.m_6616_();

                              for (int l = 1; l < level; l++) {
                                 this.entity.customLevelUp();
                              }

                              this.entity.m_35483_((ServerLevel)player.m_9236_());
                           }
                        });
                     this.entity.sendChatMessage(player, "interaction.setworkplace.success");
                  }
               ),
            () -> this.entity.sendChatMessage(player, "interaction.setworkplace.failed")
         );
   }

   public Optional<Village> getHomeVillage() {
      VillageManager manager = VillageManager.get((ServerLevel)this.entity.m_9236_());
      return manager.getOrEmpty(this.entity.getTrackedValue(VILLAGE));
   }

   public void seekHome() {
      if (this.entity.requiresHome()) {
         VillageManager manager = VillageManager.get((ServerLevel)this.entity.m_9236_());
         Optional<Village> current = this.getHomeVillage();
         Optional<Village> target = this.getHome()
            .filter(home -> home.m_122640_() == this.entity.m_9236_().m_46472_())
            .flatMap(home -> manager.findNearestVillage(home.m_122646_(), 48))
            .or(() -> current)
            .or(() -> manager.findNearestVillage(this.entity));
         target.ifPresent(v -> {
            if (current.filter(existing -> existing.getId() == v.getId()).isEmpty()) {
               this.leaveHome();
            }

            v.updateResident(this.entity);
            this.entity.setTrackedValue(VILLAGE, v.getId());
         });
      }
   }

   public void leaveHome() {
      Optional<Village> village = this.getHomeVillage();
      village.ifPresent(v -> v.removeResident(this.entity));
      this.entity.setTrackedValue(VILLAGE, -1);
   }

   public void tick() {
      if (this.entity.f_19797_ % 600 == 0 && this.entity.requiresHome()) {
         Optional<Village> village = this.getHomeVillage();
         if (village.isEmpty() && Config.getInstance().enableAutoScanByDefault || village.filter(Village::isAutoScan).isPresent()) {
            this.reportBuildings();
         }

         if (village.isEmpty()) {
            this.seekHome();
         }
      }

      if (this.entity.f_19797_ % 1200 == 0) {
         this.getHomeVillage().ifPresentOrElse(villagex -> {
            int mood = villagex.popMood();
            if (mood != 0) {
               this.entity.getVillagerBrain().modifyMoodValue(mood);
            }

            this.entity.m_9236_().m_6907_().forEach(player -> {
               int rep = villagex.popHearts(player);
               if (rep != 0) {
                  this.entity.getVillagerBrain().getMemoriesForPlayer(player).modHearts(rep);
               }
            });
            this.entity.m_9236_().m_6907_().forEach(player -> {
               int hearts = this.entity.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
               villagex.setReputation(player, this.entity, hearts);
            });
         }, this::leaveHome);
      }
   }

   private void reportBuildings() {
      VillageManager manager = VillageManager.get((ServerLevel)this.entity.m_9236_());
      Stream<BlockPos> stream = ((ServerLevel)this.entity.m_9236_())
         .m_8904_()
         .m_27138_(type -> true, p -> !manager.cache.contains(p), this.entity.m_20183_(), 48, Occupancy.ANY);
      stream.forEach(manager::reportBuilding);
      GraveyardManager.get((ServerLevel)this.entity.m_9236_()).reportToVillageManager(this.entity);
   }

   public void setHome(ServerPlayer player) {
      if (!this.entity.requiresHome()) {
         this.entity.sendChatMessage(player, "interaction.sethome.temporary");
      } else {
         VillageManager manager = VillageManager.get((ServerLevel)player.m_9236_());
         manager.processBuilding(player.m_20183_(), true, false);
         this.seekHome();
         PoiManager pointOfInterestStorage = ((ServerLevel)player.m_9236_()).m_8904_();
         Optional<BlockPos> position = pointOfInterestStorage.m_27138_(
               registryEntry -> registryEntry.m_203565_(PoiTypes.f_218060_), p -> true, player.m_20183_(), 8, Occupancy.HAS_SPACE
            )
            .findAny();
         if (position.isPresent()) {
            this.entity.sendChatMessage(player, "interaction.sethome.success");
            this.entity.m_6274_().m_257414_(MemoryModuleType.f_26359_).ifPresent(p -> {
               this.entity.m_35428_(MemoryModuleType.f_26359_);
               this.entity.m_6274_().m_21936_(MemoryModuleType.f_26359_);
            });
            pointOfInterestStorage.m_217946_(registryEntry -> registryEntry.m_203565_(PoiTypes.f_218060_), (p, q) -> true, position.get(), 1);
            this.entity.m_6274_().m_21879_(MemoryModuleType.f_26359_, GlobalPos.m_122643_(this.entity.m_9236_().m_46472_(), position.get()));
            this.entity.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.FORCED_HOME.get(), true);
            this.seekHome();
         } else {
            this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.FORCED_HOME.get());
            this.getHomeVillage()
               .map(v -> v.getBuildingAt(this.entity.m_20183_()))
               .filter(Optional::isPresent)
               .map(Optional::get)
               .filter(b -> b.getBuildingType().noBeds())
               .ifPresentOrElse(
                  building -> this.entity.sendChatMessage(player, "interaction.sethome.bedfail." + building.getBuildingType().name()),
                  () -> this.entity.sendChatMessage(player, "interaction.sethome.bedfail")
               );
         }
      }
   }

   public Optional<GlobalPos> getHome() {
      return this.entity.getMCABrain().m_257414_(MemoryModuleType.f_26359_);
   }

   public void goHome(Player player) {
      this.entity.getVillagerBrain().setMoveState(MoveState.MOVE, player);
      this.entity.getInteractions().stopInteracting();
      this.getHome().filter(p -> p.m_122640_() == this.entity.m_9236_().m_46472_()).ifPresentOrElse(home -> {
         this.entity.moveTowards(home.m_122646_());
         this.entity.sendChatMessage(player, "interaction.gohome.success");
      }, () -> this.entity.sendChatMessage(player, "interaction.gohome.fail.nohome"));
   }
}
