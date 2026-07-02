package fabric.net.mca.fabric.resources;

import fabric.net.mca.entity.interaction.gifts.GiftLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.class_2960;

public class FabricGiftLoader extends GiftLoader implements IdentifiableResourceReloadListener {
   public class_2960 getFabricId() {
      return ID;
   }
}
