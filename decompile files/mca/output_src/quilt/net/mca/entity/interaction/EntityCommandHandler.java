package quilt.net.mca.entity.interaction;

import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.network.s2c.OpenGuiRequest;

public abstract class EntityCommandHandler<T extends class_1297 & VillagerLike<?>> {
   @Nullable
   protected class_1657 interactingPlayer;
   protected final T entity;

   public EntityCommandHandler(T entity) {
      this.entity = entity;
   }

   public Optional<class_1657> getInteractingPlayer() {
      return Optional.ofNullable(this.interactingPlayer).filter(player -> player.field_7512 != null);
   }

   public void stopInteracting() {
      if (!this.entity.method_37908().field_9236 && this.interactingPlayer instanceof class_3222 serverPlayer) {
         serverPlayer.method_7346();
      }

      this.interactingPlayer = null;
   }

   public class_1269 interactAt(class_1657 player, class_243 pos, @NotNull class_1268 hand) {
      if (player instanceof class_3222 serverPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.INTERACT, this.entity), serverPlayer);
      }

      this.interactingPlayer = player;
      return class_1269.field_5812;
   }

   public boolean handle(class_3222 player, String command) {
      return false;
   }
}
