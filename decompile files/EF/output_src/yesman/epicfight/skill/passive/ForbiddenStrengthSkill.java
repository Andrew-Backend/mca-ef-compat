package yesman.epicfight.skill.passive;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class ForbiddenStrengthSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("0cfd60ba-b900-11ed-afa1-0242ac120002");

   public ForbiddenStrengthSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      container.getExecutor()
         .getEventListener()
         .addEventListener(
            PlayerEventListener.EventType.SKILL_CONSUME_EVENT,
            EVENT_UUID,
            event -> {
               if (event.getResourceType() == Skill.Resource.STAMINA
                  && event.getSkill() != this
                  && !container.getExecutor().hasStamina(event.getAmount())
                  && !container.getExecutor().getOriginal().m_7500_()) {
                  event.setResourceType(Skill.Resource.HEALTH);
                  float healthConsumeAmount = event.getAmount() - container.getExecutor().getStamina();
                  event.setAmount(healthConsumeAmount);
                  if (!container.getExecutor().isLogicalClient()
                     && event.getResourceType().predicate.canExecute(container, container.getExecutor(), healthConsumeAmount)) {
                     container.getExecutor().setStamina(0.0F);
                     Player player = container.getExecutor().getOriginal();
                     ServerLevel serverLevel = (ServerLevel)player.m_9236_();
                     serverLevel.m_6263_(
                        null,
                        player.m_20185_(),
                        player.m_20186_(),
                        player.m_20189_(),
                        (SoundEvent)EpicFightSounds.FORBIDDEN_STRENGTH.get(),
                        player.m_5720_(),
                        1.0F,
                        1.0F
                     );
                     serverLevel.m_8767_(
                        ParticleTypes.f_123798_, player.m_20185_(), player.m_20227_(0.5), player.m_20189_(), (int)healthConsumeAmount, 0.1, 0.0, 0.1, 0.2
                     );
                     serverLevel.m_8767_(
                        ParticleTypes.f_123746_, player.m_20185_(), player.m_20227_(0.5), player.m_20189_(), (int)healthConsumeAmount * 3, 0.0, 0.0, 0.75, 0.1
                     );
                  }
               }
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CONSUME_EVENT, EVENT_UUID);
   }

   @Override
   public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
      consumptionList.add(
         Component.m_237115_("attribute.name.epicfight.health.consume.tooltip"),
         Component.m_237115_("skill.epicfight.forbidden_strength.consume.tooltip"),
         SkillBookScreen.HEALTH_TEXTURE_INFO
      );
      return true;
   }
}
