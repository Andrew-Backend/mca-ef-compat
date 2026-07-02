package quilt.net.mca.entity.ai;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_2561;

public enum MoveState {
   MOVE("moving"),
   STAY("staying"),
   FOLLOW("following");

   private static final MoveState[] VALUES = values();
   private static final Map<String, MoveState> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(Enum::name, Function.identity()));
   private final String friendlyName;

   MoveState(String friendlyName) {
      this.friendlyName = friendlyName;
   }

   public class_2561 getName() {
      return class_2561.method_43471("gui.label." + this.friendlyName);
   }

   public static Optional<MoveState> byCommand(String action) {
      return Optional.ofNullable(REGISTRY.get(action.toUpperCase(Locale.ENGLISH)));
   }

   public static MoveState byId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : MOVE;
   }
}
