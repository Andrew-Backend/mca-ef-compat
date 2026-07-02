package yesman.epicfight.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.ModLoader;
import org.joml.Matrix4f;
import yesman.epicfight.api.client.forgeevent.AttributeIconRegisterEvent;
import yesman.epicfight.api.client.forgeevent.WeaponCategoryIconRegisterEvent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangeSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;

public class SkillBookScreen extends Screen {
   private static final Map<WeaponCategory, ItemStack> WEAPON_CATEGORY_ICONS = Maps.newHashMap();
   private static final Map<Attribute, SkillBookScreen.TextureInfo> ATTRIBUTE_ICONS = Maps.newHashMap();
   private static final ResourceLocation SKILLBOOK_BACKGROUND = EpicFightMod.identifier("textures/gui/screen/skillbook.png");
   public static final SkillBookScreen.TextureInfo HEALTH_TEXTURE_INFO = new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 22, 205, 10, 10);
   public static final SkillBookScreen.TextureInfo STAMINA_TEXTURE_INFO = new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 32, 205, 10, 10);
   public static final SkillBookScreen.TextureInfo COOLDOWN_TEXTURE_INFO = new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 42, 205, 10, 10);
   protected final Player opener;
   protected final LocalPlayerPatch playerpatch;
   protected final Skill skill;
   protected final Screen parentScreen;
   protected final SkillBookScreen.SkillTooltipList skillTooltipList;
   protected final SkillBookScreen.AvailableItemsList availableWeaponCategoryList;
   protected final SkillBookScreen.AttributeIconList consumptionList;
   protected final SkillBookScreen.AttributeIconList providingAttributesList;
   protected final InteractionHand hand;
   private double customScale;
   private Button learnButton;

   public static void registerIconItems() {
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.NOT_WEAPON, new ItemStack(Items.f_41852_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.AXE, new ItemStack(Items.f_42386_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.FIST, new ItemStack((ItemLike)EpicFightItems.GLOVE.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.GREATSWORD, new ItemStack((ItemLike)EpicFightItems.IRON_GREATSWORD.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.HOE, new ItemStack(Items.f_42387_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.PICKAXE, new ItemStack(Items.f_42385_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.SHOVEL, new ItemStack(Items.f_42384_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.SWORD, new ItemStack(Items.f_42383_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.UCHIGATANA, new ItemStack((ItemLike)EpicFightItems.UCHIGATANA.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.SPEAR, new ItemStack((ItemLike)EpicFightItems.IRON_SPEAR.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.TACHI, new ItemStack((ItemLike)EpicFightItems.IRON_TACHI.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.TRIDENT, new ItemStack(Items.f_42713_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.LONGSWORD, new ItemStack((ItemLike)EpicFightItems.IRON_LONGSWORD.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.DAGGER, new ItemStack((ItemLike)EpicFightItems.IRON_DAGGER.get()));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.SHIELD, new ItemStack(Items.f_42740_));
      WEAPON_CATEGORY_ICONS.put(CapabilityItem.WeaponCategories.RANGED, new ItemStack(Items.f_42411_));
      ATTRIBUTE_ICONS.put(Attributes.f_22276_, new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 22, 195, 10, 10));
      ATTRIBUTE_ICONS.put((Attribute)EpicFightAttributes.MAX_STAMINA.get(), new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 42, 195, 10, 10));
      ATTRIBUTE_ICONS.put(Attributes.f_22281_, new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 52, 195, 10, 10));
      ATTRIBUTE_ICONS.put((Attribute)EpicFightAttributes.STAMINA_REGEN.get(), new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 62, 195, 10, 10));
      ATTRIBUTE_ICONS.put(Attributes.f_22283_, new SkillBookScreen.TextureInfo(SKILLBOOK_BACKGROUND, 72, 195, 10, 10));
      WeaponCategoryIconRegisterEvent weaponCategoryIconRegisterEvent = new WeaponCategoryIconRegisterEvent(WEAPON_CATEGORY_ICONS);
      ModLoader.get().postEvent(weaponCategoryIconRegisterEvent);
      AttributeIconRegisterEvent attributeIconRegisterEvent = new AttributeIconRegisterEvent(ATTRIBUTE_ICONS);
      ModLoader.get().postEvent(attributeIconRegisterEvent);
   }

   public Button getLearnButton() {
      return this.learnButton;
   }

   public SkillBookScreen(Player opener, ItemStack stack, @Nullable InteractionHand hand) {
      this(opener, SkillBookItem.getContainSkill(stack), hand, null);
   }

   public SkillBookScreen(Player opener, Skill skill, @Nullable InteractionHand hand, @Nullable Screen parentScreen) {
      super(Component.m_237119_());
      this.f_96541_ = Minecraft.m_91087_();
      this.f_96547_ = Minecraft.m_91087_().f_91062_;
      this.opener = opener;
      this.playerpatch = EpicFightCapabilities.getEntityPatch(this.opener, LocalPlayerPatch.class);
      this.skill = skill;
      this.hand = hand;
      this.parentScreen = parentScreen;
      this.skillTooltipList = new SkillBookScreen.SkillTooltipList(Minecraft.m_91087_(), 0, 0, 0, 0, 9);
      this.availableWeaponCategoryList = new SkillBookScreen.AvailableItemsList(0, 0);
      this.consumptionList = new SkillBookScreen.AttributeIconList(Minecraft.m_91087_(), 0, 0, 100, 100, 16);
      this.providingAttributesList = new SkillBookScreen.AttributeIconList(Minecraft.m_91087_(), 0, 0, 100, 100, 16);
      List<FormattedCharSequence> list = Minecraft.m_91087_()
         .f_91062_
         .m_92923_(
            Component.m_237110_(this.skill.getTranslationKey() + ".tooltip", this.skill.getTooltipArgsOfScreen(Lists.newArrayList()).toArray(new Object[0])),
            148
         );
      list.forEach(this.skillTooltipList::add);
      if (this.skill.getAvailableWeaponCategories() != null) {
         this.skill.getAvailableWeaponCategories().forEach(this.availableWeaponCategoryList::addWeaponCategory);
      }

      if (!this.skill.getCustomConsumptionTooltips(this.consumptionList)) {
         this.consumptionList.m_6702_().clear();
         switch (this.skill.getResourceType()) {
            case WEAPON_CHARGE:
            default:
               break;
            case COOLDOWN:
               this.consumptionList
                  .add(
                     Component.m_237115_(EpicFightMod.format("attribute.name.%s.cooldown.consume.tooltip")),
                     Component.m_237110_(
                        EpicFightMod.format("attribute.name.%s.cooldown.consume"), new Object[]{String.format("%.1f", this.skill.getConsumption())}
                     ),
                     COOLDOWN_TEXTURE_INFO
                  );
               break;
            case STAMINA:
               this.consumptionList
                  .add(
                     Component.m_237115_(EpicFightMod.format("attribute.name.%s.stamina.consume.tooltip")),
                     Component.m_237110_(
                        EpicFightMod.format("attribute.name.%s.stamina.consume"), new Object[]{String.format("%.1f", this.skill.getConsumption())}
                     ),
                     STAMINA_TEXTURE_INFO
                  );
               break;
            case HEALTH:
               this.consumptionList
                  .add(
                     Component.m_237115_(EpicFightMod.format("attribute.name.%s.health.consume.tooltip")),
                     Component.m_237115_(EpicFightMod.format("attribute.name.%s.health.consume")),
                     HEALTH_TEXTURE_INFO
                  );
         }
      }

      this.skill.getModfierEntry().forEach(entry -> this.providingAttributesList.add(entry.getKey(), entry.getValue(), ATTRIBUTE_ICONS.get(entry.getKey())));
   }

   protected void m_7856_() {
      Optional<SkillContainer> skillContainer = this.playerpatch.getSkillContainerFor(this.skill);
      Optional<SkillContainer> priorSkillContainer = this.skill == null ? null : this.playerpatch.getSkillContainerFor(this.skill.getPriorSkill());
      boolean isUsing = skillContainer.isPresent();
      boolean meetsCondition = this.skill == null ? false : this.skill.getPriorSkill() == null || priorSkillContainer.isPresent();
      Component tooltip = CommonComponents.f_237098_;
      if (!isUsing) {
         if (meetsCondition) {
            if (skillContainer.isPresent()) {
               tooltip = Component.m_237110_(
                  EpicFightMod.format("gui.%s.replace"), new Object[]{Component.m_237115_(this.skill.getTranslationKey()).getString()}
               );
            }
         } else {
            tooltip = Component.m_237110_(
               EpicFightMod.format("gui.%s.require_to_learn"), new Object[]{Component.m_237115_(this.skill.getPriorSkill().getTranslationKey()).getString()}
            );
         }
      }

      Window window = Minecraft.m_91087_().m_91268_();
      if (window.m_85446_() < 270 && window.m_85449_() > 1.0) {
         this.customScale = window.m_85449_() - 1.0;
         this.f_96543_ = (int)(window.m_85441_() / this.customScale);
         this.f_96544_ = (int)(window.m_85442_() / this.customScale);
      } else {
         this.customScale = window.m_85449_();
      }

      this.learnButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s") + (isUsing ? ".applied" : (meetsCondition ? ".learn" : ".unusable"))), button -> {
               Set<SkillContainer> skillContainers = this.playerpatch.getSkillCapability().getSkillContainersFor(this.skill.getCategory());
               if (skillContainers.size() == 1) {
                  this.acquireSkillTo(skillContainers.iterator().next());
               } else {
                  SlotSelectScreen slotSelectScreen = new SlotSelectScreen(skillContainers, this);
                  this.f_96541_.m_91152_(slotSelectScreen);
               }
            }
         )
         .m_252987_(this.f_96543_ / 2 + 54, this.f_96544_ / 2 + 90, 67, 21)
         .m_257505_(Tooltip.m_257563_(tooltip, null))
         .build(x$0 -> new SkillBookScreen.LearnButton(x$0));
      if (isUsing || !meetsCondition) {
         this.learnButton.f_93623_ = false;
      }

      if (this.hand == null) {
         this.learnButton.f_93624_ = false;
      }

      this.availableWeaponCategoryList.m_252865_(this.f_96543_ / 2 + 21);
      this.availableWeaponCategoryList.m_253211_(this.f_96544_ / 2 + 50);
      this.skillTooltipList
         .m_93437_(210, 400, this.f_96544_ / 2 - 100, (this.f_96544_ + (this.availableWeaponCategoryList.availableCategories.size() == 0 ? 150 : 80)) / 2);
      this.skillTooltipList.m_93507_(this.f_96543_ / 2 - 40);
      int consumptionEndPos = this.f_96544_ / 2 + 20 + 20 * Math.min(2, this.consumptionList.m_6702_().size());
      this.consumptionList.m_93437_(140, 300, this.f_96544_ / 2 + 20, consumptionEndPos);
      this.consumptionList.m_93507_(this.f_96543_ / 2 - 160);
      this.providingAttributesList.m_93437_(140, 300, consumptionEndPos, consumptionEndPos + 60);
      this.providingAttributesList.m_93507_(this.f_96543_ / 2 - 160);
      this.m_142416_(this.learnButton);
      this.m_142416_(this.skillTooltipList);
      this.m_142416_(this.availableWeaponCategoryList);
      if (this.consumptionList.m_6702_().size() > 0) {
         this.m_142416_(this.consumptionList);
      }

      if (this.providingAttributesList.m_6702_().size() > 0) {
         this.m_142416_(this.providingAttributesList);
      }
   }

   protected void acquireSkillTo(SkillContainer skillContainer) {
      skillContainer.setSkill(this.skill);
      this.playerpatch.getSkillCapability().addLearnedSkill(this.skill);
      int i = this.hand == InteractionHand.MAIN_HAND ? this.opener.m_150109_().f_35977_ : 40;
      EpicFightNetworkManager.sendToServer(new CPChangeSkill(skillContainer.getSlot(), i, this.skill));
      this.f_96541_.m_91152_(null);
   }

   protected boolean consumesItem() {
      return true;
   }

   public void m_7379_() {
      if (this.parentScreen != null) {
         this.f_96541_.m_91152_(this.parentScreen);
      } else {
         super.m_7379_();
      }
   }

   public boolean m_6375_(double x, double y, int button) {
      Window window = Minecraft.m_91087_().m_91268_();
      return super.m_6375_((int)(x * window.m_85449_() / this.customScale), (int)(y * window.m_85449_() / this.customScale), button);
   }

   public boolean m_6348_(double x, double y, int button) {
      Window window = Minecraft.m_91087_().m_91268_();
      return super.m_6348_((int)(x * window.m_85449_() / this.customScale), (int)(y * window.m_85449_() / this.customScale), button);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      Window window = Minecraft.m_91087_().m_91268_();
      return super.m_7979_((int)(mouseX * window.m_85449_() / this.customScale), (int)(mouseY * window.m_85449_() / this.customScale), button, dx, dy);
   }

   public boolean m_6050_(double pMouseX, double pMouseY, double pDelta) {
      Window window = Minecraft.m_91087_().m_91268_();
      return super.m_6050_((int)(pMouseX * window.m_85449_() / this.customScale), (int)(pMouseY * window.m_85449_() / this.customScale), pDelta);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.render(guiGraphics, mouseX, mouseY, partialTicks, false);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, boolean asBackground) {
      guiGraphics.m_280168_().m_85836_();
      Window window = Minecraft.m_91087_().m_91268_();
      double originalScale = window.m_85449_();
      if (originalScale != this.customScale) {
         window.m_85378_(this.customScale);
         Matrix4f matrix4f = new Matrix4f()
            .setOrtho(
               0.0F,
               (float)(window.m_85441_() / window.m_85449_()),
               (float)(window.m_85442_() / window.m_85449_()),
               0.0F,
               1000.0F,
               ForgeHooksClient.getGuiFarPlane()
            );
         RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.f_276633_);
      }

      if (!asBackground) {
         this.m_280273_(guiGraphics);
      }

      int posX = (this.f_96543_ - 284) / 2;
      int posY = (this.f_96544_ - 165) / 2;
      RenderSystem.setShader(GameRenderer::m_172820_);
      guiGraphics.m_280411_(SKILLBOOK_BACKGROUND, this.f_96543_ / 2 - 192, this.f_96544_ / 2 - 140, 384, 279, 0.0F, 0.0F, 256, 186, 256, 256);
      int iconStartX = 106;
      int iconStartY = 211;
      if (this.skill.getCategory() == SkillCategories.DODGE) {
         iconStartX += 9;
      } else if (this.skill.getCategory() == SkillCategories.GUARD) {
         iconStartX += 18;
      } else if (this.skill.getCategory() == SkillCategories.IDENTITY) {
         iconStartX += 27;
      } else if (this.skill.getCategory() == SkillCategories.MOVER) {
         iconStartX += 36;
      } else if (this.skill.getCategory() == SkillCategories.PASSIVE) {
         iconStartX += 45;
      }

      guiGraphics.m_280411_(SKILLBOOK_BACKGROUND, this.f_96543_ / 2 - 160, this.f_96544_ / 2 - 73, 12, 12, iconStartX, iconStartY, 9, 9, 256, 256);
      guiGraphics.m_280168_().m_85836_();
      guiGraphics.m_280168_().m_85837_(this.f_96543_ / 2 - 16, this.f_96544_ / 2 - 73, 0.0);
      guiGraphics.m_280168_().m_85841_(-1.0F, 1.0F, 1.0F);
      RenderSystem.disableCull();
      guiGraphics.m_280411_(SKILLBOOK_BACKGROUND, 0, 0, 12, 12, iconStartX, iconStartY, 9, 9, 256, 256);
      RenderSystem.enableCull();
      guiGraphics.m_280168_().m_85849_();
      RenderSystem.enableBlend();
      guiGraphics.m_280411_(this.skill.getSkillTexture(), this.f_96543_ / 2 - 122, this.f_96544_ / 2 - 99, 68, 68, 0.0F, 0.0F, 128, 128, 128, 128);
      RenderSystem.disableBlend();
      String translationName = this.skill.getTranslationKey();
      String skillName = Component.m_237115_(translationName).getString();
      int width = this.f_96547_.m_92895_(skillName);
      guiGraphics.m_280056_(this.f_96547_, skillName, posX + 56 - width / 2, posY + 75, 0, false);
      String skillCategory = String.format(
         "(%s)", Component.m_237115_(EpicFightMod.format("skill.%s") + "." + this.skill.getCategory().toString().toLowerCase() + ".category").getString()
      );
      width = this.f_96547_.m_92895_(skillCategory);
      guiGraphics.m_280056_(this.f_96547_, skillCategory, posX + 56 - width / 2, posY + 90, 0, false);
      super.m_88315_(guiGraphics, (int)(mouseX * originalScale / this.customScale), (int)(mouseY * originalScale / this.customScale), partialTicks);
      if (asBackground) {
         this.m_280273_(guiGraphics);
      }

      guiGraphics.m_280168_().m_85849_();
      if (originalScale != this.customScale) {
         window.m_85378_(originalScale);
         Matrix4f matrix4f = new Matrix4f()
            .setOrtho(
               0.0F,
               (float)(window.m_85441_() / window.m_85449_()),
               (float)(window.m_85442_() / window.m_85449_()),
               0.0F,
               1000.0F,
               ForgeHooksClient.getGuiFarPlane()
            );
         RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.f_276633_);
      }
   }

   private static class AttributeIcon extends AbstractWidget {
      private final SkillBookScreen.TextureInfo textureInfo;

      public AttributeIcon(int x, int y, int width, int height, Attribute attribute, SkillBookScreen.TextureInfo textureInfo) {
         super(x, y, width, height, Component.m_237115_(attribute.m_22087_() + ".skillbook_tooltip"));
         this.textureInfo = textureInfo;
      }

      public AttributeIcon(int x, int y, int width, int height, Component customTooltip, SkillBookScreen.TextureInfo textureInfo) {
         super(x, y, width, height, customTooltip);
         this.textureInfo = textureInfo;
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         if (this.textureInfo != null) {
            guiGraphics.m_280411_(
               this.textureInfo.resourceLocation,
               this.m_252754_(),
               this.m_252907_(),
               this.m_5711_(),
               this.m_93694_(),
               this.textureInfo.u,
               this.textureInfo.v,
               this.textureInfo.width,
               this.textureInfo.height,
               256,
               256
            );
         }

         if (this.m_274382_()) {
            this.m_257544_(this.m_6035_() == null ? null : Tooltip.m_257550_(this.m_6035_()));
         } else {
            this.m_257544_(null);
         }
      }

      protected void m_168797_(NarrationElementOutput p_259858_) {
      }
   }

   public class AttributeIconList extends ContainerObjectSelectionList<SkillBookScreen.AttributeIconList.ProvidingAttributeEntry> {
      public AttributeIconList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
         super(minecraft, width, height, y0, y1, itemHeight);
         this.m_93488_(false);
         this.m_93473_(false, 0);
         this.m_93496_(false);
      }

      public void add(Attribute attribute, AttributeModifier ability, SkillBookScreen.TextureInfo textureInfo) {
         this.m_7085_(new SkillBookScreen.AttributeIconList.ProvidingAttributeEntry(attribute, ability, textureInfo));
      }

      public void add(Component tooltip, Component descriptor, SkillBookScreen.TextureInfo textureInfo) {
         this.m_7085_(new SkillBookScreen.AttributeIconList.ProvidingAttributeEntry(tooltip, descriptor, textureInfo));
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      public int m_5747_() {
         return this.f_93393_ + 2;
      }

      private class ProvidingAttributeEntry extends Entry<SkillBookScreen.AttributeIconList.ProvidingAttributeEntry> {
         private List<AbstractWidget> icons = Lists.newArrayList();

         private ProvidingAttributeEntry(Attribute attribute, AttributeModifier ability, SkillBookScreen.TextureInfo textureInfo) {
            String amountString = "";
            String operator = "+";
            double amount = ability.m_22218_();
            if (amount < 0.0) {
               operator = "-";
               amount = Math.abs(amount);
            }

            switch (ability.m_22217_()) {
               case ADDITION:
                  amountString = ItemStack.f_41584_.format(amount);
                  break;
               case MULTIPLY_BASE:
               case MULTIPLY_TOTAL:
                  amountString = ItemStack.f_41584_.format(amount * 100.0) + "%";
            }

            this.icons.add(new SkillBookScreen.AttributeIcon(0, 0, 12, 12, attribute, textureInfo));
            Static abilityString = new Static(
               SkillBookScreen.this,
               0,
               140,
               0,
               15,
               null,
               null,
               Component.m_237113_(operator + amountString + " " + Component.m_237115_(attribute.m_22087_()).getString())
            );
            abilityString.setColor(0, 0, 0);
            this.icons.add(abilityString);
         }

         private ProvidingAttributeEntry(Component customTooltip, Component customDescription, SkillBookScreen.TextureInfo textureInfo) {
            this.icons.add(new SkillBookScreen.AttributeIcon(0, 0, 12, 12, customTooltip, textureInfo));
            Static abilityString = new Static(SkillBookScreen.this, 0, 140, 0, 15, null, null, customDescription);
            abilityString.setColor(0, 0, 0);
            this.icons.add(abilityString);
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
         ) {
            int widgetLeft = left;

            for (AbstractWidget widget : this.icons) {
               widget.m_264152_(widgetLeft, top);
               widget.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
               widgetLeft += widget.m_5711_() + 4;
            }
         }

         public List<? extends GuiEventListener> m_6702_() {
            return this.icons;
         }

         public List<? extends NarratableEntry> m_142437_() {
            return this.icons;
         }
      }
   }

   private class AvailableItemsList extends AbstractWidget {
      private static final float ICON_LENGTH = 21.25F;
      private final List<WeaponCategory> availableCategories = Lists.newArrayList();
      private int startIdx;
      private int size;

      private AvailableItemsList(int x, int y) {
         super(x, y, 0, 0, Component.m_237115_(EpicFightMod.format("gui.%s.available_weapon_types")));
         this.f_93618_ = 0;
         this.f_93619_ = 28;
      }

      public void addWeaponCategory(WeaponCategory weaopnCategory) {
         this.availableCategories.add(weaopnCategory);
         this.f_93618_ = (int)(this.f_93618_ + 21.25F);
         this.size++;
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         if (this.availableCategories.size() != 0) {
            float x = this.m_252754_() + 3;
            int y = this.m_252907_() + 13;
            boolean updatedTooltip = false;
            guiGraphics.m_280411_(
               SkillBookScreen.SKILLBOOK_BACKGROUND,
               SkillBookScreen.this.f_96543_ / 2 + 20,
               SkillBookScreen.this.f_96544_ / 2 + 60,
               130,
               22,
               0.0F,
               223.0F,
               104,
               17,
               256,
               256
            );
            int displayedCount = 0;

            for (int i = this.startIdx; i < this.size && displayedCount <= 5; i++) {
               WeaponCategory category = this.availableCategories.get(i);
               if (SkillBookScreen.WEAPON_CATEGORY_ICONS.containsKey(category)) {
                  guiGraphics.m_280480_(SkillBookScreen.WEAPON_CATEGORY_ICONS.get(category), (int)x, y);
               }

               if (mouseX >= x && mouseX <= x + 21.25F && mouseY >= y && mouseY <= y + 21.25F) {
                  this.m_257544_(
                     Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("%s.weapon_category.") + category.toString().toLowerCase(Locale.ROOT)))
                  );
                  updatedTooltip = true;
               }

               x += 21.25F;
               displayedCount++;
            }

            if (!updatedTooltip) {
               this.m_257544_(null);
            }

            if (this.availableCategories.size() > 5) {
               int x1 = SkillBookScreen.this.f_96543_ / 2 + 20;
               int y1 = SkillBookScreen.this.f_96544_ / 2 + 83;
               int scrollSize = (int)(130.0 * (6.0 / this.size));
               int scrollStart = this.startIdx * (130 - scrollSize) + 1;
               guiGraphics.m_280509_(x1, y1, x1 + 130, y1 + 4, -1845578);
               guiGraphics.m_280509_(x1 + scrollStart, y1 + 1, x1 + scrollStart + scrollSize - 2, y1 + 3, -4016228);
            }

            guiGraphics.m_280614_(SkillBookScreen.this.f_96547_, this.m_6035_(), this.m_252754_(), this.m_252907_(), 0, false);
         }
      }

      public boolean m_6050_(double x, double y, double direction) {
         if (this.m_5953_(x, y) && this.size > 6) {
            this.startIdx = Mth.m_14045_((int)(this.startIdx - direction), 0, this.size - 6);
            return true;
         } else {
            return false;
         }
      }

      protected void m_168797_(NarrationElementOutput p_259858_) {
      }
   }

   private class LearnButton extends Button {
      protected LearnButton(Builder builder) {
         super(builder);
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         Minecraft minecraft = Minecraft.m_91087_();
         guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, this.f_93625_);
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         int texX = 106;
         if (this.m_198029_() || !this.m_142518_()) {
            texX = 156;
         }

         guiGraphics.m_280168_().m_85836_();
         guiGraphics.m_280027_(
            SkillBookScreen.SKILLBOOK_BACKGROUND, this.m_252754_(), this.m_252907_(), this.m_5711_(), this.m_93694_(), 20, 4, 45, 15, texX, 193
         );
         guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
         guiGraphics.m_280168_().m_85849_();
         int i = this.getFGColor();
         this.m_280139_(guiGraphics, minecraft.f_91062_, i | Mth.m_14167_(this.f_93625_ * 255.0F) << 24);
      }
   }

   private class SkillTooltipList extends ObjectSelectionList<SkillBookScreen.SkillTooltipList.TooltipLine> {
      public SkillTooltipList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
         super(minecraft, width, height, y0, y1, itemHeight);
         this.m_93488_(false);
         this.m_93473_(false, 0);
         this.m_93496_(false);
      }

      public void add(FormattedCharSequence tooltip) {
         this.m_7085_(new SkillBookScreen.SkillTooltipList.TooltipLine(tooltip));
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      private class TooltipLine extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<SkillBookScreen.SkillTooltipList.TooltipLine> {
         private final FormattedCharSequence tooltip;

         private TooltipLine(FormattedCharSequence string) {
            this.tooltip = string;
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
         ) {
            guiGraphics.m_280649_(SkillBookScreen.this.f_96547_, this.tooltip, left + 59, top, 0, false);
         }

         public Component m_142172_() {
            return Component.m_237119_();
         }
      }
   }

   public static class TextureInfo {
      final ResourceLocation resourceLocation;
      final int u;
      final int v;
      final int width;
      final int height;

      public TextureInfo(ResourceLocation resourceLocation, int u, int v, int width, int height) {
         this.resourceLocation = resourceLocation;
         this.u = u;
         this.v = v;
         this.width = width;
         this.height = height;
      }
   }
}
