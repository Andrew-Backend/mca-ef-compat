package yesman.epicfight.client.renderer.shader.compute.backend.program;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

public class Uniform {
   private final int programHandle;
   private final int uniformLocation;

   public Uniform(int programHandle, int uniformLocation) {
      this.programHandle = programHandle;
      this.uniformLocation = uniformLocation;
   }

   public void uploadMatrix3f(Matrix3f matrix) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         GL46C.glProgramUniformMatrix3fv(this.programHandle, this.uniformLocation, false, matrix.get(stack.callocFloat(9)));
      } catch (Throwable var6) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void uploadMatrix4f(Matrix4f matrix) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         GL46C.glProgramUniformMatrix4fv(this.programHandle, this.uniformLocation, false, matrix.get(stack.callocFloat(16)));
      } catch (Throwable var6) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void uploadUnsignedInt(int value) {
      GL46C.glProgramUniform1ui(this.programHandle, this.uniformLocation, value);
   }

   public void uploadVec4(float a, float b, float c, float d) {
      GL46C.glProgramUniform4f(this.programHandle, this.uniformLocation, a, b, c, d);
   }
}
