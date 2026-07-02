package yesman.epicfight.api.client.physics;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import org.apache.commons.lang3.tuple.Pair;
import yesman.epicfight.api.physics.PhysicsSimulator;
import yesman.epicfight.api.physics.SimulationObject;
import yesman.epicfight.api.physics.SimulationProvider;

public abstract class AbstractSimulator<KEY, B extends SimulationObject.SimulationObjectBuilder, PV extends SimulationProvider<O, SO, B, PV>, O, SO extends SimulationObject<B, PV, O>>
   implements PhysicsSimulator<KEY, B, PV, O, SO> {
   protected Map<KEY, AbstractSimulator<KEY, B, PV, O, SO>.ObjectWrapper> simulationObjects = Maps.newHashMap();

   @Override
   public void tick(O simObject) {
      this.simulationObjects.values().removeIf(keyWrapper -> {
         if (keyWrapper.isRunning()) {
            if (!keyWrapper.runWhen.getAsBoolean()) {
               keyWrapper.stopRunning();
               if (!keyWrapper.permanent) {
                  return true;
               }
            }
         } else if (keyWrapper.runWhen.getAsBoolean()) {
            keyWrapper.startRunning(simObject);
         }

         return false;
      });
   }

   @Override
   public void runUntil(KEY key, PV provider, B builder, BooleanSupplier until) {
      this.simulationObjects.put(key, new AbstractSimulator.ObjectWrapper(provider, until, false, builder));
   }

   @Override
   public void runWhen(KEY key, PV provider, B builder, BooleanSupplier when) {
      this.simulationObjects.put(key, new AbstractSimulator.ObjectWrapper(provider, when, true, builder));
   }

   @Override
   public void stop(KEY key) {
      this.simulationObjects.remove(key);
   }

   @Override
   public void restart(KEY key) {
      AbstractSimulator<KEY, B, PV, O, SO>.ObjectWrapper kwrap = this.simulationObjects.get(key);
      if (kwrap != null) {
         this.stop(key);
         this.simulationObjects.put(key, new AbstractSimulator.ObjectWrapper(kwrap.provider, kwrap.runWhen, kwrap.permanent, kwrap.builder));
      }
   }

   @Override
   public boolean isRunning(KEY key) {
      return this.simulationObjects.containsKey(key) ? this.simulationObjects.get(key).isRunning() : false;
   }

   @Override
   public Optional<SO> getRunningObject(KEY key) {
      return !this.simulationObjects.containsKey(key) ? Optional.empty() : Optional.ofNullable(this.simulationObjects.get(key).simulationObject);
   }

   public List<Pair<KEY, SO>> getAllRunningObjects() {
      return this.simulationObjects
         .entrySet()
         .stream()
         .filter(entry -> entry.getValue().isRunning())
         .map(entry -> Pair.of(entry.getKey(), entry.getValue().simulationObject))
         .toList();
   }

   protected class ObjectWrapper {
      final PV provider;
      final B builder;
      final BooleanSupplier runWhen;
      final boolean permanent;
      SO simulationObject;
      boolean isRunning;

      ObjectWrapper(PV key, BooleanSupplier runWhen, boolean permanent, B builder) {
         this.provider = key;
         this.runWhen = runWhen;
         this.permanent = permanent;
         this.builder = builder;
      }

      public void startRunning(O simObject) {
         this.simulationObject = this.provider.createSimulationData(this.provider, simObject, this.builder);
         if (this.simulationObject != null) {
            this.isRunning = true;
         }
      }

      public void stopRunning() {
         this.isRunning = false;
         this.simulationObject = null;
      }

      public boolean isRunning() {
         return this.isRunning;
      }
   }
}
