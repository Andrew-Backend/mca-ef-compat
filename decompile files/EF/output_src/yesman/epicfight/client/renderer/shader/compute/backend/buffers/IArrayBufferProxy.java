package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

public interface IArrayBufferProxy {
   void updateFromTo(int var1, int var2);

   void bindBufferBase(int var1);

   void unbind();

   void updateAll();

   void close();
}
