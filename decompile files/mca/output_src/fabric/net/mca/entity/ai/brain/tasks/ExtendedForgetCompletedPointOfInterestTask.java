package fabric.net.mca.entity.ai.brain.tasks;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_1309;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3481;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4158;
import net.minecraft.class_4208;
import net.minecraft.class_4209;
import net.minecraft.class_6880;
import net.minecraft.class_7894;
import net.minecraft.class_7898;

public class ExtendedForgetCompletedPointOfInterestTask {
   private static final int MAX_RANGE = 16;

   public static class_7894<class_1309> create(
      Predicate<class_6880<class_4158>> poiTypePredicate, class_4140<class_4208> poiPosModule, Consumer<class_1309> onFinish
   ) {
      return class_7898.method_47224(context -> context.group(context.method_47244(poiPosModule)).apply(context, poiPos -> (world, entity, time) -> {
         class_4208 globalPos = (class_4208)context.method_47243(poiPos);
         class_2338 blockPos = globalPos.method_19446();
         if (world.method_27983() == globalPos.method_19442() && blockPos.method_19769(entity.method_19538(), 16.0)) {
            class_3218 serverWorld = world.method_8503().method_3847(globalPos.method_19442());
            if (serverWorld == null || !serverWorld.method_19494().method_19116(blockPos, poiTypePredicate)) {
               poiPos.method_47252();
            } else if (isBedOccupiedByOthers(serverWorld, blockPos, entity)) {
               poiPos.method_47252();
               world.method_19494().method_19129(blockPos);
               class_4209.method_19778(world, blockPos);
            }

            return true;
         } else {
            if (entity.method_18868().method_18876(poiPosModule, class_4141.field_18457)) {
               onFinish.accept(entity);
            }

            return false;
         }
      }));
   }

   private static boolean isBedOccupiedByOthers(class_3218 world, class_2338 pos, class_1309 entity) {
      class_2680 blockState = world.method_8320(pos);
      return blockState.method_26164(class_3481.field_16443) && (Boolean)blockState.method_11654(class_2244.field_9968) && !entity.method_6113();
   }
}
