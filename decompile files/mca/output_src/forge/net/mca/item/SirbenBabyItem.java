package forge.net.mca.item;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class SirbenBabyItem extends BabyItem {
   public SirbenBabyItem(Gender gender, Properties properties) {
      super(gender, properties);
   }

   public boolean m_5812_(ItemStack stack) {
      return true;
   }

   @Override
   protected VillagerEntityMCA birthChild(ItemStack stack, ServerLevel world, ServerPlayer player) {
      VillagerEntityMCA child = super.birthChild(stack, world, player);
      child.getTraits().addTrait(Traits.SIRBEN);
      return child;
   }
}
