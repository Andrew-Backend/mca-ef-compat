package fabric.net.mca.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import fabric.net.mca.client.resources.Icon;
import fabric.net.mca.resources.Resources;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4309;

public class MCAScreens extends class_4309 {
   protected static final class_2960 ID = new class_2960("mca", "screens");
   private static final Type ICONS_TYPE = (new TypeToken<Map<String, Icon>>() {}).getType();
   private static MCAScreens INSTANCE;
   private final Map<String, Button[]> buttons = new HashMap<>();
   private final Map<String, Icon> icons = new HashMap<>();

   public static MCAScreens getInstance() {
      return INSTANCE;
   }

   public MCAScreens() {
      super(Resources.GSON, "api/gui");
      INSTANCE = this;
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.buttons.clear();
      this.icons.clear();
      data.forEach(this::loadScreen);
   }

   private void loadScreen(class_2960 id, JsonElement element) {
      if (element.isJsonObject()) {
         this.icons.putAll((Map<? extends String, ? extends Icon>)Resources.GSON.fromJson(element, ICONS_TYPE));
      } else {
         this.buttons.put(id.method_12832(), (Button[])Resources.GSON.fromJson(element, Button[].class));
      }
   }

   public Icon getIcon(String key) {
      return this.icons.getOrDefault(key, Icon.EMPTY);
   }

   public Optional<Button[]> getScreen(String guiKey) {
      return Optional.ofNullable(this.buttons.get(guiKey));
   }

   public Optional<Button> getButton(String key, String id) {
      return Arrays.stream(this.buttons.get(key)).filter(b -> b.identifier().equals(id)).findFirst();
   }
}
