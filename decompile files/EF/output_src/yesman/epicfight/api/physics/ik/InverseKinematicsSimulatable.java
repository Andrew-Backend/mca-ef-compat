package yesman.epicfight.api.physics.ik;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.physics.SimulatableObject;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public interface InverseKinematicsSimulatable extends SimulatableObject {
   float getRootXRot();

   float getRootXRotO();

   float getRootZRot();

   float getRootZRotO();

   OpenMatrix4f getModelMatrix(float var1);

   InverseKinematicsSimulator getIKSimulator();

   Entity toEntity();
}
