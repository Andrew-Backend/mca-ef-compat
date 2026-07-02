package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.entity.interaction.Constraint;
import java.util.Set;

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
