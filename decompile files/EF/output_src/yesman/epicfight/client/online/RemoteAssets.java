package yesman.epicfight.client.online;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.client.online.texture.RemoteTexture;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

public class RemoteAssets {
   private static final RemoteAssets INSTANCE = new RemoteAssets();
   private static final TextureManager TEXTURE_MANAGER = Minecraft.m_91087_().m_91097_();
   private final Map<Integer, RemoteAssets.RemoteMeshAccessor> cachedMeshes = Maps.newConcurrentMap();

   public static RemoteAssets getInstance() {
      return INSTANCE;
   }

   public synchronized AssetAccessor<Mesh> getRemoteMesh(int seq, String path, @Nullable Consumer<Mesh> callback) {
      if (this.cachedMeshes.containsKey(seq)) {
         RemoteAssets.RemoteMeshAccessor cachedMesh = this.cachedMeshes.get(seq);
         if (callback != null) {
            if (cachedMesh.get() == null) {
               cachedMesh.addWork(callback);
            } else {
               callback.accept(cachedMesh.get());
            }
         }

         return cachedMesh;
      } else {
         RemoteAssets.RemoteMeshAccessor remoteMeshAccessor = new RemoteAssets.RemoteMeshAccessor();
         remoteMeshAccessor.addWork(callback);
         this.cachedMeshes.put(seq, remoteMeshAccessor);
         CompletableFuture.runAsync(
            () -> EpicFightServerConnectionHelper.loadRemoteMesh(EpicFightSharedConstants.webServerDomain(), path, (mesh, exception) -> {
               if (exception != null) {
                  EpicFightMod.LOGGER.error("Failed at loading remote mesh " + seq + ": " + exception.getMessage());
                  exception.printStackTrace();
               } else {
                  remoteMeshAccessor.load(mesh);
               }
            })
         );
         return this.cachedMeshes.get(seq);
      }
   }

   public synchronized ResourceLocation getRemoteTexture(String fileName) {
      ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath("epicskins", "textures/remote/" + fileName);
      AbstractTexture texture = TEXTURE_MANAGER.m_174786_(textureLocation, MissingTextureAtlasSprite.m_118080_());
      if (texture == MissingTextureAtlasSprite.m_118080_()) {
         AbstractTexture httptexture = new RemoteTexture(
            EpicFightSharedConstants.webServerDomain() + "/textures/" + fileName, MissingTextureAtlasSprite.m_118071_()
         );
         TEXTURE_MANAGER.m_118495_(textureLocation, httptexture);
      }

      return textureLocation;
   }

   private static class RemoteMeshAccessor implements AssetAccessor<Mesh> {
      private Queue<Consumer<Mesh>> callback = Queues.newArrayDeque();
      private Mesh mesh;

      public void addWork(Consumer<Mesh> callback) {
         this.callback.add(callback);
      }

      public void load(Mesh mesh) {
         this.mesh = mesh;
         Minecraft.m_91087_().execute(() -> {
            this.callback.forEach(callback -> callback.accept(mesh));
            this.callback.clear();
            this.callback = null;
         });
      }

      public Mesh get() {
         return this.mesh;
      }

      @Override
      public ResourceLocation registryName() {
         return null;
      }

      @Override
      public boolean isPresent() {
         return this.mesh != null;
      }

      @Override
      public boolean inRegistry() {
         return false;
      }
   }
}
