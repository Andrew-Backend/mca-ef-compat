package yesman.epicfight.compat.kubejs.skill;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.compat.kubejs.CallbackUtils;
import yesman.epicfight.compat.kubejs.EpicFightKubeJSPlugin;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.item.EpicFightCreativeTabs;

public class CustomSkill extends Skill {
   private final Consumer<SkillContainer> onInitiate;
   private final Consumer<SkillContainer> onRemoved;
   private final BiConsumer<SkillContainer, FriendlyByteBuf> executeOnServer;
   private final BiConsumer<SkillContainer, FriendlyByteBuf> executeOnClient;
   private final BiConsumer<SkillContainer, FriendlyByteBuf> cancelOnServer;
   private final BiConsumer<SkillContainer, FriendlyByteBuf> cancelOnClient;
   private final Consumer<CustomSkill.DrawOnGuiContext> drawOnGui;
   private final Predicate<SkillContainer> shouldDraw;
   private final Predicate<SkillContainer> canExecute;
   private final Predicate<PlayerPatch<?>> executableState;
   private final BiConsumer<SkillContainer, Float> setConsumption;
   private final Consumer<SkillContainer> updateContainer;
   private final Function<PlayerPatch<?>, Float> cooldownRegenPerSecond;
   private final int maxStackSize;
   private final int maxDuration;
   private final Predicate<PlayerPatch<?>> shouldDeactivateAutomatically;
   private final Consumer<CustomSkill.OnScreenContext> onScreen;
   private final Function<CustomSkill.GetTooltipOnItem, List<Component>> getTooltipOnItem;
   private final Function<List<Object>, List<Object>> getTooltipArgsOfScreen;
   private final ResourceLocation tab;
   private ResourceLocation textureLocation = this.getSkillTexture();

   public CustomSkill(CustomSkill.CustomSkillBuilder builder) {
      super(
         new SkillBuilder()
            .setCategory(builder.category)
            .setCreativeTab((CreativeModeTab)BuiltInRegistries.f_279662_.m_7745_(builder.tab))
            .setActivateType(builder.activateType)
            .setResource(builder.resource)
            .setRegistryName(builder.id)
      );
      this.onInitiate = builder.onInitiate;
      this.onRemoved = builder.onRemoved;
      this.executeOnServer = builder.executeOnServer;
      this.executeOnClient = builder.executeOnClient;
      this.cancelOnServer = builder.cancelOnServer;
      this.cancelOnClient = builder.cancelOnClient;
      this.drawOnGui = builder.drawOnGui;
      this.shouldDraw = builder.shouldDraw;
      this.canExecute = builder.canExecute;
      this.executableState = builder.executableState;
      this.setConsumption = builder.setConsumption;
      this.updateContainer = builder.updateContainer;
      this.cooldownRegenPerSecond = builder.cooldownRegenPerSecond;
      this.maxStackSize = builder.maxStackSize;
      this.maxDuration = builder.maxDuration;
      this.shouldDeactivateAutomatically = builder.shouldDeactivateAutomatically;
      this.onScreen = builder.onScreen;
      this.getTooltipOnItem = builder.getTooltipOnItem;
      this.getTooltipArgsOfScreen = builder.getTooltipArgsOfScreen;
      if (builder.textureLocation != null) {
         this.textureLocation = builder.textureLocation;
      }

      this.tab = builder.tab;
   }

   @Override
   public boolean shouldDraw(SkillContainer container) {
      return this.shouldDraw != null ? this.shouldDraw.test(container) : super.shouldDraw(container);
   }

   @Override
   public boolean canExecute(SkillContainer container) {
      return this.canExecute != null ? this.canExecute.test(container) : super.canExecute(container);
   }

   @Override
   public boolean isExecutableState(PlayerPatch<?> executor) {
      return this.executableState != null ? this.executableState.test(executor) : super.isExecutableState(executor);
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      if (this.executeOnServer != null) {
         CallbackUtils.biSafeCallback(this.executeOnServer, container, args, "Error while executing executeOnServer for skill: " + this.getRegistryName());
      }

      super.executeOnServer(container, args);
   }

   @Override
   public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
      if (this.executeOnClient != null) {
         CallbackUtils.biSafeCallback(this.executeOnClient, container, args, "Error while executing executeOnClient for skill: " + this.getRegistryName());
      }

      super.executeOnClient(container, args);
   }

   @Override
   public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
      if (this.cancelOnServer != null) {
         CallbackUtils.biSafeCallback(this.cancelOnServer, container, args, "Error while executing cancelOnServer for skill: " + this.getRegistryName());
      }

      super.cancelOnServer(container, args);
   }

   @Override
   public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
      if (this.cancelOnClient != null) {
         CallbackUtils.biSafeCallback(this.cancelOnClient, container, args, "Error while executing cancelOnClient for skill: " + this.getRegistryName());
      }

      super.cancelOnClient(container, args);
   }

   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      if (this.drawOnGui != null) {
         CustomSkill.DrawOnGuiContext context = new CustomSkill.DrawOnGuiContext(gui, container, guiGraphics, x, y);
         CallbackUtils.safeCallback(this.drawOnGui, context, "Error while drawing HUD for skill: " + this.getRegistryName());
      }

      super.drawOnGui(gui, container, guiGraphics, x, y, partialTick);
   }

   @Override
   public void onRemoved(SkillContainer container) {
      if (this.onRemoved != null) {
         CallbackUtils.safeCallback(this.onRemoved, container, "Error while executing onRemoved for skill: " + this.getRegistryName());
      }

      super.onRemoved(container);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      if (this.onInitiate != null) {
         CallbackUtils.safeCallback(this.onInitiate, container, "Error while executing onInitiate for skill: " + this.getRegistryName());
      }

      super.onInitiate(container);
   }

   @Override
   public void setConsumption(SkillContainer container, float value) {
      if (this.setConsumption != null) {
         CallbackUtils.biSafeCallback(this.setConsumption, container, value, "Error while executing consumption for skill: " + this.getRegistryName());
      } else {
         super.setConsumption(container, value);
      }
   }

   @Override
   public void updateContainer(SkillContainer container) {
      if (this.updateContainer != null) {
         CallbackUtils.safeCallback(this.updateContainer, container, "Error while executing updateContainer for skill: " + this.getRegistryName());
      }

      super.updateContainer(container);
   }

   @Override
   public float getCooldownRegenPerSecond(PlayerPatch<?> executor) {
      return this.cooldownRegenPerSecond != null ? this.cooldownRegenPerSecond.apply(executor) : super.getCooldownRegenPerSecond(executor);
   }

   @Override
   public int getMaxStack() {
      return this.maxStackSize;
   }

   @Override
   public int getMaxDuration() {
      return this.maxDuration;
   }

   @Override
   public boolean shouldDeactivateAutomatically(PlayerPatch<?> executor) {
      return this.shouldDeactivateAutomatically != null ? this.shouldDeactivateAutomatically.test(executor) : super.shouldDeactivateAutomatically(executor);
   }

   @Override
   public void onScreen(LocalPlayerPatch localPlayerPatch, float resolutionX, float resolutionY) {
      if (this.onScreen != null) {
         CustomSkill.OnScreenContext context = new CustomSkill.OnScreenContext(localPlayerPatch, resolutionX, resolutionY);
         CallbackUtils.safeCallback(this.onScreen, context, "Error while executing onScreen for skill: " + this.getRegistryName());
      }

      super.onScreen(localPlayerPatch, resolutionX, resolutionY);
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerPatch) {
      if (this.getTooltipOnItem != null) {
         CustomSkill.GetTooltipOnItem context = new CustomSkill.GetTooltipOnItem(itemStack, cap, playerPatch);
         return this.getTooltipOnItem.apply(context);
      } else {
         return super.getTooltipOnItem(itemStack, cap, playerPatch);
      }
   }

   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> args) {
      return this.getTooltipArgsOfScreen != null ? this.getTooltipArgsOfScreen.apply(args) : super.getTooltipArgsOfScreen(args);
   }

   @Override
   public ResourceLocation getSkillTexture() {
      return this.textureLocation;
   }

   @Override
   public CreativeModeTab getCreativeTab() {
      return this.tab != null ? (CreativeModeTab)BuiltInRegistries.f_279662_.m_7745_(this.tab) : (CreativeModeTab)EpicFightCreativeTabs.ITEMS.get();
   }

   @Info("Creates a custom skill. The builder requires one of each of the following to function:\n- category\n- activateType\n- resource\n- texture\n")
   public static class CustomSkillBuilder extends BuilderBase<Skill> {
      public ResourceLocation tab;
      public SkillCategory category;
      public Skill.ActivateType activateType = Skill.ActivateType.ONE_SHOT;
      public Skill.Resource resource = Skill.Resource.NONE;
      private Consumer<SkillContainer> onInitiate;
      private Consumer<SkillContainer> onRemoved;
      private BiConsumer<SkillContainer, FriendlyByteBuf> executeOnServer;
      private BiConsumer<SkillContainer, FriendlyByteBuf> executeOnClient;
      private BiConsumer<SkillContainer, FriendlyByteBuf> cancelOnServer;
      private BiConsumer<SkillContainer, FriendlyByteBuf> cancelOnClient;
      private Consumer<CustomSkill.DrawOnGuiContext> drawOnGui;
      private Predicate<SkillContainer> shouldDraw;
      private Predicate<SkillContainer> canExecute;
      private Predicate<PlayerPatch<?>> executableState;
      private BiConsumer<SkillContainer, Float> setConsumption;
      private Consumer<SkillContainer> updateContainer;
      private Function<PlayerPatch<?>, Float> cooldownRegenPerSecond;
      private int maxStackSize = 1;
      private int maxDuration = 0;
      private Predicate<PlayerPatch<?>> shouldDeactivateAutomatically;
      private Consumer<CustomSkill.OnScreenContext> onScreen;
      private Function<CustomSkill.GetTooltipOnItem, List<Component>> getTooltipOnItem;
      private Function<List<Object>, List<Object>> getTooltipArgsOfScreen;
      private ResourceLocation textureLocation;

      public CustomSkillBuilder(ResourceLocation id) {
         super(id);
      }

      @Info(
         "Sets the creative tab that the skill book for this skill will be in.\nOptional.\nThe KubeJS tab is `'kubejs:kubejs'` and the Epic Fight tab is `epicfight:items`.\n"
      )
      public CustomSkill.CustomSkillBuilder tab(ResourceLocation tab) {
         this.tab = tab;
         return this;
      }

      @Info("Sets the category of the skill. Input a string of the category.\nRequired.\n")
      public CustomSkill.CustomSkillBuilder category(SkillCategories category) {
         this.category = category;
         return this;
      }

      @Info("Sets the activate type of the skill. Input a string of the type.\n")
      public CustomSkill.CustomSkillBuilder activateType(Skill.ActivateType activateType) {
         this.activateType = activateType;
         return this;
      }

      @Info("Sets the resource type of the skill. Input a string of the type.\n")
      public CustomSkill.CustomSkillBuilder resource(Skill.Resource resource) {
         this.resource = resource;
         return this;
      }

      @Info("Sets the texture of the skill. Input a string or resource location of the texture.\nExample: `minecraft:textures/block/stone.png`\nRequired.\n")
      public CustomSkill.CustomSkillBuilder texture(ResourceLocation textureLocation) {
         this.textureLocation = textureLocation;
         return this;
      }

      @Info("This is called when the skill is learned by the player.\n")
      public CustomSkill.CustomSkillBuilder onInitiate(Consumer<SkillContainer> consumer) {
         this.onInitiate = consumer;
         return this;
      }

      @Info("This is called when the skill is removed from the player.\n")
      public CustomSkill.CustomSkillBuilder onRemoved(Consumer<SkillContainer> consumer) {
         this.onRemoved = consumer;
         return this;
      }

      @Info(
         "This is called when the skill is executed on the server. This is where you should put your skill logic.\nThe second argument is the buffer that is sent from the client. It's used for data synchronization.\n"
      )
      public CustomSkill.CustomSkillBuilder executeOnServer(BiConsumer<SkillContainer, FriendlyByteBuf> consumer) {
         this.executeOnServer = consumer;
         return this;
      }

      @Info(
         "This is called when the skill is executed on the client. Best to use this in sync with the server if it is a skill that moves the player.\nThe second argument is the buffer that is sent from the server. It's used for data synchronization.\n"
      )
      public CustomSkill.CustomSkillBuilder executeOnClient(BiConsumer<SkillContainer, FriendlyByteBuf> consumer) {
         this.executeOnClient = consumer;
         return this;
      }

      @Info("Called when the skill is cancelled on the server.\n")
      public CustomSkill.CustomSkillBuilder cancelOnServer(BiConsumer<SkillContainer, FriendlyByteBuf> consumer) {
         this.cancelOnServer = consumer;
         return this;
      }

      @Info("Called when the skill is cancelled on the client.\n")
      public CustomSkill.CustomSkillBuilder cancelOnClient(BiConsumer<SkillContainer, FriendlyByteBuf> consumer) {
         this.cancelOnClient = consumer;
         return this;
      }

      @Info("Called when resource consumption is being calculated.\n")
      public CustomSkill.CustomSkillBuilder setConsumption(BiConsumer<SkillContainer, Float> consumer) {
         this.setConsumption = consumer;
         return this;
      }

      @Info("Called each tick the skill is active.\n")
      public CustomSkill.CustomSkillBuilder updateContainer(Consumer<SkillContainer> consumer) {
         this.updateContainer = consumer;
         return this;
      }

      @Info("Called when the cooldown regeneration is being calculated.\n")
      public CustomSkill.CustomSkillBuilder cooldownRegenPerSecond(Function<PlayerPatch<?>, Float> consumer) {
         this.cooldownRegenPerSecond = consumer;
         return this;
      }

      @Info("Sets the max stack size of the skill.\n")
      public CustomSkill.CustomSkillBuilder maxStackSize(int maxStackSize) {
         this.maxStackSize = maxStackSize;
         return this;
      }

      @Info("Sets the max duration of the skill.\n")
      public CustomSkill.CustomSkillBuilder maxDuration(int maxDuration) {
         this.maxDuration = maxDuration;
         return this;
      }

      @Info("Predicate on whether the skill should deactivate automatically or not.\n")
      public CustomSkill.CustomSkillBuilder shouldDeactivateAutomatically(Predicate<PlayerPatch<?>> predicate) {
         this.shouldDeactivateAutomatically = predicate;
         return this;
      }

      @Info("Consumer that is called to draw the skill on the GUI.\n")
      public CustomSkill.CustomSkillBuilder drawOnGui(Consumer<CustomSkill.DrawOnGuiContext> consumer) {
         this.drawOnGui = consumer;
         return this;
      }

      @Info("Predicate that is called to check if the skill should be drawn on the GUI.\n")
      public CustomSkill.CustomSkillBuilder shouldDraw(Predicate<SkillContainer> predicate) {
         this.shouldDraw = predicate;
         return this;
      }

      @Info("Predicate that is called to check if the skill can be executed.\n")
      public CustomSkill.CustomSkillBuilder canExecute(Predicate<SkillContainer> predicate) {
         this.canExecute = predicate;
         return this;
      }

      @Info("Predicate that is called to check if the skill is in executable state.\n")
      public CustomSkill.CustomSkillBuilder executableState(Predicate<PlayerPatch<?>> predicate) {
         this.executableState = predicate;
         return this;
      }

      @Info("Consumer that is called when the skill is added from the skill HUD.\n")
      public CustomSkill.CustomSkillBuilder onScreen(Consumer<CustomSkill.OnScreenContext> consumer) {
         this.onScreen = consumer;
         return this;
      }

      @Info("Sets the tooltip of the skill on the item that has this skill as an innate.\n")
      public CustomSkill.CustomSkillBuilder getTooltipOnItem(Function<CustomSkill.GetTooltipOnItem, List<Component>> function) {
         this.getTooltipOnItem = function;
         return this;
      }

      @Info("Sets the parameters of the description of the skill on the skill book GUI.\n")
      public CustomSkill.CustomSkillBuilder getTooltipArgsOfScreen(Function<List<Object>, List<Object>> function) {
         this.getTooltipArgsOfScreen = function;
         return this;
      }

      public RegistryInfo<Skill> getRegistryType() {
         return EpicFightKubeJSPlugin.SKILL_REGISTRY;
      }

      public Skill createObject() {
         return new CustomSkill(this);
      }
   }

   public record DrawOnGuiContext(BattleModeGui getGui, SkillContainer getContainer, GuiGraphics getGuiGraphics, float getX, float getY) {
   }

   public record GetTooltipOnItem(ItemStack getItemStack, CapabilityItem getCap, PlayerPatch<?> getPlayerPatch) {
   }

   public record OnScreenContext(LocalPlayerPatch getLocalPlayerPatch, float getResolutionX, float getResolutionY) {
   }
}
