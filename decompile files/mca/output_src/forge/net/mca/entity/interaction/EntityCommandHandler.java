package forge.net.mca.entity.interaction;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.network.s2c.OpenGuiRequest;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EntityCommandHandler<T extends Entity & VillagerLike<?>> {
   @Nullable
   protected Player interactingPlayer;
   protected final T entity;

   public EntityCommandHandler(T entity) {
      this.entity = entity;
   }

   public Optional<Player> getInteractingPlayer() {
      return Optional.ofNullable(this.interactingPlayer).filter(player -> player.f_36096_ != null);
   }

   public void stopInteracting() {
      if (!this.entity.m_9236_().f_46443_ && this.interactingPlayer instanceof ServerPlayer serverPlayer) {
         serverPlayer.m_6915_();
      }

      this.interactingPlayer = null;
   }

   public InteractionResult interactAt(Player player, Vec3 pos, @NotNull InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.INTERACT, this.entity), serverPlayer);
      }

      this.interactingPlayer = player;
      return InteractionResult.SUCCESS;
   }

   public boolean handle(ServerPlayer player, String command) {
      return false;
   }
}
