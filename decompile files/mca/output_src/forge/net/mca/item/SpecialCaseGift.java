package forge.net.mca.item;

import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;

public interface SpecialCaseGift {
   boolean handle(ServerPlayer var1, VillagerEntityMCA var2);
}
