package quilt.net.mca.entity.ai.brain.tasks;

import com.google.gson.JsonSyntaxException;
import net.minecraft.class_1308;
import net.minecraft.class_14;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_4112;
import net.minecraft.class_4140;
import net.minecraft.class_6862;
import net.minecraft.class_7;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import quilt.net.mca.Config;
import quilt.net.mca.util.RegistryHelper;

public class WanderOrTeleportToTargetTask extends class_4112 {
   private static final int SLOWDOWN = 5;
   private int cooldown = 5;

   protected boolean method_18978(class_3218 serverWorld, class_1308 mobEntity) {
      if (this.cooldown < 0) {
         this.cooldown = 5;
         return super.method_18978(serverWorld, mobEntity);
      } else {
         this.cooldown--;
         return false;
      }
   }

   protected void method_18983(class_3218 world, class_1308 entity, long l) {
      if (Config.getInstance().allowVillagerTeleporting) {
         entity.method_18868().method_46873(class_4140.field_18445).ifPresent(walkTarget -> {
            class_2338 targetPos = walkTarget.method_19094().method_18989();
            if (!targetPos.method_19769(entity.method_19538(), Config.getInstance().villagerMinTeleportationDistance)) {
               this.tryTeleport(world, entity, targetPos);
            }
         });
      }

      super.method_18983(world, entity, l);
   }

   private void tryTeleport(class_3218 world, class_1308 entity, class_2338 targetPos) {
      for (int i = 0; i < 10; i++) {
         int j = this.getRandomInt(entity, -3, 3);
         int k = this.getRandomInt(entity, -1, 1);
         int l = this.getRandomInt(entity, -3, 3);
         boolean bl = this.tryTeleportTo(world, entity, targetPos, targetPos.method_10263() + j, targetPos.method_10264() + k, targetPos.method_10260() + l);
         if (bl) {
            return;
         }
      }
   }

   private boolean tryTeleportTo(class_3218 world, class_1308 entity, class_2338 targetPos, int x, int y, int z) {
      if (Math.abs((double)x - targetPos.method_10263()) < 2.0 && Math.abs((double)z - targetPos.method_10260()) < 2.0) {
         return false;
      }

      if (!this.canTeleportTo(world, entity, new class_2338(x, y, z))) {
         return false;
      }

      entity.method_5859(x + 0.5, y, z + 0.5);
      return true;
   }

   private boolean canTeleportTo(class_3218 world, class_1308 entity, class_2338 pos) {
      class_7 pathNodeType = class_14.method_23476(world, pos.method_25503());
      if (pathNodeType != class_7.field_12) {
         return false;
      }

      if (!this.isAreaSafe(world, pos.method_10074())) {
         return false;
      }

      class_2338 blockPos = pos.method_10059(entity.method_24515());
      return world.method_8587(entity, entity.method_5829().method_996(blockPos));
   }

   private int getRandomInt(class_1308 entity, int min, int max) {
      return entity.method_6051().method_43048(max - min + 1) + min;
   }

   private boolean isAreaSafe(class_3218 world, class_2338 pos) {
      class_2680 aboveState = world.method_8320(pos);
      class_2960 aboveId = class_7923.field_41175.method_10221(aboveState.method_26204());

      for (String blockId : Config.getInstance().villagerPathfindingBlacklist) {
         if (blockId.equals(aboveId.toString())) {
            return false;
         }

         if (blockId.charAt(0) == '#') {
            class_2960 identifier = new class_2960(blockId.substring(1));
            class_6862<class_2248> tag = class_6862.method_40092(class_7924.field_41254, identifier);
            if (tag == null || RegistryHelper.isTagEmpty(tag)) {
               throw new JsonSyntaxException("Unknown block tag in villagerPathfindingBlacklist '" + identifier + "'");
            }

            if (aboveState.method_26164(tag)) {
               return false;
            }
         }
      }

      return true;
   }
}
