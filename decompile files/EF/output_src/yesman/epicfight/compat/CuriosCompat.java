package yesman.epicfight.compat;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer.HumanoidRender;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.client.render.CuriosLayer;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CuriosCompat implements ICompatModule {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void onModEventBusClient(IEventBus eventBus) {
      eventBus.addListener(event -> {
         if (event.get(EntityType.f_20532_) instanceof PatchedLivingEntityRenderer patchedlivingrenderer) {
            patchedlivingrenderer.addPatchedLayerAlways(CuriosLayer.class, new CuriosCompat.PatchedCuriosLayerRenderer());
         }
      });
      eventBus.addListener(event -> {
         CuriosCompat.PatchedCuriosLayerRenderer.CURIO_MESHES.values().forEach(SkinnedMesh::destroy);
         CuriosCompat.PatchedCuriosLayerRenderer.CURIO_MESHES.clear();
      });
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
   }

   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
   }

   @OnlyIn(Dist.CLIENT)
   public interface EpicFightCurioRenderer {
      void draw(
         ItemStack var1,
         SlotContext var2,
         LivingEntityPatch<LivingEntity> var3,
         LivingEntity var4,
         CuriosLayer<LivingEntity, EntityModel<LivingEntity>> var5,
         PoseStack var6,
         MultiBufferSource var7,
         int var8,
         OpenMatrix4f[] var9,
         float var10,
         float var11,
         float var12,
         float var13
      );
   }

   @OnlyIn(Dist.CLIENT)
   public static class PatchedCuriosLayerRenderer
      extends ModelRenderLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, CuriosLayer<LivingEntity, EntityModel<LivingEntity>>, SkinnedMesh> {
      private static final Map<HumanoidModel<LivingEntity>, SkinnedMesh> CURIO_MESHES = Maps.newHashMap();

      public PatchedCuriosLayerRenderer() {
         super(null);
      }

      protected void renderLayer(
         LivingEntityPatch<LivingEntity> entitypatch,
         LivingEntity livingEntity,
         CuriosLayer<LivingEntity, EntityModel<LivingEntity>> vanillaLayer,
         PoseStack poseStack,
         MultiBufferSource buffers,
         int packedLight,
         OpenMatrix4f[] poses,
         float bob,
         float yRot,
         float xRot,
         float partialTicks
      ) {
         MutableBoolean renderedEpicFightModel = new MutableBoolean(false);
         CuriosApi.getCuriosInventory(livingEntity)
            .ifPresent(
               handler -> handler.getCurios()
                  .forEach(
                     (id, stacksHandler) -> {
                        IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                        IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

                        for (int i = 0; i < stackHandler.getSlots(); i++) {
                           ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
                           boolean cosmetic = true;
                           NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                           boolean renderable = renderStates.size() > i && (Boolean)renderStates.get(i);
                           if (stack.m_41619_() && renderable) {
                              stack = stackHandler.getStackInSlot(i);
                              cosmetic = false;
                           }

                           if (!stack.m_41619_()) {
                              ItemStack finalStack = stack;
                              SlotContext slotContext = new SlotContext(id, livingEntity, i, cosmetic, renderable);
                              CuriosRendererRegistry.getRenderer(stack.m_41720_())
                                 .ifPresent(
                                    curioRenderer -> {
                                       if (curioRenderer instanceof HumanoidRender humanoidRenderer) {
                                          HumanoidModel<LivingEntity> curioModel = (HumanoidModel<LivingEntity>)humanoidRenderer.getModel(
                                             finalStack, slotContext
                                          );
                                          SkinnedMesh skinnedMesh;
                                          if (!ClientEngine.getInstance().isVanillaModelDebuggingMode() && CURIO_MESHES.containsKey(curioModel)) {
                                             skinnedMesh = CURIO_MESHES.get(curioModel);
                                          } else {
                                             poseStack.m_85836_();
                                             poseStack.m_85837_(10000.0, 0.0, 0.0);
                                             LivingEntityRenderer<?, ?> parentRenderer;
                                             if (livingEntity instanceof AbstractClientPlayer abstractClientPlayer) {
                                                parentRenderer = (LivingEntityRenderer<?, ?>)Minecraft.m_91087_()
                                                   .m_91290_()
                                                   .getSkinMap()
                                                   .get(abstractClientPlayer.m_108564_());
                                             } else {
                                                parentRenderer = (LivingEntityRenderer<?, ?>)Minecraft.m_91087_().m_91290_().getSkinMap().get("default");
                                             }

                                             curioRenderer.render(
                                                finalStack, slotContext, poseStack, parentRenderer, buffers, 0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F
                                             );
                                             poseStack.m_85849_();
                                             skinnedMesh = HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(curioModel);
                                             CURIO_MESHES.put(curioModel, skinnedMesh);
                                          }

                                          skinnedMesh.drawPosed(
                                             poseStack,
                                             buffers.m_6299_(
                                                EpicFightRenderTypes.getTriangulated(
                                                   RenderType.m_110458_(humanoidRenderer.getModelTexture(finalStack, slotContext))
                                                )
                                             ),
                                             Mesh.DrawingFunction.NEW_ENTITY,
                                             packedLight,
                                             1.0F,
                                             1.0F,
                                             1.0F,
                                             1.0F,
                                             OverlayTexture.f_118083_,
                                             entitypatch.getArmature(),
                                             poses
                                          );
                                          renderedEpicFightModel.setTrue();
                                       } else if (curioRenderer instanceof CuriosCompat.EpicFightCurioRenderer epicfightRenderer) {
                                          epicfightRenderer.draw(
                                             finalStack,
                                             slotContext,
                                             entitypatch,
                                             livingEntity,
                                             vanillaLayer,
                                             poseStack,
                                             buffers,
                                             packedLight,
                                             poses,
                                             bob,
                                             yRot,
                                             xRot,
                                             partialTicks
                                          );
                                          renderedEpicFightModel.setTrue();
                                       }
                                    }
                                 );
                           }
                        }
                     }
                  )
            );
         if (!renderedEpicFightModel.booleanValue()) {
            poseStack.m_85836_();
            OpenMatrix4f modelMatrix = poses[entitypatch.getArmature().searchJointByName("Root").getId()];
            MathUtils.mulStack(poseStack, modelMatrix);
            poseStack.m_252880_(0.0F, 0.75F, 0.0F);
            poseStack.m_85841_(-1.0F, -1.0F, 1.0F);
            vanillaLayer.render(
               poseStack,
               buffers,
               packedLight,
               livingEntity,
               livingEntity.f_267362_.m_267590_(partialTicks),
               livingEntity.f_267362_.m_267711_(partialTicks),
               partialTicks,
               bob,
               yRot,
               xRot
            );
            poseStack.m_85849_();
         }
      }
   }
}
