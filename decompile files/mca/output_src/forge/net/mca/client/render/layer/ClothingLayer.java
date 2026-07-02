package forge.net.mca.client.render.layer;

import forge.net.mca.client.gui.immersive_library.SkinCache;
import forge.net.mca.client.model.CommonVillagerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ClothingLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends VillagerLayer<T, M> {
   private final String variant;

   public ClothingLayer(RenderLayerParent<T, M> renderer, M model, String variant) {
      super(renderer, model);
      this.variant = variant;
   }

   @Override
   public ResourceLocation getSkin(T villager) {
      String v = CommonVillagerModel.getVillager(villager).isBurned() ? "burnt" : this.variant;
      String identifier = CommonVillagerModel.getVillager(villager).getClothes();
      return identifier.startsWith("immersive_library:")
         ? SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring(18)))
         : this.cached(identifier + v, clothes -> {
            ResourceLocation id = new ResourceLocation(CommonVillagerModel.getVillager(villager).getClothes());
            ResourceLocation idNew = new ResourceLocation(id.m_135827_(), id.m_135815_().replace("normal", v));
            return this.canUse(idNew) ? idNew : id;
         });
   }
}
