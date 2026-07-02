package yesman.epicfight.api.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IQuadTransformer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

public interface Mesh {
   void initialize();

   void draw(PoseStack var1, VertexConsumer var2, Mesh.DrawingFunction var3, int var4, float var5, float var6, float var7, float var8, int var9);

   void drawPosed(
      PoseStack var1,
      VertexConsumer var2,
      Mesh.DrawingFunction var3,
      int var4,
      float var5,
      float var6,
      float var7,
      float var8,
      int var9,
      @Nullable Armature var10,
      OpenMatrix4f[] var11
   );

   default void draw(
      PoseStack poseStack,
      MultiBufferSource bufferSources,
      RenderType renderType,
      Mesh.DrawingFunction drawingFunction,
      int packedLight,
      float r,
      float g,
      float b,
      float a,
      int overlay,
      @Nullable Armature armature,
      OpenMatrix4f[] poses
   ) {
      this.drawPosed(
         poseStack, bufferSources.m_6299_(EpicFightRenderTypes.getTriangulated(renderType)), drawingFunction, packedLight, r, g, b, a, overlay, armature, poses
      );
   }

   @FunctionalInterface
   interface DrawingFunction {
      Mesh.DrawingFunction NEW_ENTITY = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> builder.m_5954_(
         posX, posY, posZ, r, g, b, a, u, v, overlay, packedLight, normX, normY, normZ
      );
      Mesh.DrawingFunction POSITION_TEX = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
         builder.m_5483_(posX, posY, posZ);
         builder.m_7421_(u, v);
         builder.m_5752_();
      };
      Mesh.DrawingFunction POSITION_TEX_COLOR_NORMAL = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
         builder.m_5483_(posX, posY, posZ);
         builder.m_7421_(u, v);
         builder.m_85950_(r, g, b, a);
         builder.m_5601_(normX, normY, normZ);
         builder.m_5752_();
      };
      Mesh.DrawingFunction POSITION_TEX_COLOR_LIGHTMAP = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
         builder.m_5483_(posX, posY, posZ);
         builder.m_7421_(u, v);
         builder.m_85950_(r, g, b, a);
         builder.m_85969_(packedLight);
         builder.m_5752_();
      };
      Mesh.DrawingFunction POSITION_COLOR_LIGHTMAP = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
         builder.m_5483_(posX, posY, posZ);
         builder.m_85950_(r, g, b, a);
         builder.m_85969_(packedLight);
         builder.m_5752_();
      };
      Mesh.DrawingFunction POSITION_COLOR_NORMAL = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
         builder.m_5483_(posX, posY, posZ);
         builder.m_85950_(r, g, b, a);
         builder.m_5601_(normX, normY, normZ);
         builder.m_5752_();
      };
      Mesh.DrawingFunction POSITION_COLOR_TEX_LIGHTMAP = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
         builder.m_5483_(posX, posY, posZ);
         builder.m_85950_(r, g, b, a);
         builder.m_7421_(u, v);
         builder.m_85969_(packedLight);
         builder.m_5752_();
      };

      void draw(
         VertexConsumer var1,
         float var2,
         float var3,
         float var4,
         float var5,
         float var6,
         float var7,
         int var8,
         float var9,
         float var10,
         float var11,
         float var12,
         float var13,
         float var14,
         int var15
      );

      default void putBulkData(
         Pose pose,
         BakedQuad bakedQuad,
         VertexConsumer vertexConsumer,
         float red,
         float green,
         float blue,
         float alpha,
         int packedLight,
         int packedOverlay,
         boolean readExistingColor
      ) {
         putBulkDataWithDrawingFunction(
            this,
            vertexConsumer,
            pose,
            bakedQuad,
            new float[]{1.0F, 1.0F, 1.0F, 1.0F},
            red,
            green,
            blue,
            alpha,
            new int[]{packedLight, packedLight, packedLight, packedLight},
            packedOverlay,
            readExistingColor
         );
      }

      static void putBulkDataWithDrawingFunction(
         Mesh.DrawingFunction drawingFunction,
         VertexConsumer builder,
         Pose pPoseEntry,
         BakedQuad pQuad,
         float[] pColorMuls,
         float pRed,
         float pGreen,
         float pBlue,
         float alpha,
         int[] pCombinedLights,
         int pCombinedOverlay,
         boolean pMulColor
      ) {
         float[] afloat = new float[]{pColorMuls[0], pColorMuls[1], pColorMuls[2], pColorMuls[3]};
         int[] aint1 = pQuad.m_111303_();
         Vec3i vec3i = pQuad.m_111306_().m_122436_();
         Matrix4f matrix4f = pPoseEntry.m_252922_();
         Vector3f vector3f = pPoseEntry.m_252943_().transform(new Vector3f(vec3i.m_123341_(), vec3i.m_123342_(), vec3i.m_123343_()));
         int j = aint1.length / 8;
         MemoryStack memorystack = MemoryStack.stackPush();

         try {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.f_85811_.m_86020_());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int k = 0; k < j; k++) {
               intbuffer.clear();
               intbuffer.put(aint1, k * 8, 8);
               float f = bytebuffer.getFloat(0);
               float f1 = bytebuffer.getFloat(4);
               float f2 = bytebuffer.getFloat(8);
               float f3;
               float f4;
               float f5;
               if (pMulColor) {
                  float f6 = (bytebuffer.get(12) & 0xFF) / 255.0F;
                  float f7 = (bytebuffer.get(13) & 0xFF) / 255.0F;
                  float f8 = (bytebuffer.get(14) & 0xFF) / 255.0F;
                  f3 = f6 * afloat[k] * pRed;
                  f4 = f7 * afloat[k] * pGreen;
                  f5 = f8 * afloat[k] * pBlue;
               } else {
                  f3 = afloat[k] * pRed;
                  f4 = afloat[k] * pGreen;
                  f5 = afloat[k] * pBlue;
               }

               int l = applyBakedLighting(pCombinedLights[k], bytebuffer);
               float f9 = bytebuffer.getFloat(16);
               float f10 = bytebuffer.getFloat(20);
               Vector4f vector4f = matrix4f.transform(new Vector4f(f, f1, f2, 1.0F));
               applyBakedNormals(vector3f, bytebuffer, pPoseEntry.m_252943_());
               float vertexAlpha = pMulColor ? alpha * (bytebuffer.get(15) & 0xFF) / 255.0F : alpha;
               drawingFunction.draw(
                  builder,
                  vector4f.x(),
                  vector4f.y(),
                  vector4f.z(),
                  vector3f.x(),
                  vector3f.y(),
                  vector3f.z(),
                  l,
                  f3,
                  f4,
                  f5,
                  vertexAlpha,
                  f9,
                  f10,
                  pCombinedOverlay
               );
            }
         } catch (Throwable var34) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable var33) {
                  var34.addSuppressed(var33);
               }
            }

            throw var34;
         }

         if (memorystack != null) {
            memorystack.close();
         }
      }

      static int applyBakedLighting(int packedLight, ByteBuffer data) {
         int bl = packedLight & 65535;
         int sl = packedLight >> 16 & 65535;
         int offset = IQuadTransformer.UV2 * 4;
         int blBaked = Short.toUnsignedInt(data.getShort(offset));
         int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
         bl = Math.max(bl, blBaked);
         sl = Math.max(sl, slBaked);
         return bl | sl << 16;
      }

      static void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
         byte nx = data.get(28);
         byte ny = data.get(29);
         byte nz = data.get(30);
         if (nx != 0 || ny != 0 || nz != 0) {
            generated.set(nx / 127.0F, ny / 127.0F, nz / 127.0F);
            generated.mul(normalTransform);
         }
      }
   }

   record RenderProperties(ResourceLocation customTexturePath, Vec3f customColor, boolean isTransparent) {
      public static class Builder {
         protected String customTexturePath;
         protected Vec3f customColor = new Vec3f();
         protected boolean isTransparent;

         public Mesh.RenderProperties.Builder customTexturePath(String path) {
            this.customTexturePath = path;
            return this;
         }

         public Mesh.RenderProperties.Builder transparency(boolean isTransparent) {
            this.isTransparent = isTransparent;
            return this;
         }

         public Mesh.RenderProperties.Builder customColor(float r, float g, float b) {
            this.customColor.x = r;
            this.customColor.y = g;
            this.customColor.z = b;
            return this;
         }

         public Mesh.RenderProperties build() {
            return new Mesh.RenderProperties(
               this.customTexturePath == null ? null : ResourceLocation.parse(this.customTexturePath), this.customColor, this.isTransparent
            );
         }

         public static Mesh.RenderProperties.Builder create() {
            return new Mesh.RenderProperties.Builder();
         }
      }
   }
}
