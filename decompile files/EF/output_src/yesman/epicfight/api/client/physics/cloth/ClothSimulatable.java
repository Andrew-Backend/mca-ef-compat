package yesman.epicfight.api.client.physics.cloth;

import javax.annotation.Nullable;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.SimulatableObject;

public interface ClothSimulatable extends SimulatableObject {
   @Nullable
   Armature getArmature();

   @Nullable
   Animator getSimulatableAnimator();

   boolean invalid();

   Vec3 getObjectVelocity();

   float getYRot();

   float getYRotO();

   Vec3 getAccurateCloakLocation(float var1);

   Vec3 getAccuratePartialLocation(float var1);

   float getAccurateYRot(float var1);

   float getYRotDelta(float var1);

   float getScale();

   float getGravity();

   ClothSimulator getClothSimulator();
}
