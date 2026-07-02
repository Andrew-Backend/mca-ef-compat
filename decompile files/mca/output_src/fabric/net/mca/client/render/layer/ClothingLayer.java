package fabric.net.mca.client.render.layer;

import fabric.net.mca.client.gui.immersive_library.SkinCache;
import fabric.net.mca.client.model.CommonVillagerModel;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_572;

public class ClothingLayer<T extends class_1309, M extends class_572<T>> extends VillagerLayer<T, M> {
   private final String variant;

   public ClothingLayer(class_3883<T, M> renderer, M model, String variant) {
      super(renderer, model);
      this.variant = variant;
   }

   @Override
   public class_2960 getSkin(T villager) {
      String v = CommonVillagerModel.getVillager(villager).isBurned() ? "burnt" : this.variant;
      String identifier = CommonVillagerModel.getVillager(villager).getClothes();
      return identifier.startsWith("immersive_library:")
         ? SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring(18)))
         : this.cached(identifier + v, clothes -> {
            class_2960 id = new class_2960(CommonVillagerModel.getVillager(villager).getClothes());
            class_2960 idNew = new class_2960(id.method_12836(), id.method_12832().replace("normal", v));
            return this.canUse(idNew) ? idNew : id;
         });
   }
}
