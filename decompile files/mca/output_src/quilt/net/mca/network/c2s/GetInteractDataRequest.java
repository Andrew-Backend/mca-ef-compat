package quilt.net.mca.network.c2s;

import java.util.Set;
import java.util.UUID;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.relationship.CompassionateEntity;
import quilt.net.mca.entity.ai.relationship.EntityRelationship;
import quilt.net.mca.entity.ai.relationship.RelationshipState;
import quilt.net.mca.entity.interaction.Constraint;
import quilt.net.mca.network.s2c.GetInteractDataResponse;
import quilt.net.mca.server.world.data.FamilyTreeNode;

public class GetInteractDataRequest implements Message {
   private static final long serialVersionUID = -4363277735373237564L;
   final UUID uuid;

   public GetInteractDataRequest(UUID villager) {
      this.uuid = villager;
   }

   @Override
   public void receive(class_3222 player) {
      if (player.method_51469().method_14190(this.uuid) instanceof VillagerLike<?> villager) {
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
