package forge.net.mca.resources;

import forge.net.mca.MCA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ApiReloadListener implements ResourceManagerReloadListener {
   public static final ResourceLocation ID = MCA.locate("api");

   public void m_6213_(ResourceManager manager) {
      API.instance = new API.Data();
      API.instance.init(manager);
   }
}
