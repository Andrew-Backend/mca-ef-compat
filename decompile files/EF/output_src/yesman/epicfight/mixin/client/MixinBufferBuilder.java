package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder {
   @Shadow
   private ByteBuffer f_85648_;
   @Shadow
   private int f_231156_;
   @Shadow
   private int f_85652_;
   @Shadow
   private int f_85654_;
   @Shadow
   private VertexFormatElement f_85655_;
   @Shadow
   private int f_85656_;
   @Shadow
   private VertexFormat f_85658_;
   @Shadow
   private Mode f_85657_;
   @Shadow
   private boolean f_85661_;
   @Shadow
   private Vector3f[] f_166766_;
   @Shadow
   private VertexSorting f_276463_;
   @Shadow
   private boolean f_166762_;

   @Shadow
   protected abstract void m_166786_(IndexType var1);

   @Inject(at = @At("HEAD"), method = "setQuadSorting")
   public void epicfight_setQuadSortOrigin(VertexSorting sorting, CallbackInfo ci) {
      if (this.f_85657_ == Mode.TRIANGLES && this.f_276463_ != sorting) {
         this.f_276463_ = sorting;
         if (this.f_166766_ == null) {
            this.f_166766_ = this.makeTrianglesSortingPoints();
         }
      }
   }

   @Redirect(
      method = "storeRenderedBuffer()Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;putSortedQuadIndices(Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V")
   )
   public void epicfight_storeRenderedBuffer(BufferBuilder instance, IndexType vertexformat$indextype) {
      if (this.f_85657_ == Mode.QUADS) {
         this.m_166786_(vertexformat$indextype);
      } else if (this.f_85657_ == Mode.TRIANGLES) {
         this.putSortedTriangleIndices(vertexformat$indextype);
      }
   }

   private void putSortedTriangleIndices(IndexType indexType) {
      int[] aint = this.f_276463_.m_277065_(this.f_166766_);
      IntConsumer intconsumer = this.m_231158_(this.f_85652_, indexType);

      for (int j : aint) {
         intconsumer.accept(j * this.f_85657_.f_166948_);
         intconsumer.accept(j * this.f_85657_.f_166948_ + 1);
         intconsumer.accept(j * this.f_85657_.f_166948_ + 2);
      }
   }

   @Shadow
   private void m_85722_(int size) {
      throw new AbstractMethodError("Shadow");
   }

   @Shadow
   private IntConsumer m_231158_(int int1, IndexType indexType) {
      throw new AbstractMethodError("Shadow");
   }

   public Vector3f[] makeTrianglesSortingPoints() {
      FloatBuffer floatbuffer = this.f_85648_.asFloatBuffer();
      int i = this.f_231156_ / 4;
      int j = this.f_85658_.m_86017_();
      int k = j * this.f_85657_.f_166948_;
      int l = this.f_85654_ / this.f_85657_.f_166948_;
      Vector3f[] avector3f = new Vector3f[l];

      for (int i1 = 0; i1 < l; i1++) {
         float x1 = floatbuffer.get(i + i1 * k);
         float y1 = floatbuffer.get(i + i1 * k + 1);
         float z1 = floatbuffer.get(i + i1 * k + 2);
         float x2 = floatbuffer.get(i + i1 * k + j);
         float y2 = floatbuffer.get(i + i1 * k + j + 1);
         float z2 = floatbuffer.get(i + i1 * k + j + 2);
         float x3 = floatbuffer.get(i + i1 * k + j * 2);
         float y3 = floatbuffer.get(i + i1 * k + j * 2 + 1);
         float z3 = floatbuffer.get(i + i1 * k + j * 2 + 2);
         avector3f[i1] = this.getOriginQuadCenter(x1, y1, z1, x2, y2, z2, x3, y3, z3);
      }

      return avector3f;
   }

   private Vector3f getOriginQuadCenter(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
      float[] lineLength = new float[]{
         Mth.m_14116_(Mth.m_14207_(x2 - x1) + Mth.m_14207_(y2 - y1) + Mth.m_14207_(z2 - z1)),
         Mth.m_14116_(Mth.m_14207_(x3 - x2) + Mth.m_14207_(y3 - y2) + Mth.m_14207_(z3 - z2)),
         Mth.m_14116_(Mth.m_14207_(x1 - x3) + Mth.m_14207_(y1 - y3) + Mth.m_14207_(z1 - z3))
      };
      int longest = 0;

      for (int i = 1; i < 3; i++) {
         if (lineLength[i] > lineLength[longest]) {
            longest = i;
         }
      }

      switch (longest) {
         case 0:
            return new Vector3f((x1 + x2) * 0.5F, (y1 + y2) * 0.5F, (z1 + z2) * 0.5F);
         case 1:
            return new Vector3f((x2 + x3) * 0.5F, (y2 + y3) * 0.5F, (z2 + z3) * 0.5F);
         case 2:
            return new Vector3f((x3 + x1) * 0.5F, (y3 + y1) * 0.5F, (z3 + z1) * 0.5F);
         default:
            return null;
      }
   }
}
