package fabric.net.mca.fabric.resources;

import fabric.net.mca.resources.ApiReloadListener;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;

public class ApiIdentifiableReloadListener extends ApiReloadListener implements SimpleSynchronousResourceReloadListener {
   public class_2960 getFabricId() {
      return ID;
   }
}
