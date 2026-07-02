package quilt.net.mca.entity.ai.chatAI.inworldAIModules;

import net.minecraft.class_3222;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Relationship;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;

public class RelationshipModule {
   public void updateRelationship(Interaction interaction, class_3222 player, VillagerEntityMCA villager) {
      Interaction.RelationshipUpdate update = interaction.relationshipUpdate();
      int weightedTotal = 1 * update.trust() + 1 * update.respect() + 1 * update.familiar() + 1 * update.flirtatious() + 1 * update.attraction();
      int heartsUpdate = weightedTotal / 10;
      villager.getVillagerBrain().rewardHearts(player, heartsUpdate);
   }

   public TriggerEvent.Parameter getRelationshipTriggerParameter(class_3222 player, VillagerEntityMCA villager) {
      return new TriggerEvent.Parameter("relationshipStatus", this.getRelationshipStatus(player, villager).name());
   }

   private RelationshipModule.RelationshipStatus getRelationshipStatus(class_3222 player, VillagerEntityMCA villager) {
      if (!Relationship.IS_ENGAGED.test(villager, player) && !Relationship.IS_MARRIED.test(villager, player)) {
         if (Relationship.IS_PROMISED.test(villager, player)) {
            return RelationshipModule.RelationshipStatus.RELATIONSHIP;
         }

         int heartLevel = villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
         RelationshipModule.RelationshipStatus returnStatus = RelationshipModule.RelationshipStatus.UNKNOWN;

         for (RelationshipModule.RelationshipStatus status : RelationshipModule.RelationshipStatus.values()) {
            if (status.hasStatus(heartLevel)) {
               returnStatus = status;
            }
         }

         return returnStatus;
      } else {
         return RelationshipModule.RelationshipStatus.LIFE_PARTNER;
      }
   }

   private enum RelationshipStatus {
      UNKNOWN(Integer.MIN_VALUE),
      ARCHENEMY(Integer.MIN_VALUE),
      ENEMY(-75),
      ACQUAINTANCE(-15),
      FRIEND(25),
      CLOSE_FRIEND(100),
      DATE(Integer.MAX_VALUE),
      RELATIONSHIP(Integer.MAX_VALUE),
      LIFE_PARTNER(Integer.MAX_VALUE);

      private final int heartThreshold;

      RelationshipStatus(int heartThreshold) {
         this.heartThreshold = heartThreshold;
      }

      public boolean hasStatus(int hearts) {
         return hearts > this.heartThreshold;
      }
   }
}
