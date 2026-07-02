package yesman.epicfight.skill.passive;

import java.util.UUID;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.DeathHarvestOrb;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class DeathHarvestSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("816118e6-b902-11ed-afa1-0242ac120002");

   public DeathHarvestSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      container.getExecutor()
         .getEventListener()
         .addEventListener(
            PlayerEventListener.EventType.PLAYER_KILLED_EVENT,
            EVENT_UUID,
            event -> {
               PlayerPatch<?> playerpatch = container.getExecutor();
               Player original = playerpatch.getOriginal();
               LivingEntity target = event.getKilledEntity();
               if (event.getDamageSource().m_269533_(EpicFightDamageTypeTags.WEAPON_INNATE)) {
                  original.m_9236_()
                     .m_6263_(null, original.m_20185_(), original.m_20186_(), original.m_20189_(), SoundEvents.f_12554_, original.m_5720_(), 0.3F, 1.25F);
                  int damage = (int)original.m_21133_(Attributes.f_22281_);
                  DeathHarvestOrb harvestOrb = new DeathHarvestOrb(
                     original, target.m_20185_(), target.m_20186_() + target.m_20206_() * 0.5, target.m_20189_(), damage
                  );
                  original.m_9236_().m_7967_(harvestOrb);
               }
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
   }
}
