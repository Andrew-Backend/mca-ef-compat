package forge.net.mca.mixin;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface MixinVillagerEntityInvoker {
   @Invoker("m_35536_")
   void invokeBeginTradeWith(Player var1);
}
