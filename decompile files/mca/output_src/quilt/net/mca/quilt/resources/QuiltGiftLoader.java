package quilt.net.mca.quilt.resources;

import net.minecraft.class_2960;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import quilt.net.mca.entity.interaction.gifts.GiftLoader;

public class QuiltGiftLoader extends GiftLoader implements IdentifiableResourceReloader {
   @NotNull
   public class_2960 getQuiltId() {
      return ID;
   }
}
