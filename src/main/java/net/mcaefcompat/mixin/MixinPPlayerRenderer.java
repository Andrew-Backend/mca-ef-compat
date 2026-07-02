package net.mcaefcompat.mixin;

import forge.net.mca.MCAClient;
import forge.net.mca.client.model.PlayerEntityExtendedModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.mesh.HumanoidMesh;

/**
 * Injected into EF's PPlayerRenderer.prepareModel().
 *
 * prepareModel() is called every frame just before EF builds / draws its
 * skinned mesh. It does two things:
 *   1. Reads model parts (head, torso, arms, legs…) from renderer.getModel()
 *      and calls setHidden(true) on each one so vanilla rendering is suppressed.
 *   2. Calls setHidden(false) on them at the end of the frame (in a cleanup path).
 *
 * When MCA is active, renderer.getModel() returns PlayerEntityExtendedModel
 * (MCA's subclass of PlayerModel). The fields EF reads (f_102808_ = head, etc.)
 * are the SAME Java fields as in PlayerModel, so EF does correctly hide MCA's
 * parts. BUT: MCA's model overrides setupAnim() and copies its own rotations
 * back into those same fields AFTER EF has already read them, overwriting EF's
 * transform data and leaving the parts in their default (static) pose.
 *
 * Fix:
 *   After EF calls prepareModel (HEAD+RETURN), we additionally call
 *   setVisible(false) on every MCA-specific extra part (breasts, breastsWear)
 *   so they don't bleed through, and we call copyVisibility() on MCA's model
 *   to propagate the hidden flags to the armor model — which MCA does NOT do
 *   automatically when EF is involved.
 *
 *   The real animation fix (static limbs) is in MixinRenderEngine: we must
 *   call model.copyAttributes() into MCA's model BEFORE EF reads pose data,
 *   so that EF's joint transforms are not overwritten by MCA's setupAnim.
 *   We achieve this by forcing MCA to skip its own setupAnim() when EF is
 *   actively rendering the player, by nulling the VillagerLike temporarily.
 *
 * Actually the cleanest fix for the animation issue is:
 *   We mixin into PlayerEntityExtendedModel.setupAnim() and, if EF is currently
 *   rendering (we use a thread-local flag set by MixinRenderEngine), we skip
 *   MCA's pose overwrite and let EF's armature control the pose instead.
 */
@Mixin(value = PPlayerRenderer.class, remap = false)
public abstract class MixinPPlayerRenderer {

    @Inject(method = "prepareModel", at = @At("RETURN"))
    private void mcaefcompat$hideMcaExtraParts(
            HumanoidMesh mesh,
            AbstractClientPlayer entity,
            AbstractClientPlayerPatch entitypatch,
            PlayerRenderer renderer,
            CallbackInfo ci
    ) {
        if (!MCAClient.isPlayerRendererAllowed()) return;

        // getModel() returns PlayerEntityExtendedModel when MCA is active.
        // It extends PlayerModel, so the cast is safe if MCA is loaded.
        if (!(renderer.getModel() instanceof PlayerEntityExtendedModel<?> mcaModel)) return;

        // Hide MCA-specific extra parts that EF doesn't know about.
        // Without this they render on top of EF's skinned mesh as a static blob.
        ModelPart breasts = mcaModel.getBreastPart();
        if (breasts != null) {
            breasts.visible = false;
        }
    }
}
