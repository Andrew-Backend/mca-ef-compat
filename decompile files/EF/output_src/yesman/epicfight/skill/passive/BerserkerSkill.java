package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class BerserkerSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("fdc09ee8-fcfc-11eb-9a03-0242ac130003");
   private float speedBonus;
   private float damageBonus;

   public BerserkerSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.speedBonus = parameters.m_128457_("speed_bonus");
      this.damageBonus = parameters.m_128457_("damage_bonus");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(PlayerEventListener.EventType.MODIFY_ATTACK_SPEED_EVENT, EVENT_UUID, event -> {
         Player player = event.getPlayerPatch().getOriginal();
         float health = player.m_21223_();
         float maxHealth = player.m_21233_();
         float lostHealthPercentage = (maxHealth - health) / maxHealth;
         lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F) * 0.01F * this.speedBonus;
         float attackSpeed = event.getAttackSpeed();
         event.setAttackSpeed(Math.min(5.0F, attackSpeed * (1.0F + lostHealthPercentage)));
      });
      listener.addEventListener(PlayerEventListener.EventType.MODIFY_DAMAGE_EVENT, EVENT_UUID, event -> {
         Player player = event.getPlayerPatch().getOriginal();
         float health = player.m_21223_();
         float maxHealth = player.m_21233_();
         float lostHealthPercentage = (maxHealth - health) / maxHealth;
         lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F) * 0.01F * this.damageBonus;
         event.attachValueModifier(ValueModifier.multiplier(1.0F + lostHealthPercentage));
      });
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onInitiateClient(final SkillContainer container) {
      final Player player = container.getExecutor().getOriginal();
      container.getExecutor().getEntityDecorations().addDecorationOverlay(EntityDecorations.BERSERKER_OVERLAY, new EntityDecorations.DecorationOverlay() {
         @Override
         public Vector4f color(float partialTick) {
            float alpha = Mth.m_144920_(0.0F, 0.42F, 1.0F - player.m_21223_() / player.m_21233_());
            return new Vector4f(0.66F, 0.06F, 0.07F, alpha);
         }

         @Override
         public boolean shouldRemove() {
            return container.getExecutor().getSkill(BerserkerSkill.this) == null;
         }
      });
      container.getExecutor()
         .getEntityDecorations()
         .addParticleGenerator(
            EntityDecorations.BERSERKER_PARTICLE,
            new EntityDecorations.ParticleGenerator() {
               @Override
               public void generateParticles() {
                  float healthRatio = player.m_21223_() / player.m_21233_();
                  RandomSource random = player.m_217043_();
                  float chance = Mth.m_144920_(0.0F, 0.04F, 1.0F - healthRatio - 0.2F);

                  for (int i = 0; i < 4; i++) {
                     if (random.m_188501_() < chance) {
                        player.m_9236_()
                           .m_7106_(
                              ParticleTypes.f_123759_,
                              player.m_20185_() + random.m_188583_() * 0.4F,
                              player.m_20186_() + player.m_20206_() * 0.5 + random.m_188583_() * 0.6F,
                              player.m_20189_() + random.m_188583_() * 0.4F,
                              0.0,
                              0.2F,
                              0.0
                           );
                     }
                  }
               }

               @Override
               public boolean shouldRemove() {
                  return container.getExecutor().getSkill(BerserkerSkill.this) == null;
               }
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.MODIFY_ATTACK_SPEED_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.MODIFY_DAMAGE_EVENT, EVENT_UUID);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      Player player = container.getExecutor().getOriginal();
      float health = player.m_21223_();
      float maxHealth = player.m_21233_();
      return maxHealth - health > 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      guiGraphics.m_280411_(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
      Player player = container.getExecutor().getOriginal();
      float health = player.m_21223_();
      float maxHealth = player.m_21233_();
      float lostHealthPercentage = (maxHealth - health) / maxHealth;
      lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F);
      guiGraphics.drawString(gui.getFont(), String.format("%.0f%%", lostHealthPercentage), x + 4.0F, y + 6.0F, 16777215, true);
      poseStack.m_85849_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(String.format("%.1f", this.speedBonus));
      list.add(String.format("%.1f", this.damageBonus));
      return list;
   }
}
