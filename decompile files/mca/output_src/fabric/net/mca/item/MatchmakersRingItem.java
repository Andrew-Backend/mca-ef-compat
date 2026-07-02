package fabric.net.mca.item;

import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.util.WorldUtils;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.class_1792;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;

public class MatchmakersRingItem extends class_1792 implements SpecialCaseGift {
   public MatchmakersRingItem(class_1793 properties) {
      super(properties);
   }

   @Override
   public boolean handle(class_3222 player, VillagerEntityMCA villager) {
      if (player.method_6047().method_7947() < 2) {
         villager.sendChatMessage(player, "interaction.matchmaker.fail.needtwo");
         return false;
      }

      if (!villager.getRelationships().isMarried() && villager.getAgeState() == AgeState.ADULT) {
         Optional<VillagerEntityMCA> target = WorldUtils.getCloseEntities(villager.method_37908(), villager, 5.0)
            .stream()
            .filter(v -> v != villager && v instanceof VillagerEntityMCA)
            .map(VillagerEntityMCA.class::cast)
            .filter(v -> !v.method_6109() && !v.getRelationships().isMarried())
            .filter(v -> !v.getRelationships().getFamilyEntry().isRelative(villager.method_5667()))
            .filter(villager::canBeAttractedTo)
            .min(Comparator.comparingDouble(villager::method_5739));
         if (target.isEmpty()) {
            villager.sendChatMessage(player, "interaction.matchmaker.fail.novillagers");
            return false;
         }

         VillagerEntityMCA spouse = target.get();
         villager.getRelationships().marry(spouse);
         spouse.getRelationships().marry(villager);
         player.method_37908().method_8421(villager, (byte)12);
         if (!player.method_7337()) {
            player.method_6047().method_7934(1);
         }

         return true;
      } else {
         villager.sendChatMessage(player, "interaction.matchmaker.fail.married");
         return false;
      }
   }
}
