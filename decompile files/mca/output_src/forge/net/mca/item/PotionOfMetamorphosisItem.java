package forge.net.mca.item;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class PotionOfMetamorphosisItem extends TooltippedItem {
   private final Gender gender;

   public PotionOfMetamorphosisItem(Properties properties, Gender gender) {
      super(properties);
      this.gender = gender;
   }

   public final InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         PlayerSaveData data = PlayerSaveData.get(serverPlayer);
         CompoundTag villagerData = data.getEntityData();
         villagerData.m_128405_("gender", this.gender.ordinal());
         data.setEntityData(villagerData);
         this.common(serverPlayer);
         serverPlayer.m_284548_().m_6907_().forEach(p -> NetworkHandler.sendToPlayer(new PlayerDataMessage(player.m_20148_(), villagerData), p));
         ItemStack stack = player.m_21120_(hand);
         stack.m_41774_(1);
         return InteractionResultHolder.m_19090_(stack);
      } else {
         return super.m_7203_(world, player, hand);
      }
   }

   public InteractionResult m_6880_(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      if (entity instanceof VillagerLike<?> villager && !entity.m_9236_().f_46443_) {
         villager.getGenetics().setGender(this.gender);
         this.common(entity);
         stack.m_41774_(1);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.CONSUME;
      }
   }

   private void common(Entity entity) {
      entity.m_5496_(SoundEvents.f_12616_, 1.0F, 1.0F);
      FamilyTree tree = FamilyTree.get((ServerLevel)entity.m_9236_());
      FamilyTreeNode entry = tree.getOrCreate(entity);
      entry.setGender(this.gender);
   }
}
