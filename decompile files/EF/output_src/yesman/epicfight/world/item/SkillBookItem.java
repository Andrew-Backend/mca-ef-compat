package yesman.epicfight.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillBookItem extends Item {
   public static void setContainingSkill(String name, ItemStack stack) {
      stack.m_41784_().m_128365_("skill", StringTag.m_129297_(name));
   }

   public static void setContainingSkill(Skill skill, ItemStack stack) {
      setContainingSkill(skill.toString(), stack);
   }

   public static Skill getContainSkill(ItemStack stack) {
      if (stack.m_41783_() != null && stack.m_41783_().m_128441_("skill")) {
         String skillName = stack.m_41783_().m_128461_("skill");
         return SkillManager.getSkill(skillName);
      } else {
         return null;
      }
   }

   public SkillBookItem(Properties properties) {
      super(properties);
   }

   public boolean m_5812_(ItemStack stack) {
      return stack.m_41783_() != null && stack.m_41783_().m_128441_("skill");
   }

   public void m_7373_(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      if (stack.m_41783_() != null && stack.m_41783_().m_128441_("skill")) {
         ResourceLocation rl = ResourceLocation.parse(stack.m_41783_().m_128461_("skill"));
         tooltip.add(Component.m_237115_(String.format("skill.%s.%s", rl.m_135827_(), rl.m_135815_())).m_130940_(ChatFormatting.DARK_GRAY));
      }
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level worldIn, Player playerIn, InteractionHand hand) {
      ItemStack itemstack = playerIn.m_21120_(hand);
      playerIn.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(capability -> {
         if (capability instanceof PlayerPatch<?> playerpatch) {
            playerpatch.openSkillBook(itemstack, hand);
         }
      });
      playerIn.m_36246_(Stats.f_12982_.m_12902_(this));
      return InteractionResultHolder.m_19098_(itemstack);
   }
}
