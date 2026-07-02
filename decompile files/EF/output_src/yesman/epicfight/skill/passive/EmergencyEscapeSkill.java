package yesman.epicfight.skill.passive;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class EmergencyEscapeSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("4074c6de-0268-11ee-be56-0242ac120002");
   private final Set<WeaponCategory> availableWeapons;

   public static EmergencyEscapeSkill.Builder createEmergencyEscapeBuilder() {
      return new EmergencyEscapeSkill.Builder().setCategory(SkillCategories.PASSIVE).setResource(Skill.Resource.COOLDOWN);
   }

   public EmergencyEscapeSkill(EmergencyEscapeSkill.Builder builder) {
      super(builder);
      this.availableWeapons = builder.availableWeapons;
   }

   @Override
   public void onInitiate(SkillContainer container) {
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.SKILL_CAST_EVENT,
         EVENT_UUID,
         event -> {
            if (event.getSkillContainer().getSkill().getCategory() == SkillCategories.DODGE) {
               EntityState state = container.getExecutor().getEntityState();
               DynamicAnimation animation = container.getExecutor().<Animator>getAnimator().getPlayerFor(null).getRealAnimation().get();
               if (!event.isStateExecutable()
                     && animation instanceof AttackAnimation
                     && this.availableWeapons.contains(container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory())
                  || state.hurt() && container.getStack() > 0) {
                  event.setStateExecutable(true);
               }
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.SKILL_CONSUME_EVENT,
         EVENT_UUID,
         event -> {
            if (event.getSkill().getCategory() == SkillCategories.DODGE && !container.getExecutor().getOriginal().m_7500_()) {
               if (event.getSkill().getConsumption() > container.getExecutor().getStamina()) {
                  if (container.getExecutor().consumeForSkill(this, this.resource)) {
                     if (!container.getExecutor().isLogicalClient()) {
                        this.executeOnServer(container, event.getArguments());
                     }

                     event.setResourceType(Skill.Resource.NONE);
                  }
               } else if (container.getExecutor().getEntityState().hurt()
                  && container.getExecutor().consumeForSkill(this, this.resource)
                  && !container.getExecutor().isLogicalClient()) {
                  this.executeOnServer(container, event.getArguments());
               }
            }
         }
      );
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      this.setStackSynchronize(container, container.getStack() - 1);
      float yRot = container.getExecutor().getYRot();
      if (args != null && args.isReadable(8)) {
         args.readInt();
         yRot = args.readFloat();
      }

      container.getExecutor().playSound((SoundEvent)EpicFightSounds.EMERGENCY_ESCAPE.get(), 1.0F, 1.0F);
      SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(
         container.getExecutor().getOriginal().m_19879_(), EntityPairingPacketTypes.EMERGENCY_ESCAPE_ACTIVATED
      );
      pairingPacket.getBuffer().writeFloat(yRot);
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, container.getServerExecutor().getOriginal());
   }

   @Override
   public void onRemoved(SkillContainer container) {
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CONSUME_EVENT, EVENT_UUID);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      return container.getStack() == 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(String.format("%.1f", this.consumption));
      return list;
   }

   @Override
   public Set<WeaponCategory> getAvailableWeaponCategories() {
      return this.availableWeapons;
   }

   public static class Builder extends SkillBuilder<EmergencyEscapeSkill> {
      protected final Set<WeaponCategory> availableWeapons = Sets.newHashSet();

      public EmergencyEscapeSkill.Builder addAvailableWeaponCategory(WeaponCategory... wc) {
         this.availableWeapons.addAll(Arrays.asList(wc));
         return this;
      }
   }
}
