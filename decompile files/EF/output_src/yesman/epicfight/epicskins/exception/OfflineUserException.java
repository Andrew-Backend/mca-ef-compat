package yesman.epicfight.epicskins.exception;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OfflineUserException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public OfflineUserException(String message) {
      super(message);
   }
}
