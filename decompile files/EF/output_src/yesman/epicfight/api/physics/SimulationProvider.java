package yesman.epicfight.api.physics;

import javax.annotation.Nullable;

public interface SimulationProvider<OWN, OBJ extends SimulationObject<?, ?, ?>, BUILDER extends SimulationObject.SimulationObjectBuilder, P extends SimulationProvider<OWN, OBJ, BUILDER, P>> {
   OBJ createSimulationData(@Nullable P var1, OWN var2, BUILDER var3);
}
