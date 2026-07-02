package net.mcaefcompat;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.MCAClient;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();

        // Stage 1: event fires at all
        McaEfCompat.LOGGER.info("[MCA-EF][1] RenderLivingEvent$Pre entity={}", entity.getClass().getSimpleName());

        if (!(entity instanceof AbstractClientPlayer player)) {
            McaEfCompat.LOGGER.info("[MCA-EF][1] Not a player, skipping");
            return;
        }

        // Stage 2: MCA renderer allowed?
        boolean allowed = MCAClient.isPlayerRendererAllowed();
        McaEfCompat.LOGGER.info("[MCA-EF][2] isPlayerRendererAllowed={}", allowed);

        McaEfCompatState.EF_RENDERING.set(true);
        McaEfCompatState.SCALE_PUSHED.set(false);

        if (!allowed) return;

        // Stage 3: VillagerLike lookup
        VillagerLike<?> villager = CommonVillagerModel.getVillager(player);
        McaEfCompat.LOGGER.info("[MCA-EF][3] villager={}", villager != null ? villager.getClass().getSimpleName() : "null");

        if (villager == null) return;

        // Stage 4: scale values
        float vScale = villager.getRawScaleFactor();
        float hScale = villager.getHorizontalScaleFactor();
        AgeState ageState = villager.getAgeState();
        McaEfCompat.LOGGER.info("[MCA-EF][4] vScale={} hScale={} ageState={}", vScale, hScale, ageState);

        if (Math.abs(vScale - 1f) < 0.001f && Math.abs(hScale - 1f) < 0.001f) {
            McaEfCompat.LOGGER.info("[MCA-EF][4] Scales are default (1.0), skipping push");
            return;
        }

        // Stage 5: applying scale
        PoseStack poseStack = event.getPoseStack();
        McaEfCompat.LOGGER.info("[MCA-EF][5] Pushing pose and applying scale hScale={} vScale={}", hScale, vScale);
        poseStack.pushPose();
        poseStack.scale(hScale, vScale, hScale);

        if (ageState == AgeState.BABY) {
            McaEfCompat.LOGGER.info("[MCA-EF][5] Baby detected, applying translate");
            poseStack.translate(0.0, 0.5 * (1.0 - vScale), 0.0);
        }

        McaEfCompatState.SCALE_PUSHED.set(true);
        McaEfCompat.LOGGER.info("[MCA-EF][5] Scale pushed successfully");
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();

        McaEfCompatState.EF_RENDERING.set(false);

        if (!(entity instanceof AbstractClientPlayer)) return;

        boolean pushed = Boolean.TRUE.equals(McaEfCompatState.SCALE_PUSHED.get());
        McaEfCompat.LOGGER.info("[MCA-EF][6] RenderLivingEvent$Post player, scalePushed={}", pushed);

        if (pushed) {
            event.getPoseStack().popPose();
            McaEfCompatState.SCALE_PUSHED.set(false);
            McaEfCompat.LOGGER.info("[MCA-EF][6] Pose popped successfully");
        }
    }
}
