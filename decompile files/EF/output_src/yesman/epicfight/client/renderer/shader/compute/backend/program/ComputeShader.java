package yesman.epicfight.client.renderer.shader.compute.backend.program;

import org.lwjgl.opengl.GL20;

public class ComputeShader {
   public final int shaderId = GL20.glCreateShader(37305);

   public void setShaderSource(String source) {
      GL20.glShaderSource(this.shaderId, source);
   }

   public void compileShader() {
      GL20.glCompileShader(this.shaderId);
   }

   public boolean isCompiled() {
      return GL20.glGetShaderi(this.shaderId, 35713) == 1;
   }

   public String getInfoLog() {
      return GL20.glGetShaderInfoLog(this.shaderId);
   }

   public void delete() {
      GL20.glDeleteShader(this.shaderId);
   }
}
