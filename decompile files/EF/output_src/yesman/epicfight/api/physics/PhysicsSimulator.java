package yesman.epicfight.api.physics;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public interface PhysicsSimulator<KEY, B extends SimulationObject.SimulationObjectBuilder, PV extends SimulationProvider<O, T, B, PV>, O, T extends SimulationObject<B, PV, O>> {
   void tick(O var1);

   boolean isRunning(KEY var1);

   void runUntil(KEY var1, PV var2, B var3, BooleanSupplier var4);

   void runWhen(KEY var1, PV var2, B var3, BooleanSupplier var4);

   void restart(KEY var1);

   void stop(KEY var1);

   Optional<T> getRunningObject(KEY var1);
}
