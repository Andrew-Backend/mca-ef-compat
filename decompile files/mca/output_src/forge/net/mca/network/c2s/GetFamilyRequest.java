package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.network.s2c.GetFamilyResponse;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class GetFamilyRequest implements Message {
   private static final long serialVersionUID = -4415670234855916259L;

   @Override
   public void receive(ServerPlayer player) {
      CompoundTag familyData = new CompoundTag();
      PlayerSaveData playerData = PlayerSaveData.get(player);
      Stream.concat(playerData.getFamilyEntry().getAllRelatives(4), playerData.getPartnerUUID().stream())
         .distinct()
         .<Entity>map(player.m_284548_()::m_8791_)
         .filter(e -> e instanceof VillagerLike)
         .limit(100L)
         .forEach(e -> {
            CompoundTag nbt = new CompoundTag();
            ((Mob)e).m_7380_(nbt);
            nbt.m_128473_("Brain");
            nbt.m_128473_("memories");
            nbt.m_128473_("Inventory");
            familyData.m_128365_(e.m_20148_().toString(), nbt);
         });
      NetworkHandler.sendToPlayer(new GetFamilyResponse(familyData), player);
   }
}
