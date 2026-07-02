package yesman.epicfight.world.capabilities;

import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

public class EpicFightCapabilities {
   public static final Capability<EntityPatch> CAPABILITY_ENTITY = CapabilityManager.get(new CapabilityToken<EntityPatch>() {});
   public static final Capability<CapabilityItem> CAPABILITY_ITEM = CapabilityManager.get(new CapabilityToken<CapabilityItem>() {});
   public static final Capability<ProjectilePatch> CAPABILITY_PROJECTILE = CapabilityManager.get(new CapabilityToken<ProjectilePatch>() {});
   public static final Capability<CapabilitySkill> CAPABILITY_SKILL = CapabilityManager.get(new CapabilityToken<CapabilitySkill>() {});

   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.register(CapabilityItem.class);
      event.register(EntityPatch.class);
      event.register(ProjectilePatch.class);
      event.register(CapabilitySkill.class);
   }

   public static CapabilityItem getItemStackCapability(ItemStack stack) {
      return stack.m_41619_() ? CapabilityItem.EMPTY : (CapabilityItem)stack.getCapability(CAPABILITY_ITEM).orElse(CapabilityItem.EMPTY);
   }

   public static CapabilityItem getItemStackCapabilityOr(ItemStack stack, @Nullable CapabilityItem defaultCap) {
      return stack.m_41619_() ? defaultCap : (CapabilityItem)stack.getCapability(CAPABILITY_ITEM).orElse(defaultCap);
   }

   public static Optional<CapabilityItem> getItemCapability(ItemStack stack) {
      return stack.m_41619_() ? Optional.empty() : stack.getCapability(CAPABILITY_ITEM).resolve();
   }

   @Nullable
   public static <T extends EntityPatch> T getEntityPatch(@Nullable Entity entity, Class<T> type) {
      if (entity != null) {
         EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(CAPABILITY_ENTITY).orElse(null);
         if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
            return (T)entitypatch;
         }
      }

      return null;
   }

   @Nullable
   public static PlayerPatch getPlayerPatch(@Nullable Player player) {
      if (player != null) {
         EntityPatch<?> entitypatch = (EntityPatch<?>)player.getCapability(CAPABILITY_ENTITY).orElse(null);
         if (entitypatch != null && PlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
            return (PlayerPatch)entitypatch;
         }
      }

      return null;
   }

   @Nullable
   public static ServerPlayerPatch getServerPlayerPatch(@Nullable ServerPlayer serverPlayer) {
      if (serverPlayer != null) {
         EntityPatch<?> entitypatch = (EntityPatch<?>)serverPlayer.getCapability(CAPABILITY_ENTITY).orElse(null);
         if (entitypatch != null && ServerPlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
            return (ServerPlayerPatch)entitypatch;
         }
      }

      return null;
   }

   @Nullable
   public static LocalPlayerPatch getLocalPlayerPatch(@Nullable LocalPlayer localPlayer) {
      if (localPlayer != null) {
         EntityPatch<?> entitypatch = (EntityPatch<?>)localPlayer.getCapability(CAPABILITY_ENTITY).orElse(null);
         if (entitypatch != null && LocalPlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
            return (LocalPlayerPatch)entitypatch;
         }
      }

      return null;
   }

   public static <T extends EntityPatch<?>> Optional<T> getUnparameterizedEntityPatch(@Nullable Entity entity, Class<T> type) {
      if (entity != null) {
         EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(CAPABILITY_ENTITY).orElse(null);
         if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
            return Optional.of((T)entitypatch);
         }
      }

      return Optional.empty();
   }

   public static <E extends Entity, T extends EntityPatch<E>> Optional<T> getParameterizedEntityPatch(
      @Nullable Entity entity, Class<E> entitytype, Class<?> patchtype
   ) {
      if (entity != null && entitytype.isAssignableFrom(entity.getClass())) {
         EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(CAPABILITY_ENTITY).orElse(null);
         if (entitypatch != null && patchtype.isAssignableFrom(entitypatch.getClass())) {
            return Optional.of((T)entitypatch);
         }
      }

      return Optional.empty();
   }

   public static Optional<PlayerPatch<?>> getPlayerPatchAsOptional(@Nullable Entity entity) {
      if (entity == null) {
         return Optional.empty();
      }

      EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(CAPABILITY_ENTITY).orElse(null);
      return entitypatch instanceof PlayerPatch<?> playerpatch ? Optional.of(playerpatch) : Optional.empty();
   }

   public static Optional<ServerPlayerPatch> getServerPlayerPatchAsOptional(@Nullable Entity entity) {
      if (entity == null) {
         return Optional.empty();
      }

      EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(CAPABILITY_ENTITY).orElse(null);
      return entitypatch instanceof ServerPlayerPatch serverplayerpatch ? Optional.of(serverplayerpatch) : Optional.empty();
   }

   public static Optional<LocalPlayerPatch> getLocalPlayerPatchAsOptional(@Nullable Entity entity) {
      if (entity == null) {
         return Optional.empty();
      }

      EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(CAPABILITY_ENTITY).orElse(null);
      return entitypatch instanceof LocalPlayerPatch localplayerpatch ? Optional.of(localplayerpatch) : Optional.empty();
   }
}
