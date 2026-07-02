package quilt.net.mca.network.s2c;

import java.util.List;
import java.util.Map;
import java.util.Set;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.network.NbtDataMessage;
import quilt.net.mca.resources.BuildingTypes;
import quilt.net.mca.resources.Rank;
import quilt.net.mca.resources.Tasks;
import quilt.net.mca.resources.data.BuildingType;
import quilt.net.mca.resources.data.tasks.Task;
import quilt.net.mca.server.world.data.Village;

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
