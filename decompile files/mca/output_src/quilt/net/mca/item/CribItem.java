package quilt.net.mca.item;

import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1750;
import net.minecraft.class_1767;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_3730;
import net.minecraft.class_5712;
import net.minecraft.class_1792.class_1793;
import quilt.net.mca.entity.CribEntity;
import quilt.net.mca.entity.CribWoodType;
import quilt.net.mca.entity.EntitiesMCA;

public class CribItem extends class_1792 {
   private final CribWoodType wood;
   private final class_1767 color;

   public CribItem(class_1793 settings, CribWoodType wood, class_1767 color) {
      super(settings);
      this.wood = wood;
      this.color = color;
   }

   public CribWoodType getWood() {
      return this.wood;
   }

   public class_1767 getColor() {
      return this.color;
   }

   public class_1269 method_7884(class_1838 context) {
      class_2350 direction = context.method_8038();
      if (direction == class_2350.field_11033) {
         return class_1269.field_5814;
      }

      class_1937 world = context.method_8045();
      class_1750 itemPlacementContext = new class_1750(context);
      class_2338 blockPos = itemPlacementContext.method_8037();
      class_1799 itemStack = context.method_8041();
      class_243 vec3d = class_243.method_24955(blockPos);
      class_238 box = ((class_1299)EntitiesMCA.CRIB.get()).method_18386().method_30231(vec3d.method_10216(), vec3d.method_10214(), vec3d.method_10215());
      if (world.method_8587(null, box) && world.method_8335(null, box).isEmpty()) {
         if (world instanceof class_3218 serverWorld) {
            CribEntity crib = (CribEntity)((class_1299)EntitiesMCA.CRIB.get())
               .method_5888(serverWorld, null, null, blockPos, class_3730.field_16465, true, true);
            crib.setWoodType(this.wood);
            crib.setColor(this.color);
            float f = class_3532.method_15375((class_3532.method_15393(context.method_8044() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            crib.method_5808(crib.method_23317(), crib.method_23318(), crib.method_23321(), f, 0.0F);
            serverWorld.method_30771(crib);
            world.method_43128(null, crib.method_23317(), crib.method_23318(), crib.method_23321(), class_3417.field_14969, class_3419.field_15245, 0.75F, 0.8F);
            crib.method_32875(class_5712.field_28738, context.method_8036());
         }

         itemStack.method_7934(1);
         return class_1269.method_29236(world.field_9236);
      } else {
         return class_1269.field_5814;
      }
   }
}
