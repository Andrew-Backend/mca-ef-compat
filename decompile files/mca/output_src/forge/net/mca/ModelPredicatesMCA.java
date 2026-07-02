package forge.net.mca;

import forge.net.mca.item.BabyItem;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.item.SirbenBabyItem;
import forge.net.mca.item.VillagerTrackerItem;
import forge.net.mca.util.network.datasync.CDataParameter;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public interface ModelPredicatesMCA {
   static void setup(CDataParameter.TriConsumer<Item, ResourceLocation, ClampedItemPropertyFunction> register) {
      register.accept(
         (Item)ItemsMCA.BABY_BOY.get(), new ResourceLocation("invalidated"), (stack, world, entity, i) -> BabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (Item)ItemsMCA.BABY_GIRL.get(), new ResourceLocation("invalidated"), (stack, world, entity, i) -> BabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (Item)ItemsMCA.SIRBEN_BABY_BOY.get(),
         new ResourceLocation("invalidated"),
         (stack, world, entity, i) -> SirbenBabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (Item)ItemsMCA.SIRBEN_BABY_GIRL.get(),
         new ResourceLocation("invalidated"),
         (stack, world, entity, i) -> SirbenBabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (Item)ItemsMCA.VILLAGER_TRACKER.get(),
         new ResourceLocation("angle"),
         new CompassItemPropertyFunction((world, stack, entity) -> VillagerTrackerItem.getTargetPos(stack))
      );
   }
}
