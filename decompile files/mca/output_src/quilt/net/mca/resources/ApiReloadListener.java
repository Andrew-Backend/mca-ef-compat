package quilt.net.mca.resources;

import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_4013;
import quilt.net.mca.MCA;

public class ApiReloadListener implements class_4013 {
   public static final class_2960 ID = MCA.locate("api");

   public void method_14491(class_3300 manager) {
      API.instance = new API.Data();
      API.instance.init(manager);
   }
}
