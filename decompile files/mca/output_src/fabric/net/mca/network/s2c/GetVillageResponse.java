package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.network.NbtDataMessage;
import fabric.net.mca.resources.BuildingTypes;
import fabric.net.mca.resources.Rank;
import fabric.net.mca.resources.Tasks;
import fabric.net.mca.resources.data.BuildingType;
import fabric.net.mca.resources.data.tasks.Task;
import fabric.net.mca.server.world.data.Village;
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
