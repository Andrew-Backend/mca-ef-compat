package forge.net.mca.resources;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;

public interface PoolUtil {
   static <T> Optional<T> pop(List<T> selection, RandomSource rng) {
      return Optional.ofNullable(popOne(selection, null, rng));
   }

   static <T> T popOne(List<T> selection, T def, RandomSource rng) {
      return selection.isEmpty() ? def : selection.remove(rng.m_188503_(selection.size()));
   }

   static <T> Optional<T> pick(List<T> selection, RandomSource rng) {
      return Optional.ofNullable(pickOne(selection, null, rng));
   }

   static <T> T pickOne(List<T> selection, T def, RandomSource rng) {
      return selection.isEmpty() ? def : selection.get(rng.m_188503_(selection.size()));
   }

   static <T> T pickOne(T[] selection, T def, RandomSource rng) {
      return selection.length == 0 ? def : selection[rng.m_188503_(selection.length)];
   }
}
