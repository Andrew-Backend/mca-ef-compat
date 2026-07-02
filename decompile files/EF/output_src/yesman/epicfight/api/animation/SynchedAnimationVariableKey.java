package yesman.epicfight.api.animation;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.IForgeRegistry.BakeCallback;
import net.minecraftforge.registries.IForgeRegistry.ClearCallback;
import net.minecraftforge.registries.IForgeRegistry.CreateCallback;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.api.utils.datastruct.ClearableIdMapper;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPAnimationVariablePacket;
import yesman.epicfight.network.common.AnimationVariablePacket;
import yesman.epicfight.network.server.SPAnimationVariablePacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface SynchedAnimationVariableKey<T> {
   ResourceLocation BY_ID_REGISTRY = EpicFightMod.identifier("variablekeytoid");

   static <T> SynchedAnimationVariableKey.SynchedSharedAnimationVariableKey<T> shared(
      Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> codec
   ) {
      return new SynchedAnimationVariableKey.SynchedSharedAnimationVariableKey<>(defaultValueSupplier, mutable, codec);
   }

   static <T> SynchedAnimationVariableKey.SynchedIndependentAnimationVariableKey<T> independent(
      Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> codec
   ) {
      return new SynchedAnimationVariableKey.SynchedIndependentAnimationVariableKey<>(defaultValueSupplier, mutable, codec);
   }

   static SynchedAnimationVariableKey.SynchedAnimationVariableKeyCallbacks getRegistryCallback() {
      return SynchedAnimationVariableKey.SynchedAnimationVariableKeyCallbacks.INSTANCE;
   }

   static IdMapper<SynchedAnimationVariableKey<?>> getIdMap() {
      return (IdMapper<SynchedAnimationVariableKey<?>>)SynchedAnimationVariableKeys.REGISTRY.get().getSlaveMap(BY_ID_REGISTRY, IdMapper.class);
   }

   static <T> SynchedAnimationVariableKey<T> byId(int id) {
      return (SynchedAnimationVariableKey<T>)getIdMap().m_7942_(id);
   }

   PacketBufferCodec<T> getPacketBufferCodec();

   boolean isSharedKey();

   default int getId() {
      return getIdMap().m_7447_(this);
   }

   default void sync(
      LivingEntityPatch<?> entitypatch, @Nullable AssetAccessor<? extends StaticAnimation> animation, T value, AnimationVariablePacket.Action action
   ) {
      if (entitypatch.isLogicalClient()) {
         EpicFightNetworkManager.sendToServer(new CPAnimationVariablePacket<>(this, animation, value, action));
      } else {
         entitypatch.sendToAllPlayersTrackingMe(new SPAnimationVariablePacket<>(entitypatch, this, animation, value, action));
      }
   }

   class SynchedAnimationVariableKeyCallbacks
      implements BakeCallback<SynchedAnimationVariableKey<?>>,
      CreateCallback<SynchedAnimationVariableKey<?>>,
      ClearCallback<SynchedAnimationVariableKey<?>> {
      private static final SynchedAnimationVariableKey.SynchedAnimationVariableKeyCallbacks INSTANCE = new SynchedAnimationVariableKey.SynchedAnimationVariableKeyCallbacks();

      public void onBake(IForgeRegistryInternal<SynchedAnimationVariableKey<?>> owner, RegistryManager stage) {
         ClearableIdMapper<SynchedAnimationVariableKey<?>> synchedanimationvariablekeybyid = (ClearableIdMapper<SynchedAnimationVariableKey<?>>)owner.getSlaveMap(
            SynchedAnimationVariableKey.BY_ID_REGISTRY, ClearableIdMapper.class
         );
         owner.forEach(synchedanimationvariablekeybyid::m_122667_);
      }

      public void onCreate(IForgeRegistryInternal<SynchedAnimationVariableKey<?>> owner, RegistryManager stage) {
         owner.setSlaveMap(SynchedAnimationVariableKey.BY_ID_REGISTRY, new ClearableIdMapper(owner.getKeys().size()));
      }

      public void onClear(IForgeRegistryInternal<SynchedAnimationVariableKey<?>> owner, RegistryManager stage) {
         ((ClearableIdMapper)owner.getSlaveMap(SynchedAnimationVariableKey.BY_ID_REGISTRY, ClearableIdMapper.class)).clear();
      }
   }

   class SynchedIndependentAnimationVariableKey<T> extends AnimationVariables.IndependentAnimationVariableKey<T> implements SynchedAnimationVariableKey<T> {
      private final PacketBufferCodec<T> packetBufferCodec;

      protected SynchedIndependentAnimationVariableKey(Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> packetBufferCodec) {
         super(defaultValueSupplier, mutable);
         this.packetBufferCodec = packetBufferCodec;
      }

      @Override
      public boolean isSharedKey() {
         return false;
      }

      @Override
      public PacketBufferCodec<T> getPacketBufferCodec() {
         return this.packetBufferCodec;
      }
   }

   class SynchedSharedAnimationVariableKey<T> extends AnimationVariables.SharedAnimationVariableKey<T> implements SynchedAnimationVariableKey<T> {
      private final PacketBufferCodec<T> packetBufferCodec;

      protected SynchedSharedAnimationVariableKey(Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> packetBufferCodec) {
         super(defaultValueSupplier, mutable);
         this.packetBufferCodec = packetBufferCodec;
      }

      @Override
      public boolean isSynched() {
         return true;
      }

      @Override
      public PacketBufferCodec<T> getPacketBufferCodec() {
         return this.packetBufferCodec;
      }
   }
}
