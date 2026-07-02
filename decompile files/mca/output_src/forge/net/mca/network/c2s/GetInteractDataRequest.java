package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.entity.interaction.Constraint;
import forge.net.mca.network.s2c.GetInteractDataResponse;
import forge.net.mca.server.world.data.FamilyTreeNode;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class GetInteractDataRequest implements Message {
   private static final long serialVersionUID = -4363277735373237564L;
   final UUID uuid;

   public GetInteractDataRequest(UUID villager) {
      this.uuid = villager;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_284548_().m_8791_(this.uuid) instanceof VillagerLike<?> villager) {
         Set<Constraint> constraints = Constraint.allMatching(villager, player);
         EntityRelationship relationship = ((CompassionateEntity)villager).getRelationships();
         FamilyTreeNode family = relationship.getFamilyEntry();
         String fatherName = relationship.getFamilyTree().getOrEmpty(family.father()).map(FamilyTreeNode::getName).orElse(null);
         String motherName = relationship.getFamilyTree().getOrEmpty(family.mother()).map(FamilyTreeNode::getName).orElse(null);
         String spouseName = relationship.getFamilyTree().getOrEmpty(family.partner()).map(FamilyTreeNode::getName).orElse(null);
         RelationshipState marriageState = relationship.getRelationshipState();
         NetworkHandler.sendToPlayer(new GetInteractDataResponse(constraints, fatherName, motherName, spouseName, marriageState), player);
      }
   }
}
