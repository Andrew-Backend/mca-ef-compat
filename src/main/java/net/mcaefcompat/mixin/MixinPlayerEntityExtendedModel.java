package net.mcaefcompat.mixin;

import forge.net.mca.client.model.PlayerEntityExtendedModel;
import net.mcaefcompat.McaEfCompat;
import net.mcaefcompat.McaEfCompatState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injected into MCA's PlayerEntityExtendedModel.setupAnim().
 *
 * Root cause of static limbs:
 *   Every frame, the render pipeline calls model.setupAnim() to pose the model
 *   according to the entity's walking/running/attacking state. MCA overrides
 *   this in PlayerEntityExtendedModel and writes its OWN rotations to the shared
 *   ModelPart fields (head, torso, leftArm, rightArm, leftLeg, rightLeg).
 *
 *   EF then reads those same fields in setJointTransforms() / setArmaturePose()
 *   to transfer vanilla pose data into its armature. But MCA has already written
 *   its static genetics-adjusted pose into those fields — EF's animation system
 *   never had a chance to write its own transforms because MCA's setupAnim runs
 *   AFTER EF sets the pose.
 *
 *   Wait — actually the sequence is:
 *     1. vanilla setupAnim() runs first (via prepareVanillaModel in EF's renderer)
 *     2. MCA's setupAnim override runs next, overwriting with MCA static pose
 *     3. EF calls setJointTransforms — but MCA already clobbered the rotations
 *
 *   Result: EF copies MCA's static pose into the armature → limbs don't move.
 *
 * Fix:
 *   When EF is actively rendering this player (tracked via ThreadLocal set in
 *   MixinRenderEngine.renderEntityArmatureModel HEAD), we cancel MCA's
 *   setupAnim so EF gets the clean vanilla pose that IT expects.
 *
 *   MCA's genetics scale is still applied via our MixinRenderEngine (PoseStack
 *   scale), so proportions are preserved. Only the per-frame pose override
 *   is suppressed.
 */
@Mixin(value = PlayerEntityExtendedModel.class, remap = false)
public abstract class MixinPlayerEntityExtendedModel {

    @Inject(
        method = "m_6973_",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mcaefcompat$skipSetupAnimDuringEFRender(
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float animationProgress,
            float headYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        // If EF's render pipeline is active for this thread, cancel MCA's
        // pose override so EF can control the animation properly.
        if (McaEfCompatState.isEfRendering()) {
            McaEfCompat.LOGGER.debug("[MCA-EF] setupAnim cancelled — EF is rendering");
            ci.cancel();
        } else {
            McaEfCompat.LOGGER.debug("[MCA-EF] setupAnim NOT cancelled — EF flag is false");
        }
    }
}
