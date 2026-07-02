package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface FakeBlockRenderer {
   void render(Camera var1, PoseStack var2, MultiBufferSource var3, Level var4, BlockPos var5, float var6, float var7, float var8, float var9);
}
