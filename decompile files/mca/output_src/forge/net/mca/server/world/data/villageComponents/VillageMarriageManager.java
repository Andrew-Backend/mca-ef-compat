package forge.net.mca.server.world.data.villageComponents;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.server.world.data.Village;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class VillageMarriageManager {
   private final Village village;

   public VillageMarriageManager(Village village) {
      this.village = village;
   }

   public void marry(ServerLevel world) {
      if (!(world.f_46441_.m_188501_() >= Config.getInstance().marriageChancePerMinute)) {
         List<VillagerEntityMCA> allVillagers = this.village.getResidents(world);
         List<VillagerEntityMCA> availableVillagers = allVillagers.stream()
            .filter(v -> !v.m_6162_())
            .filter(v -> !v.getRelationships().isMarried())
            .filter(v -> !v.getRelationships().isEngaged())
            .filter(v -> !v.getRelationships().isPromised())
            .collect(Collectors.toList());
         if (availableVillagers.size() > 1 && !(availableVillagers.size() <= allVillagers.size() * (1.0 - this.village.getMarriageThreshold()))) {
            availableVillagers.sort(
               Comparator.comparingInt(a -> a.getVillagerBrain().getMemories().values().stream().map(Memories::getHearts).max(Integer::compare).orElse(0))
            );
            VillagerEntityMCA suitor = availableVillagers.remove(0);
            availableVillagers.stream()
               .filter(suitor::canBeAttractedTo)
               .filter(i -> !suitor.getRelationships().getFamilyEntry().isRelative(i.m_20148_()))
               .findFirst()
               .ifPresent(mate -> {
                  suitor.getRelationships().marry(mate);
                  mate.getRelationships().marry(suitor);
                  if (Config.getInstance().villagerMarriageNotification) {
                     this.village.broadCastMessage(world, "events.marry", suitor, mate);
                  }

                  this.village
                     .getCivilRegistry()
                     .ifPresent(r -> r.addText(Component.m_237110_("events.marry", new Object[]{suitor.m_7755_(), mate.m_7755_()})));
               });
         }
      }
   }
}
