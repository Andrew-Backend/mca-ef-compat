package forge.net.mca;

import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.KeyMapping;

public class KeyBindings {
   public static final List<KeyMapping> list = new LinkedList<>();
   public static final KeyMapping SKIN_LIBRARY = newKey("skin_library", 85);

   private static KeyMapping newKey(String name, int code) {
      KeyMapping key = new KeyMapping("key.mca." + name, Type.KEYSYM, code, "itemGroup.mca.mca_tab");
      list.add(key);
      return key;
   }
}
