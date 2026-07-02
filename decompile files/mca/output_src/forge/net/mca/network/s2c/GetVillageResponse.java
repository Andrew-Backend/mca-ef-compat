package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.network.NbtDataMessage;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.Tasks;
import forge.net.mca.resources.data.BuildingType;
import forge.net.mca.resources.data.tasks.Task;
import forge.net.mca.server.world.data.Village;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetVillageResponse extends NbtDataMessage {
   private static final long serialVersionUID = 4882425683460617550L;
   public final Rank rank;
   public final int reputation;
   public final boolean isVillage;
   public final Set<String> ids;
   public final Map<Rank, List<Task>> tasks;
   public final Map<String, BuildingType> buildingTypes;

   public GetVillageResponse(Village data, Rank rank, int reputation, boolean isVillage, Set<String> ids) {
      super(data.save());
      this.rank = rank;
      this.reputation = reputation;
      this.isVillage = isVillage;
      this.ids = ids;
      this.tasks = Tasks.getInstance().tasks;
      this.buildingTypes = BuildingTypes.getInstance().getServerBuildingTypes();
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleVillageDataResponse(this);
   }
}
