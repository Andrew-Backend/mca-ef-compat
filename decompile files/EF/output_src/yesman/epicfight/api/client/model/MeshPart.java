package yesman.epicfight.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector4f;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

public abstract class MeshPart {
   protected final List<VertexBuilder> verticies;
   protected final Mesh.RenderProperties renderProperties;
   protected final Supplier<OpenMatrix4f> vanillaPartTracer;
   protected boolean isHidden;
   protected static final Vector4f COLOR = new Vector4f();

   public MeshPart(List<VertexBuilder> vertices, @Nullable Mesh.RenderProperties renderProperties, @Nullable Supplier<OpenMatrix4f> vanillaPartTracer) {
      this.verticies = vertices;
      this.renderProperties = renderProperties;
      this.vanillaPartTracer = vanillaPartTracer;
   }

   public abstract void draw(PoseStack var1, VertexConsumer var2, Mesh.DrawingFunction var3, int var4, float var5, float var6, float var7, float var8, int var9);

   public void setHidden(boolean hidden) {
      this.isHidden = hidden;
   }

   public boolean isHidden() {
      return this.isHidden;
   }

   public List<VertexBuilder> getVertices() {
      return this.verticies;
   }

   public OpenMatrix4f getVanillaPartTransform() {
      return this.vanillaPartTracer == null ? null : this.vanillaPartTracer.get();
   }

   public VertexConsumer getBufferBuilder(RenderType renderType, MultiBufferSource bufferSource) {
      return this.renderProperties.customTexturePath() != null
         ? bufferSource.m_6299_(EpicFightRenderTypes.replaceTexture(this.renderProperties.customTexturePath(), renderType))
         : bufferSource.m_6299_(renderType);
   }

   public Vector4f getColor(float r, float g, float b, float a) {
      if (this.renderProperties != null && this.renderProperties.customColor() != null) {
         COLOR.set(this.renderProperties.customColor().x, this.renderProperties.customColor().y, this.renderProperties.customColor().z, a);
         return COLOR;
      } else {
         COLOR.set(r, g, b, a);
         return COLOR;
      }
   }
}
