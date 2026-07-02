package quilt.net.mca.network.c2s;

import java.util.EnumSet;
import net.minecraft.class_1294;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2709;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3230;
import net.minecraft.class_5535;
import net.minecraft.class_6862;
import net.minecraft.class_7924;
import net.minecraft.class_2902.class_2903;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.util.WorldUtils;
import quilt.net.mca.util.compat.ExtendedFuzzyPositions;

public class DestinyMessage implements Message {
   private static final long serialVersionUID = -782119062565197963L;
   private final String location;
   private final boolean isClosing;

   public DestinyMessage(String location, boolean isClosing) {
      this.location = location;
      this.isClosing = isClosing;
   }

   public DestinyMessage(String location) {
      this(location, false);
   }

   public DestinyMessage(boolean isClosing) {
      this(null, isClosing);
   }

   @Override
   public void receive(class_3222 player) {
      if (this.isClosing) {
         player.method_6016(class_1294.field_5905);
         player.method_6016(class_1294.field_5914);
      }

      if (Config.getInstance().allowDestinyTeleportation && this.location != null) {
         MCA.executorService
            .execute(
               () -> {
                  if (this.location.charAt(0) == '#') {
                     String tagId = this.location.substring(1);
                     WorldUtils.getClosestStructurePosition(
                           player.method_51469(), player.method_24515(), class_6862.method_40092(class_7924.field_41246, new class_2960(tagId)), 128
                        )
                        .ifPresent(pos -> this.handleBlockPos(player, pos));
                  } else {
                     WorldUtils.getClosestStructurePosition(player.method_51469(), player.method_24515(), new class_2960(this.location), 128)
                        .ifPresent(pos -> this.handleBlockPos(player, pos));
                  }
               }
            );
      }
   }

   private void handleBlockPos(class_3222 player, class_2338 pos) {
      player.method_37908().method_8500(pos);
      if (this.location.equals("minecraft:ancient_city")) {
         pos = new class_2338(pos.method_10263(), -50, pos.method_10260());
      } else {
         pos = player.method_37908().method_8598(class_2903.field_13202, pos);
      }

      pos = class_5535.method_31540(pos, player.method_37908().method_31605(), p -> player.method_37908().method_8320(p).method_26228(player.method_37908(), p));
      pos = ExtendedFuzzyPositions.downWhile(pos, 1, p -> !player.method_37908().method_8320(p.method_10074()).method_26234(player.method_37908(), p));
      class_1923 chunkPos = new class_1923(pos);
      player.method_51469().method_14178().method_17297(class_3230.field_19347, chunkPos, 1, player.method_5628());
      player.field_13987
         .method_14360(
            pos.method_10263(), pos.method_10264(), pos.method_10260(), player.method_36454(), player.method_36455(), EnumSet.noneOf(class_2709.class)
         );
      player.method_26284(player.method_37908().method_27983(), pos, 0.0F, true, false);
      if (player.method_37908().method_8503() != null && player.method_37908().method_8503().method_19466(player.method_7334())) {
         player.method_51469().method_8554(pos, 0.0F);
      }
   }
}
