package yesman.epicfight.client.gui.datapack.widgets;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.JointMask;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.datapack.screen.DatapackEditScreen;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;
import yesman.epicfight.client.gui.datapack.screen.SelectAnimationScreen;
import yesman.epicfight.client.gui.datapack.screen.SelectFromRegistryScreen;
import yesman.epicfight.client.gui.datapack.screen.SelectModelScreen;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;

public abstract class PopupBox<T> extends AbstractWidget implements DataBindingComponent<T, Pair<String, T>> {
   public static final ResourceLocation POPUP_ICON = EpicFightMod.identifier("textures/gui/widget/popup_icon.png");
   protected final Screen owner;
   protected final Font font;
   protected final Function<T, String> toDisplayString;
   protected T item;
   protected String itemDisplayName;
   protected Predicate<T> filter;
   protected Consumer<Pair<String, T>> responder;
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public PopupBox(
      Screen owner,
      Font font,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      Component title,
      Function<T, String> displayStringMapper,
      Consumer<Pair<String, T>> responder
   ) {
      super(x1, y1, x2, y2, title);
      this.owner = owner;
      this.font = font;
      this.responder = responder;
      this.toDisplayString = displayStringMapper;
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.horizontalSizingOption = horizontal;
      this.verticalSizingOption = vertical;
   }

   public Predicate<T> getFilter() {
      return this.filter == null ? item -> true : this.filter;
   }

   public PopupBox<T> applyFilter(Predicate<T> filter) {
      this.filter = filter;
      return this;
   }

   protected boolean m_93680_(double x, double y) {
      return this.f_93623_ && this.f_93624_ && x >= this._getX() && y >= this._getY() && x < this._getX() + this.f_93618_ && y < this._getY() + this.f_93619_;
   }

   protected boolean clickedPopupButton(double x, double y) {
      return this.f_93623_
         && this.f_93624_
         && x >= (double)this._getX() + this.f_93618_ - 14.0
         && y >= this._getY()
         && x < this._getX() + this.f_93618_
         && y < this._getY() + this.f_93619_;
   }

   public abstract void m_5716_(double var1, double var3);

   public boolean m_6375_(double x, double y, int button) {
      if (!this.f_93623_ || !this.f_93624_) {
         return false;
      } else if (this.m_7972_(button) && this.m_93680_(x, y)) {
         this.m_7435_(Minecraft.m_91087_().m_91106_());
         this.m_5716_(x, y);
         return true;
      } else {
         return false;
      }
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int outlineColor = this.m_93696_() ? -1 : (this.m_142518_() ? -6250336 : -12566463);
      guiGraphics.m_280509_(this._getX() - 1, this._getY() - 1, this._getX() + this.f_93618_ + 1, this._getY() + this.f_93619_ + 1, outlineColor);
      guiGraphics.m_280509_(this._getX(), this._getY(), this._getX() + this.f_93618_, this._getY() + this.f_93619_, -16777216);
      String correctedString = StringUtil.m_14408_(this.itemDisplayName) ? "" : this.font.m_92834_(this.itemDisplayName, this.f_93618_ - 16);
      guiGraphics.m_280056_(this.font, correctedString, this._getX() + 4, this._getY() + this.f_93619_ / 2 - 9 / 2 + 1, 16777215, false);
      RenderSystem.enableBlend();
      if (!this.m_142518_()) {
         RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 1.0F);
      }

      this.m_280322_(
         guiGraphics,
         POPUP_ICON,
         this._getX() + this.f_93618_ - this.f_93619_,
         this._getY(),
         0,
         0,
         0,
         this.f_93619_,
         this.f_93619_,
         this.f_93619_,
         this.f_93619_
      );
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   protected MutableComponent m_5646_() {
      Component component = this._getMessage();
      return Component.m_237110_("gui.epicfight.narrate.popbupBox", new Object[]{component});
   }

   protected void m_168797_(NarrationElementOutput narrationElementInput) {
      narrationElementInput.m_169146_(NarratedElementType.TITLE, this.m_5646_());
   }

   @Override
   public void setX1(int x1) {
      this.x1 = x1;
   }

   @Override
   public void setX2(int x2) {
      this.x2 = x2;
   }

   @Override
   public void setY1(int y1) {
      this.y1 = y1;
   }

   @Override
   public void setY2(int y2) {
      this.y2 = y2;
   }

   @Override
   public int getX1() {
      return this.x1;
   }

   @Override
   public int getX2() {
      return this.x2;
   }

   @Override
   public int getY1() {
      return this.y1;
   }

   @Override
   public int getY2() {
      return this.y2;
   }

   @Override
   public ResizableComponent.HorizontalSizing getHorizontalSizingOption() {
      return this.horizontalSizingOption;
   }

   @Override
   public ResizableComponent.VerticalSizing getVerticalSizingOption() {
      return this.verticalSizingOption;
   }

   @Override
   public void _setActive(boolean active) {
      this.f_93623_ = active;
   }

   @Override
   public void _setResponder(Consumer<Pair<String, T>> responder) {
      this.responder = responder;
   }

   @Override
   public Consumer<Pair<String, T>> _getResponder() {
      return this.responder;
   }

   @Override
   public void _setValue(@Nullable T item) {
      this.item = item;
      this.itemDisplayName = this.toDisplayString.apply(item);
      if (this.responder != null) {
         this.responder.accept(Pair.of(this.itemDisplayName, item));
      }

      if (!StringUtil.m_14408_(this.itemDisplayName) && !this.itemDisplayName.equals(this.font.m_92834_(this.itemDisplayName, this.f_93618_ - 16))) {
         this.m_257544_(Tooltip.m_257550_(Component.m_237113_(this.itemDisplayName)));
      } else {
         this.m_257544_(null);
      }
   }

   public void setDisplayText(String displayName) {
      this.itemDisplayName = displayName;
   }

   @Override
   public T _getValue() {
      return this.item;
   }

   @Override
   public void reset() {
      this.item = null;
      this.itemDisplayName = "";
   }

   @Override
   public void _tick() {
   }

   @Override
   public int _getX() {
      return this.m_252754_();
   }

   @Override
   public int _getY() {
      return this.m_252907_();
   }

   @Override
   public int _getWidth() {
      return this.m_5711_();
   }

   @Override
   public int _getHeight() {
      return this.m_93694_();
   }

   @Override
   public void _setX(int x) {
      this.m_252865_(x);
   }

   @Override
   public void _setY(int y) {
      this.m_253211_(y);
   }

   @Override
   public void _setWidth(int width) {
      this.m_93674_(width);
   }

   @Override
   public void _setHeight(int height) {
      this.setHeight(height);
   }

   @Override
   public Component _getMessage() {
      return this.m_6035_();
   }

   @Override
   public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_87963_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public static class AnimationPopupBox extends PopupBox<AssetAccessor<? extends StaticAnimation>> {
      private AssetAccessor<? extends Armature> armature;
      private AssetAccessor<? extends SkinnedMesh> mesh;

      public AnimationPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, AssetAccessor<? extends StaticAnimation>>> responder
      ) {
         super(
            owner,
            font,
            x1,
            x2,
            y1,
            y2,
            horizontal,
            vertical,
            title,
            animation -> ParseUtil.nullOrToString(animation, a -> a.registryName().toString()),
            responder
         );
      }

      public void setModel(AssetAccessor<? extends Armature> armature, AssetAccessor<? extends SkinnedMesh> mesh) {
         this.armature = armature;
         this.mesh = mesh;
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            if (this.armature != null && this.armature.get() != null && this.mesh != null && this.mesh.get() != null) {
               this.owner
                  .getMinecraft()
                  .m_91152_(new SelectAnimationScreen(this.owner, this::_setValue, this::_setValue, this.getFilter(), this.armature, this.mesh));
            } else {
               this.owner
                  .getMinecraft()
                  .m_91152_(
                     new MessageScreen("", "Define model and armature first.", this.owner, button2 -> this.owner.getMinecraft().m_91152_(this.owner), 180, 60)
                  );
            }
         }
      }
   }

   public static class ArmaturePopupBox extends PopupBox<AssetAccessor<Armature>> {
      public ArmaturePopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, AssetAccessor<Armature>>> responder
      ) {
         super(
            owner,
            font,
            x1,
            x2,
            y1,
            y2,
            horizontal,
            vertical,
            title,
            accessor -> ParseUtil.nullOrToString(accessor, accessor$2 -> accessor$2.registryName().toString()),
            responder
         );
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            Set<Pair<ResourceLocation, AssetAccessor<Armature>>> entries = Armatures.entry();
            DatapackEditScreen.getCurrentScreen().getUserArmatures().entrySet().forEach(entry -> entries.add(Pair.of(entry.getKey(), entry.getValue())));
            this.owner.getMinecraft().m_91152_(new SelectFromRegistryScreen<>(this.owner, entries, "Armature", (name, item) -> {
               this._setValue(item);
               this.setDisplayText(name);
            }, (name, item) -> {
               this._setValue(item);
               this.setDisplayText(name);
            }, c -> {}, this.getFilter()));
         }
      }
   }

   public static class BuiltinMobpatchPopupBox extends PopupBox<EntityType<?>> {
      public BuiltinMobpatchPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, EntityType<?>>> responder
      ) {
         super(owner, font, x1, x2, y1, y2, horizontal, vertical, title, entityType -> ParseUtil.nullParam(EntityType.m_20613_(entityType)), responder);
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            Set<Pair<ResourceLocation, EntityType<?>>> set = Sets.newHashSet();
            EntityPatchProvider.getPatchedEntities().forEach(entityType -> set.add(Pair.of(EntityType.m_20613_(entityType), entityType)));
            this.owner
               .getMinecraft()
               .m_91152_(
                  new SelectFromRegistryScreen<>(
                     this.owner, set, "Preset", (name, item) -> this._setValue(item), (name, item) -> this._setValue(item), c -> {}, this.getFilter()
                  )
               );
         }
      }
   }

   public static class ColliderPopupBox extends PopupBox<Collider> {
      public ColliderPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, Collider>> responder
      ) {
         super(
            owner,
            font,
            x1,
            x2,
            y1,
            y2,
            horizontal,
            vertical,
            title,
            collider -> ParseUtil.nullOrToString(collider, c -> ParseUtil.nullParam(ColliderPreset.getKey(c))),
            responder
         );
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            this.owner
               .getMinecraft()
               .m_91152_(
                  new SelectFromRegistryScreen<>(
                     this.owner,
                     ParseUtil.mapEntryToPair(ColliderPreset.entries()),
                     "Collider",
                     (name, item) -> this._setValue(item),
                     (name, item) -> this._setValue(item),
                     c -> {},
                     this.getFilter()
                  )
               );
         }
      }
   }

   public static class JointMaskPopupBox extends PopupBox<JointMask.JointMaskSet> {
      public JointMaskPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, JointMask.JointMaskSet>> responder
      ) {
         super(owner, font, x1, x2, y1, y2, horizontal, vertical, title, jointMask -> ParseUtil.nullParam(JointMaskReloadListener.getKey(jointMask)), responder);
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            this.owner
               .getMinecraft()
               .m_91152_(
                  new SelectFromRegistryScreen<>(
                     this.owner,
                     ParseUtil.mapEntryToPair(JointMaskReloadListener.entries()),
                     "Joint Mask",
                     (name, item) -> this._setValue(item),
                     (name, item) -> this._setValue(item),
                     c -> {},
                     this.getFilter()
                  )
               );
         }
      }
   }

   public static class MeshPopupBox extends PopupBox<AssetAccessor<? extends SkinnedMesh>> {
      public MeshPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, AssetAccessor<? extends SkinnedMesh>>> responder
      ) {
         super(
            owner,
            font,
            x1,
            x2,
            y1,
            y2,
            horizontal,
            vertical,
            title,
            mesh -> ParseUtil.nullOrToString(mesh, accessor -> ParseUtil.nullOrToString(accessor, accessor$2 -> accessor$2.registryName().toString())),
            responder
         );
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            this.owner.getMinecraft().m_91152_(new SelectModelScreen(this.owner, (name, item) -> {
               this._setValue(item);
               this.setDisplayText(name);
            }, (name, item) -> {
               this._setValue(item);
               this.setDisplayText(name);
            }));
         }
      }
   }

   @FunctionalInterface
   public interface PopupBoxProvider<T, P extends PopupBox<T>> {
      P create(
         Screen var1,
         Font var2,
         int var3,
         int var4,
         int var5,
         int var6,
         ResizableComponent.HorizontalSizing var7,
         ResizableComponent.VerticalSizing var8,
         Component var9,
         Consumer<Pair<String, T>> var10
      );
   }

   public static class RegistryPopupBox<T> extends PopupBox<T> {
      protected final IForgeRegistry<T> registry;
      protected final Consumer<T> onPressRow;

      public RegistryPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         IForgeRegistry<T> registry,
         Consumer<Pair<String, T>> responder
      ) {
         this(owner, font, x1, x2, y1, y2, horizontal, vertical, title, registry, item -> {}, responder);
      }

      public RegistryPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         IForgeRegistry<T> registry,
         Consumer<T> onPressRow,
         Consumer<Pair<String, T>> responder
      ) {
         super(
            owner,
            font,
            x1,
            x2,
            y1,
            y2,
            horizontal,
            vertical,
            title,
            item -> registry.containsValue(item) ? registry.getKey(item).toString() : ParseUtil.nullParam(item),
            responder
         );
         this.registry = registry;
         this.onPressRow = onPressRow;
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            this.owner
               .getMinecraft()
               .m_91152_(
                  new SelectFromRegistryScreen<>(
                     this.owner,
                     this.registry,
                     (name, item) -> this._setValue((T)item),
                     (name, item) -> this._setValue((T)item),
                     this.onPressRow,
                     this.getFilter()
                  )
               );
         }
      }
   }

   public static class RendererPopupBox extends PopupBox<ResourceLocation> {
      public RendererPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, ResourceLocation>> responder
      ) {
         super(owner, font, x1, x2, y1, y2, horizontal, vertical, title, entityType -> ParseUtil.nullOrToString(entityType, rl -> rl.toString()), responder);
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            Set<Pair<ResourceLocation, ResourceLocation>> set = Sets.newHashSet();
            ClientEngine.getInstance().renderEngine.getRendererEntries().forEach(rl -> set.add(Pair.of(rl, rl)));
            this.owner
               .getMinecraft()
               .m_91152_(
                  new SelectFromRegistryScreen<>(
                     this.owner, set, "Renderer", (name, item) -> this._setValue(item), (name, item) -> this._setValue(item), c -> {}, this.getFilter()
                  )
               );
         }
      }
   }

   public static class SoundPopupBox extends PopupBox.RegistryPopupBox<SoundEvent> {
      public SoundPopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, SoundEvent>> responder
      ) {
         super(
            owner,
            font,
            x1,
            x2,
            y1,
            y2,
            horizontal,
            vertical,
            title,
            ForgeRegistries.SOUND_EVENTS,
            soundevent -> Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_119752_(soundevent, 1.0F)),
            responder
         );
      }
   }

   public static class WeaponTypePopupBox extends PopupBox<Function<Item, CapabilityItem.Builder>> {
      public WeaponTypePopupBox(
         Screen owner,
         Font font,
         int x1,
         int x2,
         int y1,
         int y2,
         ResizableComponent.HorizontalSizing horizontal,
         ResizableComponent.VerticalSizing vertical,
         Component title,
         Consumer<Pair<String, Function<Item, CapabilityItem.Builder>>> responder
      ) {
         super(owner, font, x1, x2, y1, y2, horizontal, vertical, title, builder -> {
            Map<Function<Item, CapabilityItem.Builder>, ResourceLocation> map = Maps.newHashMap();
            WeaponTypeReloadListener.entries().forEach(entry -> map.put(entry.getValue(), entry.getKey()));
            DatapackEditScreen.getSerializableWeaponTypes().forEach(entry -> map.put(entry.getValue(), entry.getKey()));
            return ParseUtil.nullParam(map.get(builder));
         }, responder);
      }

      @Override
      public void m_5716_(double x, double y) {
         if (this.clickedPopupButton(x, y)) {
            Set<Entry<ResourceLocation, Function<Item, CapabilityItem.Builder>>> weaponTypeEntry = Sets.newHashSet();
            weaponTypeEntry.addAll(WeaponTypeReloadListener.entries());
            weaponTypeEntry.addAll(DatapackEditScreen.getSerializableWeaponTypes());
            this.owner
               .getMinecraft()
               .m_91152_(new SelectFromRegistryScreen<>(this.owner, ParseUtil.mapEntryToPair(weaponTypeEntry), "Weapon Type", (name, item) -> {
                  Consumer<Pair<String, Function<Item, CapabilityItem.Builder>>> responder = this.responder;
                  this._setResponder(null);
                  this._setValue(item);
                  responder.accept(Pair.of(name, item));
                  this._setResponder(responder);
                  this.setDisplayText(name);
               }, (name, item) -> {
                  Consumer<Pair<String, Function<Item, CapabilityItem.Builder>>> responder = this.responder;
                  this._setResponder(null);
                  this._setValue(item);
                  responder.accept(Pair.of(name, item));
                  this._setResponder(responder);
                  this.setDisplayText(name);
               }, c -> {}, this.getFilter()));
         }
      }
   }
}
