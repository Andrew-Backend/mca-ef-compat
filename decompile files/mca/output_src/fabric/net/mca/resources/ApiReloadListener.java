package fabric.net.mca.resources;

import fabric.net.mca.MCA;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_4013;

public class ApiReloadListener implements class_4013 {
   public static final class_2960 ID = MCA.locate("api");

   public void method_14491(class_3300 manager) {
      API.instance = new API.Data();
      API.instance.init(manager);
   }
}
