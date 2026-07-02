package yesman.epicfight.model.armature.types;

import yesman.epicfight.api.animation.Joint;

public interface ToolHolderArmature {
   Joint leftToolJoint();

   Joint rightToolJoint();

   Joint backToolJoint();
}
