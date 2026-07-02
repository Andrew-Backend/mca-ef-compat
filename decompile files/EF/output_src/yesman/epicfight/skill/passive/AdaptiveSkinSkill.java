package yesman.epicfight.skill.passive;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class AdaptiveSkinSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("e9cd15f0-72cc-474b-bfee-66276d06157d");
   private final Map<TagKey<DamageType>, Vec3f> protectableDamageTypeTags;
   private float damageResistance;
   private int maxResistanceStack;

   public static AdaptiveSkinSkill.Builder createAdaptiveSkinBuilder() {
      return new AdaptiveSkinSkill.Builder()
         .addProtectableDamageTypeTags(
            ImmutableMap.of(
               EpicFightDamageTypeTags.IS_MELEE,
               new Vec3f(0.8901961F, 0.49803922F, 0.49803922F),
               DamageTypeTags.f_268524_,
               new Vec3f(0.4F, 0.77254903F, 1.0F),
               DamageTypeTags.f_268745_,
               new Vec3f(0.8980392F, 0.56078434F, 0.25882354F),
               EpicFightDamageTypeTags.IS_MAGIC,
               new Vec3f(0.8862745F, 0.6039216F, 0.91764706F),
               DamageTypeTags.f_268415_,
               new Vec3f(0.8117647F, 0.8039216F, 0.47058824F)
            )
         )
         .setCategory(SkillCategories.PASSIVE)
         .setResource(Skill.Resource.NONE);
   }

   public AdaptiveSkinSkill(AdaptiveSkinSkill.Builder builder) {
      super(builder);
      this.protectableDamageTypeTags = Collections.unmodifiableMap(builder.protectableDamageTypeTags);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.damageResistance = parameters.m_128457_("damage_resistance");
      this.maxResistanceStack = parameters.m_128451_("max_resistance_stack");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT,
         EVENT_UUID,
         event -> {
            TagKey<DamageType> currentResisting = container.getDataManager()
               .getDataValue((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
            int stacks = container.getDataManager().<Integer>getDataValueOptional((SkillDataKey<Integer>)SkillDataKeys.STACKS.get()).orElse(0);
            if (event.getDamageSource().m_269533_(currentResisting)) {
               event.attachValueModifier(ValueModifier.multiplier(1.0F - this.damageResistance * stacks));
               if (stacks < this.maxResistanceStack) {
                  container.getExecutor().playSound((SoundEvent)EpicFightSounds.ADAPTIVE_SKIN_INCREASE.get(), 1.0F, 0.0F, 0.0F);
                  container.getDataManager().setDataSyncF((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), v -> v + 1);
               }

               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().f_19797_);
            } else if (stacks <= 1) {
               for (TagKey<DamageType> protectableTag : this.protectableDamageTypeTags.keySet()) {
                  if (event.getDamageSource().m_269533_(protectableTag)) {
                     container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), 1);
                     container.getDataManager().setDataSync((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get(), protectableTag);
                     container.getDataManager()
                        .setDataSync((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().f_19797_);
                     if (stacks == 0) {
                        container.getExecutor().playSound((SoundEvent)EpicFightSounds.ADAPTIVE_SKIN_INCREASE.get(), 1.0F, 0.0F, 0.0F);
                     } else {
                        container.getExecutor().playSound((SoundEvent)EpicFightSounds.ADAPTIVE_SKIN_DECREASE.get(), 1.0F, 0.0F, 0.0F);
                     }
                     break;
                  }
               }
            } else {
               boolean adaptableType = false;

               for (TagKey<DamageType> protectableTag : this.protectableDamageTypeTags.keySet()) {
                  if (event.getDamageSource().m_269533_(protectableTag)) {
                     adaptableType = true;
                     break;
                  }
               }

               if (adaptableType) {
                  container.getExecutor().playSound((SoundEvent)EpicFightSounds.ADAPTIVE_SKIN_DECREASE.get(), 1.0F, 0.0F, 0.0F);
                  container.getDataManager().setDataSyncF((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), v -> v - 1);
               }
            }
         }
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onInitiateClient(final SkillContainer container) {
      container.getExecutor()
         .getEntityDecorations()
         .addDecorationOverlay(
            EntityDecorations.ADAPTIVE_SKIN_OVERLAY,
            new EntityDecorations.DecorationOverlay() {
               @Override
               public RenderType getRenderType() {
                  TagKey<DamageType> resistingDamageTypeTagKey = container.getExecutor()
                     .getSkill(AdaptiveSkinSkill.this)
                     .getDataManager()
                     .getDataValue((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
                  Vec3f color = AdaptiveSkinSkill.this.getGlintColor(resistingDamageTypeTagKey);
                  return EpicFightRenderTypes.coloredGlintWorldRendertype(container.getExecutor().getOriginal(), color.x, color.y, color.z);
               }

               @Override
               public boolean shouldRender() {
                  return container.getExecutor()
                        .getSkill(AdaptiveSkinSkill.this)
                        .getDataManager()
                        .<TagKey<DamageType>>getDataValueOptional((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get())
                        .orElse(EpicFightDamageTypeTags.NONE)
                     != EpicFightDamageTypeTags.NONE;
               }

               @Override
               public boolean shouldRemove() {
                  return container.getExecutor().getSkill(AdaptiveSkinSkill.this) == null;
               }
            }
         );
      container.getExecutor()
         .getEntityDecorations()
         .addColorModifier(
            EntityDecorations.ADAPTIVE_SKIN_COLOR,
            new EntityDecorations.RenderAttributeModifier<Vector4f>() {
               public void modifyValue(Vector4f val, float partialTick) {
                  TagKey<DamageType> resistingDamageTypeTagKey = container.getExecutor()
                     .getSkill(AdaptiveSkinSkill.this)
                     .getDataManager()
                     .getDataValue((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
                  if (!EpicFightDamageTypeTags.NONE.equals(resistingDamageTypeTagKey)) {
                     Vec3f color = AdaptiveSkinSkill.this.getGlintColor(resistingDamageTypeTagKey);
                     val.x = color.x;
                     val.y = color.y;
                     val.z = color.z;
                  }
               }

               @Override
               public boolean shouldRemove() {
                  return container.getExecutor().getSkill(AdaptiveSkinSkill.this) == null;
               }
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
   }

   @Override
   public void updateContainer(SkillContainer container) {
      super.updateContainer(container);
      TagKey<DamageType> resistingDamageTypeTag = container.getDataManager()
         .getDataValue((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
      if (!EpicFightDamageTypeTags.NONE.equals(resistingDamageTypeTag)
         && container.getExecutor().getOriginal().f_19797_
               - container.getDataManager().<Integer>getDataValueOptional((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get()).orElse(0)
            > 300) {
         container.getDataManager().setDataSync((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get(), EpicFightDamageTypeTags.NONE);
         container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), 0);
      }
   }

   private Vec3f getGlintColor(TagKey<DamageType> tagKey) {
      return this.protectableDamageTypeTags.get(tagKey);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      TagKey<DamageType> resistingDamageTypeTag = container.getDataManager()
         .getDataValue((SkillDataKey<TagKey<DamageType>>)SkillDataKeys.RESISTING_DAMAGE_TYPE.get());
      return !EpicFightDamageTypeTags.NONE.equals(resistingDamageTypeTag) && this.protectableDamageTypeTags.containsKey(resistingDamageTypeTag);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      Vec3f color = this.protectableDamageTypeTags.get(container.getDataManager().getDataValue((SkillDataKey)SkillDataKeys.RESISTING_DAMAGE_TYPE.get()));
      guiGraphics.m_280479_(this.getSkillTexture(), (int)x, (int)x + 24, (int)y, (int)y + 24, 0, 0.0F, 1.0F, 0.0F, 1.0F, color.x, color.y, color.z, 1.0F);
      int stacks = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.STACKS.get());
      if (stacks > 1) {
         guiGraphics.drawString(gui.getFont(), String.valueOf(stacks), x + 18.0F, y + 16.0F, 16777215, true);
      }

      int lastHitTick = container.getDataManager().<Integer>getDataValueOptional((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get()).orElse(0);
      if (container.getExecutor().getOriginal().f_19797_ - lastHitTick > 200) {
         int remainseconds = 1 + (100 - (container.getExecutor().getOriginal().f_19797_ - lastHitTick - 200)) / 20;
         guiGraphics.drawString(gui.getFont(), String.valueOf(remainseconds), x + 8.0F, y + 8.0F, 16777215, true);
      }

      poseStack.m_85849_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(ItemStack.f_41584_.format(this.damageResistance * 100.0F));
      list.add(this.maxResistanceStack);
      StringBuilder sb = new StringBuilder();

      for (TagKey<DamageType> tag : this.protectableDamageTypeTags.keySet()) {
         String tagKey = String.format("tag.%s.%s.%s", tag.f_203867_().m_135782_().m_135815_(), tag.f_203868_().m_135827_(), tag.f_203868_().m_135815_());
         sb.append("- " + Component.m_237115_(tagKey).getString() + "\n");
      }

      list.add(sb.toString());
      return list;
   }

   public static class Builder extends SkillBuilder<AdaptiveSkinSkill> {
      protected final Map<TagKey<DamageType>, Vec3f> protectableDamageTypeTags = new LinkedHashMap<>();

      public AdaptiveSkinSkill.Builder addProtectableDamageTypeTags(Map<TagKey<DamageType>, Vec3f> tags) {
         this.protectableDamageTypeTags.putAll(tags);
         return this;
      }
   }
}
