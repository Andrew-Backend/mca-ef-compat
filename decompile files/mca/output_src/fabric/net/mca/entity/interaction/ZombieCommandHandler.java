package fabric.net.mca.entity.interaction;

import fabric.net.mca.entity.ZombieVillagerEntityMCA;
import net.minecraft.class_1268;
import net.minecraft.class_3222;

public class ZombieCommandHandler extends EntityCommandHandler<ZombieVillagerEntityMCA> {
   public ZombieCommandHandler(ZombieVillagerEntityMCA entity) {
      super(entity);
   }

   @Override
   public boolean handle(class_3222 player, String command) {
      switch (command) {
         case "gift":
            if (this.entity.method_5992(player, class_1268.field_5808).method_23665() && !player.method_31549().field_7477) {
               player.method_5998(class_1268.field_5808).method_7934(1);
            }

            return true;
         default:
            return super.handle(player, command);
      }
   }
}
