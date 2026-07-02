package quilt.net.mca.entity.ai;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1743;
import net.minecraft.class_1787;
import net.minecraft.class_1794;
import net.minecraft.class_1810;
import net.minecraft.class_1829;
import net.minecraft.class_2561;
import org.jetbrains.annotations.Nullable;

public enum Chore {
   NONE("none", null),
   PROSPECT("prospecting", class_1810.class),
   HARVEST("harvesting", class_1794.class),
   CHOP("chopping", class_1743.class),
   HUNT("hunting", class_1829.class),
   FISH("fishing", class_1787.class);

   private static final Chore[] VALUES = values();
   private static final Map<String, Chore> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(c -> c.friendlyName, Function.identity()));
   private final String friendlyName;
   @Nullable
   private final Class<?> toolType;

   Chore(String friendlyName, @Nullable Class<?> toolType) {
      this.friendlyName = friendlyName;
      this.toolType = toolType;
   }

   public class_2561 getName() {
      return class_2561.method_43471("gui.label." + this.friendlyName);
   }

   @Nullable
   public Class<?> getToolType() {
      return this.toolType;
   }

   public static Optional<Chore> byCommand(String action) {
      return Optional.ofNullable(REGISTRY.get(action.toLowerCase(Locale.ENGLISH)));
   }

   public static Chore byId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : NONE;
   }
}
