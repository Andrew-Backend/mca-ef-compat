package yesman.epicfight.skill.dodge;

import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DodgeSkill extends Skill {
   protected final AnimationManager.AnimationAccessor<? extends StaticAnimation>[] animations;

   public static DodgeSkill.Builder createDodgeBuilder() {
      return new DodgeSkill.Builder().setCategory(SkillCategories.DODGE).setActivateType(Skill.ActivateType.ONE_SHOT).setResource(Skill.Resource.STAMINA);
   }

   public DodgeSkill(DodgeSkill.Builder builder) {
      super(builder);
      this.animations = builder.animations;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Object getExecutionPacket(SkillContainer skillContainer, FriendlyByteBuf args) {
      LocalPlayerPatch executor = skillContainer.getClientExecutor();
      LocalPlayer localPlayer = executor.getOriginal();
      float pulse = Mth.m_14036_(0.3F + EnchantmentHelper.m_220302_((LivingEntity)executor.getOriginal()), 0.0F, 1.0F);
      InputUtils.sneakingTick(localPlayer, false, pulse);
      MovementDirection movementDirection = MovementDirection.fromInputState(InputManager.getInputState(localPlayer.f_108618_));
      int vertic = movementDirection.vertical();
      int horizon = movementDirection.horizontal();
      float yRot = EpicFightCameraAPI.getInstance().getForwardYRot();
      float degree = Mth.m_14177_(-(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon) + yRot);
      CPSkillRequest packet = new CPSkillRequest(skillContainer.getSlot());
      packet.getBuffer().writeInt(vertic >= 0 ? 0 : 1);
      packet.getBuffer().writeFloat(degree);
      return packet;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(ItemStack.f_41584_.format(this.consumption));
      return list;
   }

   @Override
   public void executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args) {
      super.executeOnServer(skillContainer, args);
      ServerPlayerPatch executor = skillContainer.getServerExecutor();
      int i = args.readInt();
      float yRot = args.readFloat();
      executor.playAnimationSynchronized(this.animations[i], 0.0F);
      executor.setModelYRot(yRot, true);
   }

   @Override
   public boolean isExecutableState(PlayerPatch<?> executor) {
      EntityState playerState = executor.getEntityState();
      return !executor.isInAir()
         && playerState.canUseSkill()
         && !executor.getOriginal().m_20069_()
         && !executor.getOriginal().m_6147_()
         && executor.getOriginal().m_20202_() == null;
   }

   public static class Builder extends SkillBuilder<DodgeSkill> {
      protected AnimationManager.AnimationAccessor<? extends StaticAnimation>[] animations;

      @SafeVarargs
      public final DodgeSkill.Builder setAnimations(AnimationManager.AnimationAccessor<? extends StaticAnimation>... animations) {
         this.animations = animations;
         return this;
      }
   }
}
