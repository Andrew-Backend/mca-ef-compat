package forge.net.mca.server.world.data.villageComponents;

import forge.net.mca.Config;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.resources.Names;
import forge.net.mca.server.world.data.Village;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.BlockGetter;

public class VillageInnManager {
   private final Village village;

   public VillageInnManager(Village village) {
      this.village = village;
   }

   public void updateInn(ServerLevel world) {
      this.village.getBuildingsOfType("inn").forEach(b -> {
         if (world.f_46441_.m_188501_() < Config.getInstance().adventurerAtInnChancePerMinute) {
            List<BlockPos> values = new ArrayList<>(b.getBlocks().values().stream().flatMap(Collection::stream).toList());
            Collections.shuffle(values);

            for (BlockPos p : values) {
               if (this.trySpawnAdventurer(world, p.m_7494_())) {
                  break;
               }
            }
         }
      });
   }

   private boolean doesNotSuffocateAt(BlockGetter world, BlockPos pos) {
      for (BlockPos blockPos : BlockPos.m_121940_(pos, pos.m_7494_())) {
         if (!world.m_8055_(blockPos).m_60812_(world, blockPos).m_83281_()) {
            return false;
         }
      }

      return true;
   }

   private boolean trySpawnAdventurer(ServerLevel world, BlockPos blockPos) {
      if (!world.m_143340_(blockPos)) {
         return true;
      }

      String name = null;
      if (this.doesNotSuffocateAt(world, blockPos)) {
         int i = world.f_46441_.m_188503_(10);
         if (i == 0 && Config.getInstance().innSpawnsWanderingTraders) {
            WanderingTrader trader = (WanderingTrader)EntityType.f_20494_.m_262496_(world, blockPos, MobSpawnType.EVENT);
            if (trader != null) {
               name = trader.m_7755_().getString();
               trader.m_35891_(Config.getInstance().adventurerStayTime);
            }
         } else if (i == 1 && Config.getInstance().innSpawnsCultists) {
            VillagerEntityMCA adventurer = this.spawnInnVillager(world, blockPos, Gender.getRandom());
            if (adventurer != null) {
               name = adventurer.m_7755_().getString();
               adventurer.setProfession((VillagerProfession)ProfessionsMCA.CULTIST.get());
               adventurer.setDespawnDelay(Config.getInstance().adventurerStayTime);
            }
         } else if (Config.getInstance().innSpawnsAdventurers) {
            VillagerEntityMCA adventurer = this.spawnInnVillager(world, blockPos, Gender.getRandom());
            if (adventurer != null) {
               name = adventurer.m_7755_().getString();
               adventurer.setProfession((VillagerProfession)ProfessionsMCA.ADVENTURER.get());
               adventurer.setDespawnDelay(Config.getInstance().adventurerStayTime);
            }
         }

         if (name != null) {
            if (Config.getInstance().innArrivalNotification) {
               this.village.broadCastMessage(world, "events.arrival.inn", name);
            }

            return true;
         }
      }

      return false;
   }

   private VillagerEntityMCA spawnInnVillager(ServerLevel world, BlockPos blockPos, Gender gender) {
      VillagerEntityMCA adventurer = (VillagerEntityMCA)gender.getVillagerType().m_262496_(world, blockPos, MobSpawnType.EVENT);
      if (adventurer != null) {
         adventurer.m_6593_(Component.m_237113_(Names.pickCitizenName(gender)));
      }

      return adventurer;
   }
}
