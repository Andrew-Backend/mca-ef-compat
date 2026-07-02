package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

public class EverlastingAllegiance extends WeaponInnateSkill {
   private AnimationManager.AnimationAccessor<? extends StaticAnimation> callingAnimation = Animations.EVERLASTING_ALLEGIANCE_CALL;

   public static void setThrownTridentEntityId(SkillContainer skillContainer, int entityId) {
      skillContainer.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.THROWN_TRIDENT_ENTITY_ID.get(), entityId);
   }

   public static int getThrownTridentEntityId(SkillContainer skillContainer) {
      return skillContainer.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.THROWN_TRIDENT_ENTITY_ID.get());
   }

   public EverlastingAllegiance(SkillBuilder<? extends WeaponInnateSkill> builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
   }

   @Override
   public boolean checkExecuteCondition(SkillContainer container) {
      return container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.THROWN_TRIDENT_ENTITY_ID.get()) >= 0;
   }

   @Override
   public boolean canExecute(SkillContainer container) {
      return this.checkExecuteCondition(container);
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      super.executeOnServer(container, args);
      if (container.getExecutor()
         .getOriginal()
         .m_9236_()
         .m_6815_(container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.THROWN_TRIDENT_ENTITY_ID.get())) instanceof ThrownTrident trident
         )
       {
         ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(trident, ThrownTridentPatch.class);
         tridentPatch.recalledBySkill();
         container.getExecutor().playAnimationSynchronized(this.callingAnimation, 0.0F);
         this.cancelOnServer(container, args);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
      super.cancelOnClient(container, args);
      if (container.getExecutor()
         .getOriginal()
         .m_9236_()
         .m_6815_(container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.THROWN_TRIDENT_ENTITY_ID.get())) instanceof ThrownTrident trident
         )
       {
         EpicFightCapabilities.getUnparameterizedEntityPatch(trident, ThrownTridentPatch.class).ifPresent(ThrownTridentPatch::recalledBySkill);
      }
   }

   @Override
   public void updateContainer(SkillContainer container) {
      super.updateContainer(container);
      int thrownTrident = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.THROWN_TRIDENT_ENTITY_ID.get());
      if (container.isDisabled() && thrownTrident >= 0) {
         container.setDisabled(false);
      } else if (!container.isDisabled() && thrownTrident < 0) {
         container.setDisabled(true);
      }
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
      List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
      this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Returning Trident:");
      return list;
   }

   public WeaponInnateSkill registerPropertiesToAnimation() {
      return this;
   }
}
