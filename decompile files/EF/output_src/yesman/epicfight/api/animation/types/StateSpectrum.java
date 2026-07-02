package yesman.epicfight.api.animation.types;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class StateSpectrum {
   private final Set<StateSpectrum.StatesInTime> timePairs = Sets.newHashSet();

   void readFrom(StateSpectrum.Blueprint blueprint) {
      this.timePairs.clear();
      this.timePairs.addAll(blueprint.timePairs);
   }

   public <T> T getSingleState(EntityState.StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
      for (StateSpectrum.StatesInTime state : this.timePairs) {
         if (state.isIn(entitypatch, time)) {
            for (Entry<EntityState.StateFactor<?>, ?> timeEntry : state.getStates(entitypatch)) {
               if (timeEntry.getKey() == stateFactor) {
                  return (T)timeEntry.getValue();
               }
            }
         }
      }

      return stateFactor.defaultValue();
   }

   public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStateMap(LivingEntityPatch<?> entitypatch, float time) {
      TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap = new TypeFlexibleHashMap<>(true);

      for (StateSpectrum.StatesInTime state : this.timePairs) {
         if (state.isIn(entitypatch, time)) {
            for (Entry<EntityState.StateFactor<?>, ?> timeEntry : state.getStates(entitypatch)) {
               stateMap.put(timeEntry.getKey(), timeEntry.getValue());
            }
         }
      }

      return stateMap;
   }

   public static class Blueprint {
      StateSpectrum.StatesInTime currentState;
      Set<StateSpectrum.StatesInTime> timePairs = Sets.newHashSet();

      public StateSpectrum.Blueprint newTimePair(float start, float end) {
         this.currentState = new StateSpectrum.SimpleStatesInTime(start, end);
         this.timePairs.add(this.currentState);
         return this;
      }

      public StateSpectrum.Blueprint newConditionalTimePair(Function<LivingEntityPatch<?>, Integer> condition, float start, float end) {
         this.currentState = new StateSpectrum.ConditionalStatesInTime(condition, start, end);
         this.timePairs.add(this.currentState);
         return this;
      }

      public StateSpectrum.Blueprint newVariableTimePair(Function<LivingEntityPatch<?>, Float> variableStart, Function<LivingEntityPatch<?>, Float> variableEnd) {
         this.currentState = new StateSpectrum.VariableStatesInTime(variableStart, variableEnd);
         this.timePairs.add(this.currentState);
         return this;
      }

      public <T> StateSpectrum.Blueprint addState(EntityState.StateFactor<T> factor, T val) {
         if (this.currentState instanceof StateSpectrum.SimpleStatesInTime simpleState) {
            simpleState.addState(factor, val);
         }

         if (this.currentState instanceof StateSpectrum.VariableStatesInTime variableState) {
            variableState.addState(factor, val);
         }

         return this;
      }

      public <T> StateSpectrum.Blueprint addConditionalState(int metadata, EntityState.StateFactor<T> factor, T val) {
         if (this.currentState instanceof StateSpectrum.ConditionalStatesInTime conditionalState) {
            conditionalState.addConditionalState(metadata, factor, val);
         }

         return this;
      }

      public <T> StateSpectrum.Blueprint removeState(EntityState.StateFactor<T> factor) {
         for (StateSpectrum.StatesInTime timePair : this.timePairs) {
            timePair.removeState(factor);
         }

         return this;
      }

      public <T> StateSpectrum.Blueprint addStateRemoveOld(EntityState.StateFactor<T> factor, T val) {
         this.removeState(factor);
         return this.addState(factor, val);
      }

      public <T> StateSpectrum.Blueprint addStateIfNotExist(EntityState.StateFactor<T> factor, T val) {
         for (StateSpectrum.StatesInTime timePair : this.timePairs) {
            if (timePair.hasState(factor)) {
               return this;
            }
         }

         return this.addState(factor, val);
      }

      public StateSpectrum.Blueprint clear() {
         this.currentState = null;
         this.timePairs.clear();
         return this;
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();

         for (StateSpectrum.StatesInTime state : this.timePairs) {
            sb.append(state + "\n");
         }

         return sb.toString();
      }
   }

   static class ConditionalStatesInTime extends StateSpectrum.StatesInTime {
      float start;
      float end;
      Int2ObjectMap<Map<EntityState.StateFactor<?>, Object>> conditionalStates = new Int2ObjectOpenHashMap();
      Function<LivingEntityPatch<?>, Integer> condition;

      public ConditionalStatesInTime(Function<LivingEntityPatch<?>, Integer> condition, float start, float end) {
         this.start = start;
         this.end = end;
         this.condition = condition;
      }

      public <T> StateSpectrum.StatesInTime addConditionalState(int metadata, EntityState.StateFactor<T> factor, T val) {
         Map<EntityState.StateFactor<?>, Object> states = (Map<EntityState.StateFactor<?>, Object>)this.conditionalStates
            .computeIfAbsent(metadata, key -> Maps.newHashMap());
         states.put(factor, val);
         return this;
      }

      @Override
      public Set<Entry<EntityState.StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch) {
         return ((Map)this.conditionalStates.get(this.condition.apply(entitypatch))).entrySet();
      }

      @Override
      public boolean isIn(LivingEntityPatch<?> entitypatch, float time) {
         return this.start <= time && this.end > time;
      }

      @Override
      public boolean hasState(EntityState.StateFactor<?> state) {
         boolean hasState = false;
         ObjectIterator var3 = this.conditionalStates.values().iterator();

         while (var3.hasNext()) {
            Map<EntityState.StateFactor<?>, Object> states = (Map<EntityState.StateFactor<?>, Object>)var3.next();
            hasState |= states.containsKey(state);
         }

         return hasState;
      }

      @Override
      public void removeState(EntityState.StateFactor<?> state) {
         ObjectIterator var2 = this.conditionalStates.values().iterator();

         while (var2.hasNext()) {
            Map<EntityState.StateFactor<?>, Object> states = (Map<EntityState.StateFactor<?>, Object>)var2.next();
            states.remove(state);
         }
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("Time: %.2f ~ %.2f, ", this.start, this.end));
         int entryCnt = 0;
         ObjectIterator var3 = this.conditionalStates.entrySet().iterator();

         while (var3.hasNext()) {
            Entry<Integer, Map<EntityState.StateFactor<?>, Object>> entry = (Entry<Integer, Map<EntityState.StateFactor<?>, Object>>)var3.next();
            sb.append(String.format("States %d: %s", entry.getKey(), entry.getValue()));
            if (++entryCnt < this.conditionalStates.size()) {
               sb.append(", ");
            }
         }

         return sb.toString();
      }
   }

   static class SimpleStatesInTime extends StateSpectrum.StatesInTime {
      float start;
      float end;
      Map<EntityState.StateFactor<?>, Object> states = Maps.newHashMap();

      public SimpleStatesInTime(float start, float end) {
         this.start = start;
         this.end = end;
      }

      @Override
      public boolean isIn(LivingEntityPatch<?> entitypatch, float time) {
         return this.start <= time && this.end > time;
      }

      public <T> StateSpectrum.StatesInTime addState(EntityState.StateFactor<T> factor, T val) {
         this.states.put(factor, val);
         return this;
      }

      @Override
      public Set<Entry<EntityState.StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch) {
         return this.states.entrySet();
      }

      @Override
      public boolean hasState(EntityState.StateFactor<?> state) {
         return this.states.containsKey(state);
      }

      @Override
      public void removeState(EntityState.StateFactor<?> state) {
         this.states.remove(state);
      }

      @Override
      public String toString() {
         return String.format("Time: %.2f ~ %.2f, States: %s", this.start, this.end, this.states);
      }
   }

   abstract static class StatesInTime {
      public abstract Set<Entry<EntityState.StateFactor<?>, Object>> getStates(LivingEntityPatch<?> var1);

      public abstract void removeState(EntityState.StateFactor<?> var1);

      public abstract boolean hasState(EntityState.StateFactor<?> var1);

      public abstract boolean isIn(LivingEntityPatch<?> var1, float var2);
   }

   static class VariableStatesInTime extends StateSpectrum.StatesInTime {
      Function<LivingEntityPatch<?>, Float> variableStart;
      Function<LivingEntityPatch<?>, Float> variableEnd;
      Map<EntityState.StateFactor<?>, Object> states = Maps.newHashMap();

      public VariableStatesInTime(Function<LivingEntityPatch<?>, Float> variableStart, Function<LivingEntityPatch<?>, Float> variableEnd) {
         this.variableStart = variableStart;
         this.variableEnd = variableEnd;
      }

      @Override
      public boolean isIn(LivingEntityPatch<?> entitypatch, float time) {
         return this.variableStart.apply(entitypatch) <= time && this.variableEnd.apply(entitypatch) > time;
      }

      public <T> StateSpectrum.StatesInTime addState(EntityState.StateFactor<T> factor, T val) {
         this.states.put(factor, val);
         return this;
      }

      @Override
      public Set<Entry<EntityState.StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch) {
         return this.states.entrySet();
      }

      @Override
      public boolean hasState(EntityState.StateFactor<?> state) {
         return this.states.containsKey(state);
      }

      @Override
      public void removeState(EntityState.StateFactor<?> state) {
         this.states.remove(state);
      }

      @Override
      public String toString() {
         return String.format("States: %s", this.states);
      }
   }
}
