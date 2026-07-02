package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = "epicfight", value = Dist.CLIENT, bus = Bus.MOD)
public class EpicFightShaders {
   public static ShaderInstance positionColorNormalShader;

   @Nullable
   public static ShaderInstance getPositionColorNormalShader() {
      return positionColorNormalShader;
   }

   @SubscribeEvent
   public static void registerShadersEvent(RegisterShadersEvent event) throws IOException {
      event.registerShader(
         new ShaderInstance(event.getResourceProvider(), EpicFightMod.identifier("solid_model"), DefaultVertexFormat.f_166851_),
         reloadedShader -> positionColorNormalShader = reloadedShader
      );
   }
}
