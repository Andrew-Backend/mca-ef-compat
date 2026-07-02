package yesman.epicfight.world.entity.eventlistener;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraftforge.fml.LogicalSide;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerEventListener {
   private final Map<PlayerEventListener.EventType<? extends DetachablePlayerEvent<?>>, TreeMultimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>>> events;
   private final PlayerPatch<?> playerpatch;

   public PlayerEventListener(PlayerPatch<?> playerpatch) {
      this.playerpatch = playerpatch;
      this.events = Maps.newHashMap();
   }

   public <T extends DetachablePlayerEvent<?>> void addEventListener(PlayerEventListener.EventType<T> eventType, UUID uuid, Consumer<T> function) {
      this.addEventListener(eventType, uuid, function, -1);
   }

   public <T extends DetachablePlayerEvent<?>> void addEventListener(PlayerEventListener.EventType<T> eventType, UUID uuid, Consumer<T> function, int priority) {
      if (eventType.shouldActive(this.playerpatch.isLogicalClient())) {
         if (!this.events.containsKey(eventType)) {
            this.events.put(eventType, TreeMultimap.create());
         }

         priority = Math.max(priority, -1);
         this.removeListener(eventType, uuid, priority);
         TreeMultimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>> map = this.events.get(eventType);
         map.put(priority, EventTrigger.makeEvent(uuid, function, priority));
      }
   }

   public <T extends DetachablePlayerEvent<?>> void removeListener(PlayerEventListener.EventType<T> eventType, UUID uuid) {
      this.removeListener(eventType, uuid, -1);
   }

   public <T extends DetachablePlayerEvent<?>> void removeListener(PlayerEventListener.EventType<T> eventType, UUID uuid, int priority) {
      Multimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>> map = (Multimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>>)this.events
         .get(eventType);
      if (map != null) {
         priority = Math.max(priority, -1);
         map.get(priority).removeIf(trigger -> trigger.is(uuid));
      }
   }

   public <T extends DetachablePlayerEvent<?>> boolean triggerEvents(PlayerEventListener.EventType<T> eventType, T event) {
      boolean cancel = false;
      TreeMultimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>> map = this.events.get(eventType);
      if (map != null) {
         for (int i : map.keySet().descendingSet()) {
            if (!cancel || i == -1) {
               for (EventTrigger<?> eventTrigger : map.get(i)) {
                  if (eventType.shouldActive(this.playerpatch.isLogicalClient())) {
                     EventTrigger<T> castedTrigger = (EventTrigger<T>)eventTrigger;
                     castedTrigger.trigger(event);
                     cancel |= event.isCanceled();
                  }
               }
            }
         }
      }

      return cancel;
   }

   public static class EventType<T extends DetachablePlayerEvent<?>> {
      public static final PlayerEventListener.EventType<ActionEvent<LocalPlayerPatch>> ACTION_EVENT_CLIENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<ActionEvent<ServerPlayerPatch>> ACTION_EVENT_SERVER = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<ModifyAttackSpeedEvent> MODIFY_ATTACK_SPEED_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<ModifyBaseDamageEvent<PlayerPatch<?>>> MODIFY_DAMAGE_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<DealDamageEvent.Attack> DEAL_DAMAGE_EVENT_ATTACK = new PlayerEventListener.EventType<>(
         LogicalSide.SERVER
      );
      public static final PlayerEventListener.EventType<DealDamageEvent.Hurt> DEAL_DAMAGE_EVENT_HURT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<DealDamageEvent.Damage> DEAL_DAMAGE_EVENT_DAMAGE = new PlayerEventListener.EventType<>(
         LogicalSide.SERVER
      );
      public static final PlayerEventListener.EventType<TakeDamageEvent.Attack> TAKE_DAMAGE_EVENT_ATTACK = new PlayerEventListener.EventType<>(
         LogicalSide.SERVER
      );
      public static final PlayerEventListener.EventType<TakeDamageEvent.Hurt> TAKE_DAMAGE_EVENT_HURT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<TakeDamageEvent.Damage> TAKE_DAMAGE_EVENT_DAMAGE = new PlayerEventListener.EventType<>(
         LogicalSide.SERVER
      );
      public static final PlayerEventListener.EventType<AnimationBeginEvent> ANIMATION_BEGIN_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<AnimationEndEvent> ANIMATION_END_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<AttackEndEvent> ATTACK_ANIMATION_END_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<AttackPhaseEndEvent> ATTACK_PHASE_END_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<BasicAttackEvent> BASIC_ATTACK_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<MovementInputEvent> MOVEMENT_INPUT_EVENT = new PlayerEventListener.EventType<>(LogicalSide.CLIENT);
      public static final PlayerEventListener.EventType<RightClickItemEvent<LocalPlayerPatch>> CLIENT_ITEM_USE_EVENT = new PlayerEventListener.EventType<>(
         LogicalSide.CLIENT
      );
      public static final PlayerEventListener.EventType<RightClickItemEvent<ServerPlayerPatch>> SERVER_ITEM_USE_EVENT = new PlayerEventListener.EventType<>(
         LogicalSide.SERVER
      );
      public static final PlayerEventListener.EventType<ItemUseEndEvent> SERVER_ITEM_STOP_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<ProjectileHitEvent> PROJECTILE_HIT_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<SkillCastEvent> SKILL_CAST_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<SkillCancelEvent> SKILL_CANCEL_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<SkillConsumeEvent> SKILL_CONSUME_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<StaminaConsumeEvent> STAMINA_CONSUME_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<ComboCounterHandleEvent> COMBO_COUNTER_HANDLE_EVENT = new PlayerEventListener.EventType<>(
         LogicalSide.SERVER
      );
      public static final PlayerEventListener.EventType<TargetIndicatorCheckEvent> TARGET_INDICATOR_ALERT_CHECK_EVENT = new PlayerEventListener.EventType<>(
         LogicalSide.CLIENT
      );
      public static final PlayerEventListener.EventType<FallEvent> FALL_EVENT = new PlayerEventListener.EventType<>(null);
      public static final PlayerEventListener.EventType<SetTargetEvent> SET_TARGET_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<DodgeSuccessEvent> DODGE_SUCCESS_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<PlayerKilledEvent> PLAYER_KILLED_EVENT = new PlayerEventListener.EventType<>(LogicalSide.SERVER);
      public static final PlayerEventListener.EventType<UpdatePlayerMotionEvent.BaseLayer> UPDATE_BASE_LIVING_MOTION_EVENT = new PlayerEventListener.EventType<>(
         LogicalSide.CLIENT
      );
      public static final PlayerEventListener.EventType<UpdatePlayerMotionEvent.CompositeLayer> UPDATE_COMPOSITE_LIVING_MOTION_EVENT = new PlayerEventListener.EventType<>(
         LogicalSide.CLIENT
      );
      LogicalSide side;

      public EventType(LogicalSide side) {
         this.side = side;
      }

      public boolean shouldActive(boolean isRemote) {
         return this.side == null || this.side.isClient() == isRemote;
      }
   }
}
