package quilt.net.mca;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_304;
import net.minecraft.class_3675.class_307;

public class KeyBindings {
   public static final List<class_304> list = new LinkedList<>();
   public static final class_304 SKIN_LIBRARY = newKey("skin_library", 85);

   private static class_304 newKey(String name, int code) {
      class_304 key = new class_304("key.mca." + name, class_307.field_1668, code, "itemGroup.mca.mca_tab");
      list.add(key);
      return key;
   }
}
