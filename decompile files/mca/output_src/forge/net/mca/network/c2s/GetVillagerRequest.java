package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.GetVillagerResponse;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class GetVillagerRequest implements Message {
   private static final long serialVersionUID = -4415670234855916259L;
   private final UUID uuid;

   public GetVillagerRequest(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(ServerPlayer player) {
      Entity e = player.m_284548_().m_8791_(this.uuid);
      CompoundTag villagerData = getVillagerData(e);
      if (villagerData != null) {
         NetworkHandler.sendToPlayer(new GetVillagerResponse(villagerData), player);
      }
   }

   private static void storeNode(CompoundTag data, Optional<FamilyTreeNode> entry, String prefix) {
      if (entry.isPresent()) {
         data.m_128359_("tree_" + prefix + "_name", entry.get().getName());
         data.m_128362_("tree_" + prefix + "_uuid", entry.get().id());
      } else {
         data.m_128359_("tree_" + prefix + "_name", "");
         data.m_128362_("tree_" + prefix + "_uuid", Util.f_137441_);
      }
   }

   public static CompoundTag getVillagerData(Entity e) {
      CompoundTag data;
      if (e instanceof ServerPlayer serverPlayer) {
         data = PlayerSaveData.get(serverPlayer).getEntityData();
      } else {
         if (!(e instanceof LivingEntity)) {
            return null;
         }

         data = new CompoundTag();
         ((Mob)e).m_7380_(data);
      }

      FamilyTree tree = FamilyTree.get((ServerLevel)e.m_9236_());
      FamilyTreeNode entry = tree.getOrCreate(e);
      storeNode(data, tree.getOrEmpty(entry.partner()), "spouse");
      storeNode(data, tree.getOrEmpty(entry.father()), "father");
      storeNode(data, tree.getOrEmpty(entry.mother()), "mother");
      return data;
   }
}
