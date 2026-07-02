package quilt.net.mca.quilt.client.gui;

import net.minecraft.class_2960;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import quilt.net.mca.client.gui.MCAScreens;

public class QuiltMCAScreens extends MCAScreens implements IdentifiableResourceReloader {
   @NotNull
   public class_2960 getQuiltId() {
      return ID;
   }
}
