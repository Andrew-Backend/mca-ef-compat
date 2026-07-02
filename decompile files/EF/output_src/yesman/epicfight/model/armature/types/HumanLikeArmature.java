package yesman.epicfight.model.armature.types;

import yesman.epicfight.api.animation.Joint;

public interface HumanLikeArmature extends ToolHolderArmature {
   Joint leftHandJoint();

   Joint rightHandJoint();

   Joint leftArmJoint();

   Joint rightArmJoint();

   Joint leftLegJoint();

   Joint rightLegJoint();

   Joint leftThighJoint();

   Joint rightThighJoint();

   Joint headJoint();
}
