package fabric.net.mca.resources;

import java.util.List;
import java.util.Optional;
import net.minecraft.class_5819;

public interface PoolUtil {
   static <T> Optional<T> pop(List<T> selection, class_5819 rng) {
      return Optional.ofNullable(popOne(selection, null, rng));
   }

   static <T> T popOne(List<T> selection, T def, class_5819 rng) {
      return selection.isEmpty() ? def : selection.remove(rng.method_43048(selection.size()));
   }

   static <T> Optional<T> pick(List<T> selection, class_5819 rng) {
      return Optional.ofNullable(pickOne(selection, null, rng));
   }

   static <T> T pickOne(List<T> selection, T def, class_5819 rng) {
      return selection.isEmpty() ? def : selection.get(rng.method_43048(selection.size()));
   }

   static <T> T pickOne(T[] selection, T def, class_5819 rng) {
      return selection.length == 0 ? def : selection[rng.method_43048(selection.length)];
   }
}
