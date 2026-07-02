package yesman.epicfight.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class EntityUI {
   public static final List<EntityUI> ENTITY_UI_LIST = Lists.newArrayList();
   public static final TargetIndicator TARGET_INDICATOR = new TargetIndicator();
   public static final HealthBar HEALTH_BAR = new HealthBar();
   public static final ResourceLocation BATTLE_ICON = EpicFightMod.identifier("textures/gui/battle_icons.png");

   public EntityUI() {
      ENTITY_UI_LIST.add(this);
   }

   public static void drawUIAsLevelModel(
      Matrix4f matrix,
      ResourceLocation textureLocation,
      MultiBufferSource buffer,
      float minX,
      float minY,
      float maxX,
      float maxY,
      int minU,
      int minV,
      int maxU,
      int maxV,
      int uvSize
   ) {
      float uvSizeInvert = 1.0F / uvSize;
      drawUIAsLevelModel(
         matrix, textureLocation, buffer, minX, minY, maxX, maxY, minU * uvSizeInvert, minV * uvSizeInvert, maxU * uvSizeInvert, maxV * uvSizeInvert
      );
   }

   public static void drawUIAsLevelModel(
      Matrix4f matrix,
      ResourceLocation textureLocation,
      MultiBufferSource buffer,
      float minX,
      float minY,
      float maxX,
      float maxY,
      float minU,
      float minV,
      float maxU,
      float maxV
   ) {
      VertexConsumer vertexConsumer = buffer.m_6299_(EpicFightRenderTypes.entityUITexture(textureLocation));
      vertexConsumer.m_252986_(matrix, minX, minY, 0.0F).m_7421_(minU, maxV).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, 0.0F).m_7421_(maxU, maxV).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, 0.0F).m_7421_(maxU, minV).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, 0.0F).m_7421_(minU, minV).m_5752_();
   }

   public static void drawColoredQuadAsLevelModel(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int packedColor) {
      VertexConsumer vertexConsumer = buffer.m_6299_(EpicFightRenderTypes.entityUIColor());
      vertexConsumer.m_252986_(matrix, minX, minY, 0.0F).m_193479_(packedColor).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, 0.0F).m_193479_(packedColor).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, 0.0F).m_193479_(packedColor).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, 0.0F).m_193479_(packedColor).m_5752_();
   }

   public static void drawColoredQuadAsLevelModel(
      Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int r, int g, int b, int a
   ) {
      VertexConsumer vertexConsumer = buffer.m_6299_(EpicFightRenderTypes.entityUIColor());
      vertexConsumer.m_252986_(matrix, minX, minY, 0.0F).m_6122_(r, g, b, a).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, minY, 0.0F).m_6122_(r, g, b, a).m_5752_();
      vertexConsumer.m_252986_(matrix, maxX, maxY, 0.0F).m_6122_(r, g, b, a).m_5752_();
      vertexConsumer.m_252986_(matrix, minX, maxY, 0.0F).m_6122_(r, g, b, a).m_5752_();
   }

   public final Matrix4f getModelViewMatrixAlignedToCamera(
      PoseStack poseStack, LivingEntity entity, float x, float y, float z, boolean lockRotation, float partialTicks
   ) {
      float posX = (float)Mth.m_14139_(partialTicks, entity.f_19790_, entity.m_20185_());
      float posY = (float)Mth.m_14139_(partialTicks, entity.f_19791_, entity.m_20186_());
      float posZ = (float)Mth.m_14139_(partialTicks, entity.f_19792_, entity.m_20189_());
      poseStack.m_85836_();
      poseStack.m_252880_(-posX, -posY, -posZ);
      poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(180.0F));
      float screenX = posX + x;
      float screenY = posY + y;
      float screenZ = posZ + z;
      OpenMatrix4f viewMatrix = OpenMatrix4f.importFromMojangMatrix(poseStack.m_85850_().m_252922_());
      OpenMatrix4f finalMatrix = new OpenMatrix4f();
      finalMatrix.translate(new Vec3f(-screenX, screenY, -screenZ));
      poseStack.m_85849_();
      if (lockRotation) {
         finalMatrix.m00 = viewMatrix.m00;
         finalMatrix.m01 = viewMatrix.m10;
         finalMatrix.m02 = viewMatrix.m20;
         finalMatrix.m10 = viewMatrix.m01;
         finalMatrix.m11 = viewMatrix.m11;
         finalMatrix.m12 = viewMatrix.m21;
         finalMatrix.m20 = viewMatrix.m02;
         finalMatrix.m21 = viewMatrix.m12;
         finalMatrix.m22 = viewMatrix.m22;
      }

      finalMatrix.mulFront(viewMatrix);
      return OpenMatrix4f.exportToMojangMatrix(finalMatrix);
   }

   public abstract boolean shouldDraw(LivingEntity var1, @Nullable LivingEntityPatch<?> var2, LocalPlayerPatch var3, float var4);

   public abstract void draw(LivingEntity var1, @Nullable LivingEntityPatch<?> var2, LocalPlayerPatch var3, PoseStack var4, MultiBufferSource var5, float var6);
}
