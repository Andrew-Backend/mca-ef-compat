package yesman.epicfight.skill.mover;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

public class PhantomAscentSkill extends Skill {
   private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac120002");
   private final List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> animations = new ArrayList<>(2);
   private int extraJumps;
   private double jumpPower;

   public PhantomAscentSkill(SkillBuilder<? extends Skill> builder) {
      super(builder);
      this.animations.add(Animations.BIPED_PHANTOM_ASCENT_FORWARD);
      this.animations.add(Animations.BIPED_PHANTOM_ASCENT_BACKWARD);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.extraJumps = parameters.m_128451_("extra_jumps");
      this.consumption = 0.2F;
      this.jumpPower = parameters.m_128459_("jump_power");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT,
         EVENT_UUID,
         event -> {
            if (event.getPlayerPatch().getOriginal().m_20202_() == null
               && event.getPlayerPatch().isEpicFightMode()
               && !event.getPlayerPatch().getOriginal().m_150110_().f_35935_
               && !event.getPlayerPatch().isHoldingAny()
               && !event.getPlayerPatch().getEntityState().inaction()) {
               boolean jumpPressed = isJumpActionPressed();
               boolean jumpPressedPrev = container.getDataManager()
                  .<Boolean>getDataValue((SkillDataKey<Boolean>)SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get());
               if (jumpPressed && !jumpPressedPrev) {
                  if (container.getStack() < 1) {
                     return;
                  }

                  int jumpCounter = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.JUMP_COUNT.get());
                  if (jumpCounter <= 0 && event.getPlayerPatch().currentLivingMotion != LivingMotions.FALL) {
                     container.getDataManager().setData((SkillDataKey<Integer>)SkillDataKeys.JUMP_COUNT.get(), 1);
                  } else if (jumpCounter < this.extraJumps + 1) {
                     SkillCastEvent skillexecuteevent = new SkillCastEvent(container.getExecutor(), container, null);
                     container.getExecutor().getEventListener().triggerEvents(PlayerEventListener.EventType.SKILL_CAST_EVENT, skillexecuteevent);
                     if (skillexecuteevent.isCanceled()) {
                        return;
                     }

                     container.setResource(0.0F);
                     if (jumpCounter == 0 && event.getPlayerPatch().currentLivingMotion == LivingMotions.FALL) {
                        container.getDataManager().setData((SkillDataKey<Integer>)SkillDataKeys.JUMP_COUNT.get(), 2);
                     } else {
                        container.getDataManager().setDataF((SkillDataKey<Integer>)SkillDataKeys.JUMP_COUNT.get(), v -> v + 1);
                     }

                     container.getDataManager().setDataSync((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get(), true);
                     float f = Mth.m_14036_(0.3F + EnchantmentHelper.m_220302_((LivingEntity)container.getExecutor().getOriginal()), 0.0F, 1.0F);
                     event.sneakingTick(false, f);
                     MovementDirection movementDirection = MovementDirection.fromInputState(event.getInputState());
                     int forward = movementDirection.forward();
                     int backward = movementDirection.backward();
                     int left = movementDirection.left();
                     int right = movementDirection.right();
                     int vertic = movementDirection.vertical();
                     int horizon = movementDirection.horizontal();
                     int degree = -(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon);
                     int scale = forward == 0 && backward == 0 && left == 0 && right == 0 ? 0 : (vertic < 0 ? -1 : 1);
                     float launchingYRot = EpicFightCameraAPI.getInstance().getForwardYRot() + degree;
                     Vec3 horizontalLaunchingDirection = MathUtils.getVectorForRotation(0.0F, launchingYRot).m_82490_(0.15 * scale);
                     Vec3 currentDelta = container.getExecutor().getOriginal().m_20184_();
                     Vec3 newDelta = currentDelta.m_82549_(horizontalLaunchingDirection);
                     container.getExecutor()
                        .getOriginal()
                        .m_20334_(newDelta.f_82479_, this.jumpPower + container.getExecutor().getOriginal().m_285755_(), currentDelta.f_82481_);
                     event.getPlayerPatch().setModelYRot(EpicFightCameraAPI.getInstance().getForwardYRot() + degree, true);
                     event.getPlayerPatch().playAnimationInClientSide(this.animations.get(vertic < 0 ? 1 : 0), 0.0F);
                     ClientEngine.getInstance().controlEngine.releaseAllServedKeys();
                  }
               }

               container.getDataManager().setData((SkillDataKey<Boolean>)SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), jumpPressed);
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK,
         EVENT_UUID,
         event -> {
            if (event.getDamageSource().m_269533_(DamageTypeTags.f_268549_)
               && container.getDataManager().<Boolean>getDataValue((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get())) {
               float damage = event.getDamage();
               if (damage < 2.5F) {
                  event.setCanceled(true);
               }

               container.getDataManager().setData((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
            }
         }
      );
      listener.addEventListener(PlayerEventListener.EventType.FALL_EVENT, EVENT_UUID, event -> {
         container.getDataManager().setData((SkillDataKey<Integer>)SkillDataKeys.JUMP_COUNT.get(), 0);
         if (event.getPlayerPatch().isLogicalClient()) {
            container.getDataManager().setData((SkillDataKey<Boolean>)SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), false);
         }
      });
   }

   @Override
   public void onRemoved(SkillContainer container) {
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
      listener.removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
      listener.removeListener(PlayerEventListener.EventType.FALL_EVENT, EVENT_UUID);
   }

   @Override
   public boolean canExecute(SkillContainer container) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(this.extraJumps);
      return list;
   }

   private static boolean isJumpActionPressed() {
      return InputManager.isActionActive(MinecraftInputAction.JUMP);
   }
}
