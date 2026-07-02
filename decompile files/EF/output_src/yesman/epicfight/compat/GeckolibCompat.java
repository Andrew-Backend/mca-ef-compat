package yesman.epicfight.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import software.bernie.geckolib.event.GeoRenderEvent.Entity.Post;
import software.bernie.geckolib.event.GeoRenderEvent.Entity.Pre;
import yesman.epicfight.api.client.model.transformer.GeoModelTransformer;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class GeckolibCompat implements ICompatModule {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void onModEventBusClient(IEventBus eventBus) {
      eventBus.addListener(event -> event.enqueueWork(() -> HumanoidModelBaker.registerNewTransformer(new GeoModelTransformer())));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
      eventBus.addListener(GeoModelTransformer::getGeoArmorTexturePath);
      eventBus.addListener(this::geoEntityRenderPreEvent);
      eventBus.addListener(this::geoEntityRenderPostEvent);
   }

   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
   }

   @OnlyIn(Dist.CLIENT)
   public void geoEntityRenderPreEvent(Pre event) {
      Entity entity = event.getEntity();
      if (entity.m_9236_() != null) {
         if (entity instanceof LivingEntity livingentity) {
            RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
            if (renderEngine.hasRendererFor(livingentity)) {
               LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
               LocalPlayerPatch playerpatch = null;
               float originalYRot = 0.0F;
               if ((event.getPartialTick() == 0.0F || event.getPartialTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localPlayerPatch) {
                  playerpatch = localPlayerPatch;
                  originalYRot = playerpatch.getModelYRot();
                  playerpatch.setModelYRotInGui(livingentity.m_146908_());
                  event.getPoseStack().m_85837_(0.0, 0.1, 0.0);
               }

               if (entitypatch != null && entitypatch.overrideRender()) {
                  event.setCanceled(true);
                  renderEngine.renderEntityArmatureModel(
                     livingentity,
                     entitypatch,
                     event.getRenderer(),
                     event.getBufferSource(),
                     event.getPoseStack(),
                     event.getPackedLight(),
                     event.getPartialTick()
                  );
                  if (ClientEngine.getInstance().getPlayerPatch() != null
                     && !renderEngine.minecraft.f_91066_.f_92062_
                     && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.m_9236_())) {
                     for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
                        if (entityIndicator.shouldDraw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getPartialTick())) {
                           entityIndicator.draw(
                              livingentity,
                              entitypatch,
                              ClientEngine.getInstance().getPlayerPatch(),
                              event.getPoseStack(),
                              event.getBufferSource(),
                              event.getPartialTick()
                           );
                        }
                     }
                  }
               }

               if (playerpatch != null) {
                  playerpatch.disableModelYRotInGui(originalYRot);
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void geoEntityRenderPostEvent(Post event) {
      Entity entity = event.getEntity();
      if (entity.m_9236_() != null) {
         if (entity instanceof LivingEntity livingentity) {
            RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
            if (ClientEngine.getInstance().getPlayerPatch() != null
               && !renderEngine.minecraft.f_91066_.f_92062_
               && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.m_9236_())) {
               LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);

               for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
                  if (entityIndicator.shouldDraw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getPartialTick())) {
                     entityIndicator.draw(
                        livingentity,
                        entitypatch,
                        ClientEngine.getInstance().getPlayerPatch(),
                        event.getPoseStack(),
                        event.getBufferSource(),
                        event.getPartialTick()
                     );
                  }
               }
            }
         }
      }
   }
}
