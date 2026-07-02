package fabric.net.mca.entity.ai;

import fabric.net.mca.Config;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.server.world.data.GraveyardManager;
import fabric.net.mca.server.world.data.Village;
import fabric.net.mca.server.world.data.VillageManager;
import fabric.net.mca.util.network.datasync.CDataManager;
import fabric.net.mca.util.network.datasync.CDataParameter;
import fabric.net.mca.util.network.datasync.CParameter;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3852;
import net.minecraft.class_4140;
import net.minecraft.class_4153;
import net.minecraft.class_4208;
import net.minecraft.class_7477;
import net.minecraft.class_7923;
import net.minecraft.class_4153.class_4155;
import net.minecraft.server.MinecraftServer;

public class Residency {
   private static final CDataParameter<Integer> VILLAGE = CParameter.create("buildings", -1);
   private final VillagerEntityMCA entity;

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return builder.addAll(VILLAGE);
   }

   public Residency(VillagerEntityMCA entity) {
      this.entity = entity;
   }

   public class_2338 getWorkplace() {
      return this.entity.method_18868().method_46873(class_4140.field_18439).<class_2338>map(class_4208::method_19446).orElse(class_2338.field_10980);
   }

   public void setWorkplace(class_3222 player) {
      class_4153 pointOfInterestStorage = ((class_3218)player.method_37908()).method_19494();
      pointOfInterestStorage.method_34712(class_3852.field_17051.comp_820(), a -> true, this.entity.method_24515(), 8, class_4155.field_18487)
         .ifPresentOrElse(
            blockPos -> pointOfInterestStorage.method_19132(blockPos)
               .ifPresent(
                  pointOfInterestType -> {
                     pointOfInterestStorage.method_19126(
                        class_3852.field_17051.comp_820(), (registryEntry, blockPos2) -> blockPos2.equals(blockPos), blockPos, 1
                     );
                     this.entity.method_19176(class_4140.field_25160);
                     this.entity.method_18868().method_18875(class_4140.field_25160);
                     this.entity.method_19176(class_4140.field_18439);
                     this.entity.method_18868().method_18875(class_4140.field_18439);
                     class_4208 globalPos = class_4208.method_19443(player.method_37908().method_27983(), blockPos);
                     this.entity.method_18868().method_18878(class_4140.field_18439, globalPos);
                     player.method_37908().method_8421(this.entity, (byte)14);
                     MinecraftServer minecraftServer = player.method_37908().method_8503();
                     Optional.ofNullable(minecraftServer.method_3847(globalPos.method_19442()))
                        .flatMap(world -> world.method_19494().method_19132(globalPos.method_19446()))
                        .flatMap(
                           registryEntry -> class_7923.field_41195.method_10220().filter(profession -> profession.comp_819().test(registryEntry)).findFirst()
                        )
                        .ifPresent(profession -> {
                           class_3852 oldProfession = this.entity.method_7231().method_16924();
                           if (oldProfession != profession) {
                              int level = this.entity.method_7231().method_16925();
                              this.entity.method_7195(this.entity.method_7231().method_16921(profession).method_16920(1));
                              this.entity.method_16917(null);
                              this.entity.method_8264();

                              for (int l = 1; l < level; l++) {
                                 this.entity.customLevelUp();
                              }

                              this.entity.method_19179((class_3218)player.method_37908());
                           }
                        });
                     this.entity.sendChatMessage(player, "interaction.setworkplace.success");
                  }
               ),
            () -> this.entity.sendChatMessage(player, "interaction.setworkplace.failed")
         );
   }

   public Optional<Village> getHomeVillage() {
      VillageManager manager = VillageManager.get((class_3218)this.entity.method_37908());
      return manager.getOrEmpty(this.entity.getTrackedValue(VILLAGE));
   }

   public void seekHome() {
      if (this.entity.requiresHome()) {
         VillageManager manager = VillageManager.get((class_3218)this.entity.method_37908());
         Optional<Village> current = this.getHomeVillage();
         Optional<Village> target = this.getHome()
            .filter(home -> home.method_19442() == this.entity.method_37908().method_27983())
            .flatMap(home -> manager.findNearestVillage(home.method_19446(), 48))
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
      if (this.entity.field_6012 % 600 == 0 && this.entity.requiresHome()) {
         Optional<Village> village = this.getHomeVillage();
         if (village.isEmpty() && Config.getInstance().enableAutoScanByDefault || village.filter(Village::isAutoScan).isPresent()) {
            this.reportBuildings();
         }

         if (village.isEmpty()) {
            this.seekHome();
         }
      }

      if (this.entity.field_6012 % 1200 == 0) {
         this.getHomeVillage().ifPresentOrElse(villagex -> {
            int mood = villagex.popMood();
            if (mood != 0) {
               this.entity.getVillagerBrain().modifyMoodValue(mood);
            }

            this.entity.method_37908().method_18456().forEach(player -> {
               int rep = villagex.popHearts(player);
               if (rep != 0) {
                  this.entity.getVillagerBrain().getMemoriesForPlayer(player).modHearts(rep);
               }
            });
            this.entity.method_37908().method_18456().forEach(player -> {
               int hearts = this.entity.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
               villagex.setReputation(player, this.entity, hearts);
            });
         }, this::leaveHome);
      }
   }

   private void reportBuildings() {
      VillageManager manager = VillageManager.get((class_3218)this.entity.method_37908());
      Stream<class_2338> stream = ((class_3218)this.entity.method_37908())
         .method_19494()
         .method_21647(type -> true, p -> !manager.cache.contains(p), this.entity.method_24515(), 48, class_4155.field_18489);
      stream.forEach(manager::reportBuilding);
      GraveyardManager.get((class_3218)this.entity.method_37908()).reportToVillageManager(this.entity);
   }

   public void setHome(class_3222 player) {
      if (!this.entity.requiresHome()) {
         this.entity.sendChatMessage(player, "interaction.sethome.temporary");
      } else {
         VillageManager manager = VillageManager.get((class_3218)player.method_37908());
         manager.processBuilding(player.method_24515(), true, false);
         this.seekHome();
         class_4153 pointOfInterestStorage = ((class_3218)player.method_37908()).method_19494();
         Optional<class_2338> position = pointOfInterestStorage.method_21647(
               registryEntry -> registryEntry.method_40225(class_7477.field_39291), p -> true, player.method_24515(), 8, class_4155.field_18487
            )
            .findAny();
         if (position.isPresent()) {
            this.entity.sendChatMessage(player, "interaction.sethome.success");
            this.entity.method_18868().method_46873(class_4140.field_18438).ifPresent(p -> {
               this.entity.method_19176(class_4140.field_18438);
               this.entity.method_18868().method_18875(class_4140.field_18438);
            });
            pointOfInterestStorage.method_19126(registryEntry -> registryEntry.method_40225(class_7477.field_39291), (p, q) -> true, position.get(), 1);
            this.entity.method_18868().method_18878(class_4140.field_18438, class_4208.method_19443(this.entity.method_37908().method_27983(), position.get()));
            this.entity.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.FORCED_HOME.get(), true);
            this.seekHome();
         } else {
            this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.FORCED_HOME.get());
            this.getHomeVillage()
               .map(v -> v.getBuildingAt(this.entity.method_24515()))
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

   public Optional<class_4208> getHome() {
      return this.entity.getMCABrain().method_46873(class_4140.field_18438);
   }

   public void goHome(class_1657 player) {
      this.entity.getVillagerBrain().setMoveState(MoveState.MOVE, player);
      this.entity.getInteractions().stopInteracting();
      this.getHome().filter(p -> p.method_19442() == this.entity.method_37908().method_27983()).ifPresentOrElse(home -> {
         this.entity.moveTowards(home.method_19446());
         this.entity.sendChatMessage(player, "interaction.gohome.success");
      }, () -> this.entity.sendChatMessage(player, "interaction.gohome.fail.nohome"));
   }
}
