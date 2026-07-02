package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.network.s2c.GetFamilyResponse;
import fabric.net.mca.server.world.data.PlayerSaveData;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_2487;
import net.minecraft.class_3222;

public class GetFamilyRequest implements Message {
   private static final long serialVersionUID = -4415670234855916259L;

   @Override
   public void receive(class_3222 player) {
      class_2487 familyData = new class_2487();
      PlayerSaveData playerData = PlayerSaveData.get(player);
      Stream.concat(playerData.getFamilyEntry().getAllRelatives(4), playerData.getPartnerUUID().stream())
         .distinct()
         .<class_1297>map(player.method_51469()::method_14190)
         .filter(e -> e instanceof VillagerLike)
         .limit(100L)
         .forEach(e -> {
            class_2487 nbt = new class_2487();
            ((class_1308)e).method_5652(nbt);
            nbt.method_10551("Brain");
            nbt.method_10551("memories");
            nbt.method_10551("Inventory");
            familyData.method_10566(e.method_5667().toString(), nbt);
         });
      NetworkHandler.sendToPlayer(new GetFamilyResponse(familyData), player);
   }
}
