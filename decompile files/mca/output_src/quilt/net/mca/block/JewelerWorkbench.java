package quilt.net.mca.block;

import net.minecraft.class_1263;
import net.minecraft.class_1264;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1750;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2383;
import net.minecraft.class_2415;
import net.minecraft.class_2470;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2753;
import net.minecraft.class_2769;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import net.minecraft.class_2689.class_2690;
import net.minecraft.class_4970.class_2251;
import org.jetbrains.annotations.Nullable;

public class JewelerWorkbench extends class_2248 {
   public static final class_2753 FACING = class_2383.field_11177;
   protected static final class_265 SHAPE = class_2248.method_9541(1.0, 0.1, 1.0, 15.0, 24.0, 15.0);

   public JewelerWorkbench(class_2251 properties) {
      super(properties);
   }

   public class_1269 method_9534(class_2680 state, class_1937 world, class_2338 pos, class_1657 player, class_1268 hand, class_3965 rayTrace) {
      return world.field_9236 ? class_1269.field_5812 : class_1269.field_21466;
   }

   protected void method_9515(class_2690<class_2248, class_2680> builder) {
      builder.method_11667(new class_2769[]{FACING});
   }

   public class_265 method_9530(class_2680 state, class_1922 worldIn, class_2338 pos, class_3726 context) {
      return SHAPE;
   }

   @Nullable
   public class_2680 method_9605(class_1750 context) {
      return (class_2680)this.method_9564().method_11657(FACING, context.method_8042().method_10153());
   }

   public class_2680 method_9598(class_2680 state, class_2470 rot) {
      return (class_2680)state.method_11657(FACING, rot.method_10503((class_2350)state.method_11654(FACING)));
   }

   public class_2680 method_9569(class_2680 state, class_2415 mirrorIn) {
      return state.method_26186(mirrorIn.method_10345((class_2350)state.method_11654(FACING)));
   }

   @Deprecated
   public void method_9536(class_2680 state, class_1937 world, class_2338 pos, class_2680 newState, boolean isMoving) {
      if (!state.method_27852(newState.method_26204())) {
         if (world.method_8321(pos) instanceof class_1263 invEntity) {
            class_1264.method_5451(world, pos, invEntity);
            world.method_8455(pos, this);
         }

         super.method_9536(state, world, pos, newState, isMoving);
      }
   }
}
