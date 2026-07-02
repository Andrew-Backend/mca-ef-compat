package forge.net.mca.entity.interaction;

import forge.net.mca.entity.ZombieVillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class ZombieCommandHandler extends EntityCommandHandler<ZombieVillagerEntityMCA> {
   public ZombieCommandHandler(ZombieVillagerEntityMCA entity) {
      super(entity);
   }

   @Override
   public boolean handle(ServerPlayer player, String command) {
      switch (command) {
         case "gift":
            if (this.entity.m_6071_(player, InteractionHand.MAIN_HAND).m_19077_() && !player.m_150110_().f_35937_) {
               player.m_21120_(InteractionHand.MAIN_HAND).m_41774_(1);
            }

            return true;
         default:
            return super.handle(player, command);
      }
   }
}
