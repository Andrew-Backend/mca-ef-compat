package fabric.net.mca.item;

import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.network.s2c.PlayerDataMessage;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import fabric.net.mca.server.world.data.PlayerSaveData;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3417;
import net.minecraft.class_1792.class_1793;

public class PotionOfMetamorphosisItem extends TooltippedItem {
   private final Gender gender;

   public PotionOfMetamorphosisItem(class_1793 properties, Gender gender) {
      super(properties);
      this.gender = gender;
   }

   public final class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
      if (player instanceof class_3222 serverPlayer) {
         PlayerSaveData data = PlayerSaveData.get(serverPlayer);
         class_2487 villagerData = data.getEntityData();
         villagerData.method_10569("gender", this.gender.ordinal());
         data.setEntityData(villagerData);
         this.common(serverPlayer);
         serverPlayer.method_51469().method_18456().forEach(p -> NetworkHandler.sendToPlayer(new PlayerDataMessage(player.method_5667(), villagerData), p));
         class_1799 stack = player.method_5998(hand);
         stack.method_7934(1);
         return class_1271.method_22427(stack);
      } else {
         return super.method_7836(world, player, hand);
      }
   }

   public class_1269 method_7847(class_1799 stack, class_1657 player, class_1309 entity, class_1268 hand) {
      if (entity instanceof VillagerLike<?> villager && !entity.method_37908().field_9236) {
         villager.getGenetics().setGender(this.gender);
         this.common(entity);
         stack.method_7934(1);
         return class_1269.field_5812;
      } else {
         return class_1269.field_21466;
      }
   }

   private void common(class_1297 entity) {
      entity.method_5783(class_3417.field_15168, 1.0F, 1.0F);
      FamilyTree tree = FamilyTree.get((class_3218)entity.method_37908());
      FamilyTreeNode entry = tree.getOrCreate(entity);
      entry.setGender(this.gender);
   }
}
