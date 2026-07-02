package forge.net.mca.item;

import forge.net.mca.SoundsMCA;
import forge.net.mca.TagsMCA;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.block.TombstoneBlock;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.util.localization.FlowingText;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ScytheItem extends SwordItem {
   public ScytheItem(Properties settings) {
      super(Tiers.GOLD, 10, -2.4F, settings);
   }

   public void m_7373_(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
      tooltip.addAll(FlowingText.wrap(Component.m_237115_(this.m_5671_(stack) + ".tooltip").m_130940_(ChatFormatting.GRAY), 160));
   }

   public UseAnim m_6164_(ItemStack stack) {
      return UseAnim.BLOCK;
   }

   public int m_8105_(ItemStack stack) {
      return 72000;
   }

   public void m_6883_(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
      if (entity instanceof LivingEntity living) {
         boolean active = stack.m_41784_().m_128471_("active");
         RandomSource r = entity.m_9236_().f_46441_;
         if (active != selected) {
            stack.m_41784_().m_128379_("active", selected);
            float baseVolume = selected ? 0.75F : 0.25F;
            entity.m_9236_()
               .m_5594_(
                  null,
                  entity.m_20183_(),
                  (SoundEvent)SoundsMCA.REAPER_SCYTHE_OUT.get(),
                  entity.m_5720_(),
                  baseVolume + r.m_188501_() / 2.0F,
                  0.65F + r.m_188501_() / 10.0F
               );
         }

         if (selected && living.f_20913_ == -1) {
            entity.m_9236_().m_5594_(null, entity.m_20183_(), (SoundEvent)SoundsMCA.REAPER_SCYTHE_SWING.get(), entity.m_5720_(), 0.25F, 1.0F);
         }
      }
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player user, InteractionHand hand) {
      user.m_6672_(hand);
      return super.m_7203_(world, user, hand);
   }

   public InteractionResult m_6225_(UseOnContext context) {
      if (hasSoul(context.m_43722_())) {
         InteractionResult result = use(context, false);
         if (result == InteractionResult.SUCCESS) {
            setSoul(context.m_43722_(), false);
         }

         if (result != InteractionResult.PASS) {
            return result;
         }
      }

      return super.m_6225_(context);
   }

   public boolean m_5812_(ItemStack stack) {
      return super.m_5812_(stack) || hasSoul(stack);
   }

   public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (target.m_9236_().f_46441_.m_188503_(50) > 40) {
         target.m_7292_(new MobEffectInstance(MobEffects.f_19615_, 1000, 1));
      }

      SoundEvent sound = (SoundEvent)SoundsMCA.REAPER_SCYTHE_OUT.get();
      if (!hasSoul(stack)
         && target.m_21224_()
         && (target.m_6095_() == EntitiesMCA.MALE_VILLAGER.get() || target.m_6095_() == EntitiesMCA.FEMALE_VILLAGER.get())) {
         setSoul(stack, true);
         sound = SoundEvents.f_11700_;
         if (attacker instanceof ServerPlayer) {
            CriterionMCA.GENERIC_EVENT_CRITERION.trigger((ServerPlayer)attacker, "scytheKill");
         }
      }

      RandomSource r = attacker.m_9236_().f_46441_;
      attacker.m_9236_().m_5594_(null, attacker.m_20183_(), sound, attacker.m_5720_(), 0.75F + r.m_188501_() / 2.0F, 0.75F + r.m_188501_() / 2.0F);
      return super.m_7579_(stack, target, attacker);
   }

   public boolean m_6832_(ItemStack stack, ItemStack ingredient) {
      return stack.m_41720_() == ingredient.m_41720_();
   }

   public static void setSoul(ItemStack stack, boolean soul) {
      stack.m_41784_().m_128379_("hasSoul", soul);
   }

   public static boolean hasSoul(ItemStack stack) {
      return stack.m_41782_() && stack.m_41783_().m_128471_("hasSoul");
   }

   public static InteractionResult use(UseOnContext context, boolean cure) {
      Level world = context.m_43725_();
      BlockPos pos = context.m_8083_();
      BlockState state = world.m_8055_(pos);
      return state.m_204336_(TagsMCA.Blocks.TOMBSTONES) ? TombstoneBlock.Data.of(world.m_7702_(pos)).filter(TombstoneBlock.Data::hasEntity).map(data -> {
         if (!context.m_43725_().f_46443_) {
            CriterionMCA.GENERIC_EVENT_CRITERION.trigger((ServerPlayer)context.m_43723_(), cure ? "staffOfLife" : "scytheRevive");
         }

         if (!world.f_46443_ && !data.isResurrecting()) {
            data.startResurrecting(cure);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }).orElse(InteractionResult.FAIL) : InteractionResult.PASS;
   }
}
