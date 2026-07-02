package forge.net.mca.block;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class InfernalFlameBlock extends BaseFireBlock {
   public InfernalFlameBlock(Properties settings) {
      super(settings, 2.0F);
   }

   protected boolean m_7599_(BlockState state) {
      return true;
   }
}
