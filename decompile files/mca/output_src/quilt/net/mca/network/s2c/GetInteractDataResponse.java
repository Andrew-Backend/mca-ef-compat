package quilt.net.mca.network.s2c;

import java.util.Set;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.entity.ai.relationship.RelationshipState;
import quilt.net.mca.entity.interaction.Constraint;

public class GetInteractDataResponse implements Message {
   private static final long serialVersionUID = -4168503424192658779L;
   public final Set<Constraint> constraints;
   public final String father;
   public final String mother;
   public final String spouse;
   public final RelationshipState marriageState;

   public GetInteractDataResponse(Set<Constraint> constraints, String father, String mother, String spouse, RelationshipState marriageState) {
      this.constraints = constraints;
      this.father = father;
      this.mother = mother;
      this.spouse = spouse;
      this.marriageState = marriageState;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleInteractDataResponse(this);
   }
}
