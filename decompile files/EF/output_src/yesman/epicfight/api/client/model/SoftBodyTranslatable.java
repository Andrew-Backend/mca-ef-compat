package yesman.epicfight.api.client.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.physics.SimulationProvider;

public interface SoftBodyTranslatable
   extends SimulationProvider<ClothSimulatable, ClothSimulator.ClothObject, ClothSimulator.ClothObjectBuilder, SoftBodyTranslatable> {
   List<ClothSimulatable> TRACKING_SIMULATION_SUBJECTS = Lists.newArrayList();

   default boolean canStartSoftBodySimulation() {
      return this.getSoftBodySimulationInfo() != null;
   }

   void putSoftBodySimulationInfo(Map<String, SoftBodyTranslatable.ClothSimulationInfo> var1);

   Map<String, SoftBodyTranslatable.ClothSimulationInfo> getSoftBodySimulationInfo();

   default StaticMesh<?> getOriginalMesh() {
      return this instanceof Meshes.MeshAccessor<?> meshAccessor ? (StaticMesh)meshAccessor.get() : (StaticMesh)this;
   }

   record ClothSimulationInfo(
      float particleMass,
      float selfCollision,
      List<int[]> constraints,
      ClothSimulator.ClothObject.ClothPart.ConstraintType[] constraintTypes,
      float[] compliances,
      int[] particles,
      float[] weights,
      float[] rootDistance,
      int[] normalOffsetMapping
   ) {
   }
}
