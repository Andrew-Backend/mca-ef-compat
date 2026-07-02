package yesman.epicfight.skill.passive;

import java.util.UUID;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class CatharsisSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("0c6efd18-199e-48a6-aa1b-51a1c1f9f613");
   private float regenBonus;

   public CatharsisSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.regenBonus = parameters.m_128457_("regen_bonus");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      container.getExecutor()
         .getEventListener()
         .addEventListener(
            PlayerEventListener.EventType.DODGE_SUCCESS_EVENT,
            EVENT_UUID,
            event -> {
               SkillContainer innateSkillContainer = container.getExecutor().getSkill(SkillSlots.WEAPON_INNATE);
               if (innateSkillContainer.getSkill() != null && innateSkillContainer.getStack() < innateSkillContainer.getSkill().getMaxStack()) {
                  ServerPlayer serverplayer = event.getPlayerPatch().getOriginal();
                  event.getPlayerPatch().playSound((SoundEvent)EpicFightSounds.CATHARSIS.get(), 1.0F, 1.0F, 1.0F);
                  serverplayer.m_284548_()
                     .m_8767_(
                        (SimpleParticleType)EpicFightParticles.CATHARSIS.get(),
                        serverplayer.m_20185_(),
                        serverplayer.m_20188_(),
                        serverplayer.m_20189_(),
                        0,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                     );
                  float damage = (float)serverplayer.m_21133_(Attributes.f_22281_);
                  innateSkillContainer.getSkill()
                     .setConsumptionSynchronize(innateSkillContainer, innateSkillContainer.getResource() + damage + this.consumption * this.regenBonus);
               }
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
   }
}
