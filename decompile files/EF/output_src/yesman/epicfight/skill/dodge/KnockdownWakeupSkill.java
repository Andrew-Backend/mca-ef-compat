package yesman.epicfight.skill.dodge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class KnockdownWakeupSkill extends DodgeSkill {
   public KnockdownWakeupSkill(DodgeSkill.Builder builder) {
      super(builder);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Object getExecutionPacket(SkillContainer skillContainer, FriendlyByteBuf args) {
      LocalPlayerPatch executor = skillContainer.getClientExecutor();
      LocalPlayer localPlayer = executor.getOriginal();
      float pulse = Mth.m_14036_(0.3F + EnchantmentHelper.m_220302_((LivingEntity)executor.getOriginal()), 0.0F, 1.0F);
      InputUtils.sneakingTick(localPlayer, false, pulse);
      MovementDirection movementDirection = MovementDirection.fromInputState(InputManager.getInputState(localPlayer.f_108618_));
      int horizon = movementDirection.horizontal();
      float yRot = Minecraft.m_91087_().f_91063_.m_109153_().m_90590_();
      CPSkillRequest packet = new CPSkillRequest(skillContainer.getSlot());
      packet.getBuffer().writeInt(horizon >= 0 ? 0 : 1);
      packet.getBuffer().writeFloat(yRot);
      return packet;
   }

   @Override
   public boolean isExecutableState(PlayerPatch<?> executor) {
      EntityState playerState = executor.getEntityState();
      float elapsedTime = executor.<Animator>getAnimator().getPlayerFor(null).getElapsedTime();
      return !executor.isInAir()
         && (!playerState.hurt() || playerState.knockDown())
         && !executor.getOriginal().m_20069_()
         && !executor.getOriginal().m_6147_()
         && elapsedTime > 0.7F;
   }
}
