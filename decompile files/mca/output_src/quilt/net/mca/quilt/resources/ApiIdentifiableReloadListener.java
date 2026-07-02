package quilt.net.mca.quilt.resources;

import net.minecraft.class_2960;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;
import quilt.net.mca.resources.ApiReloadListener;

public class ApiIdentifiableReloadListener extends ApiReloadListener implements SimpleSynchronousResourceReloader {
   @NotNull
   public class_2960 getQuiltId() {
      return ID;
   }
}
