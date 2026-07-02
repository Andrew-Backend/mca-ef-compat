package yesman.epicfight.client.renderer.shader.compute.backend.program;

public enum BarrierFlags {
   SHADER_STORAGE(8192),
   ATOMIC_COUNTER(4096),
   ELEMENT_ARRAY(2),
   VERTEX_ATTRIB_ARRAY(1),
   COMMAND(64);

   private final int flag;

   BarrierFlags(int flag) {
      this.flag = flag;
   }

   public static int getFlags(BarrierFlags... barrierFlags) {
      int intFlags = 0;

      for (BarrierFlags barrierFlag : barrierFlags) {
         intFlags |= barrierFlag.flag;
      }

      return intFlags;
   }
}
