package yesman.epicfight.client.gui.datapack.widgets;

import java.util.function.Consumer;
import javax.annotation.Nullable;

public interface DataBindingComponent<T, R> extends ResizableComponent {
   void reset();

   T _getValue();

   void _setValue(@Nullable T var1);

   void _setResponder(Consumer<R> var1);

   Consumer<R> _getResponder();
}
