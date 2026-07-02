package quilt.net.mca.server.world.data.villageComponents;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Memories;
import quilt.net.mca.server.world.data.Village;

public class VillageMarriageManager {
   private final Village village;

   public VillageMarriageManager(Village village) {
      this.village = village;
   }

   public void marry(class_3218 world) {
      if (!(world.field_9229.method_43057() >= Config.getInstance().marriageChancePerMinute)) {
         List<VillagerEntityMCA> allVillagers = this.village.getResidents(world);
         List<VillagerEntityMCA> availableVillagers = allVillagers.stream()
            .filter(v -> !v.method_6109())
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
               .filter(i -> !suitor.getRelationships().getFamilyEntry().isRelative(i.method_5667()))
               .findFirst()
               .ifPresent(
                  mate -> {
                     suitor.getRelationships().marry(mate);
                     mate.getRelationships().marry(suitor);
                     if (Config.getInstance().villagerMarriageNotification) {
                        this.village.broadCastMessage(world, "events.marry", suitor, mate);
                     }

                     this.village
                        .getCivilRegistry()
                        .ifPresent(r -> r.addText(class_2561.method_43469("events.marry", new Object[]{suitor.method_5477(), mate.method_5477()})));
                  }
               );
         }
      }
   }
}
