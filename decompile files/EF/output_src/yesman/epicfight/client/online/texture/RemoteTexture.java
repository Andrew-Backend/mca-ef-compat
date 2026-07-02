package yesman.epicfight.client.online.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class RemoteTexture extends SimpleTexture {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private final String urlString;
   @Nullable
   private CompletableFuture<?> future;

   public RemoteTexture(String pUrlString, ResourceLocation defaultLocation) {
      super(defaultLocation);
      this.urlString = pUrlString;
   }

   private void loadCallback(NativeImage pImage) {
      Minecraft.m_91087_().execute(() -> {
         if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.upload(pImage));
         } else {
            this.upload(pImage);
         }
      });
   }

   private void upload(NativeImage pImage) {
      TextureUtil.prepareImage(this.m_117963_(), pImage.m_84982_(), pImage.m_85084_());
      pImage.m_85040_(0, 0, 0, true);
   }

   public void m_6704_(ResourceManager pResourceManager) throws IOException {
      if (this.future == null) {
         this.future = CompletableFuture.runAsync(() -> {
            LOGGER.debug("Downloading http texture from {}", this.urlString);

            try {
               HttpURLConnection httpurlconnection = (HttpURLConnection)new URL(this.urlString).openConnection(Minecraft.m_91087_().m_91096_());
               httpurlconnection.setDoInput(true);
               httpurlconnection.setDoOutput(false);
               httpurlconnection.connect();
               if (httpurlconnection.getResponseCode() / 100 == 2) {
                  InputStream inputstream = httpurlconnection.getInputStream();
                  Minecraft.m_91087_().execute(() -> {
                     NativeImage nativeimage1 = this.load(inputstream);
                     if (nativeimage1 != null) {
                        this.loadCallback(nativeimage1);
                     }

                     if (httpurlconnection != null) {
                        httpurlconnection.disconnect();
                     }
                  });
               }
            } catch (Exception exception) {
               LOGGER.error("Couldn't download http texture", exception);
            }
         }, Util.m_183991_());
      }
   }

   @Nullable
   private NativeImage load(InputStream pStream) {
      NativeImage nativeimage = null;

      try {
         nativeimage = NativeImage.m_85058_(pStream);
      } catch (Exception exception) {
         LOGGER.warn("Error while loading the skin texture", exception);
      }

      return nativeimage;
   }
}
