package forge.net.mca.entity.ai.brain.tasks;

import com.google.gson.JsonSyntaxException;
import forge.net.mca.Config;
import forge.net.mca.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class WanderOrTeleportToTargetTask extends MoveToTargetSink {
   private static final int SLOWDOWN = 5;
   private int cooldown = 5;

   protected boolean m_6114_(ServerLevel serverWorld, Mob mobEntity) {
      if (this.cooldown < 0) {
         this.cooldown = 5;
         return super.m_6114_(serverWorld, mobEntity);
      } else {
         this.cooldown--;
         return false;
      }
   }

   protected void m_6725_(ServerLevel world, Mob entity, long l) {
      if (Config.getInstance().allowVillagerTeleporting) {
         entity.m_6274_().m_257414_(MemoryModuleType.f_26370_).ifPresent(walkTarget -> {
            BlockPos targetPos = walkTarget.m_26420_().m_6675_();
            if (!targetPos.m_203195_(entity.m_20182_(), Config.getInstance().villagerMinTeleportationDistance)) {
               this.tryTeleport(world, entity, targetPos);
            }
         });
      }

      super.m_6725_(world, entity, l);
   }

   private void tryTeleport(ServerLevel world, Mob entity, BlockPos targetPos) {
      for (int i = 0; i < 10; i++) {
         int j = this.getRandomInt(entity, -3, 3);
         int k = this.getRandomInt(entity, -1, 1);
         int l = this.getRandomInt(entity, -3, 3);
         boolean bl = this.tryTeleportTo(world, entity, targetPos, targetPos.m_123341_() + j, targetPos.m_123342_() + k, targetPos.m_123343_() + l);
         if (bl) {
            return;
         }
      }
   }

   private boolean tryTeleportTo(ServerLevel world, Mob entity, BlockPos targetPos, int x, int y, int z) {
      if (Math.abs((double)x - targetPos.m_123341_()) < 2.0 && Math.abs((double)z - targetPos.m_123343_()) < 2.0) {
         return false;
      }

      if (!this.canTeleportTo(world, entity, new BlockPos(x, y, z))) {
         return false;
      }

      entity.m_6021_(x + 0.5, y, z + 0.5);
      return true;
   }

   private boolean canTeleportTo(ServerLevel world, Mob entity, BlockPos pos) {
      BlockPathTypes pathNodeType = WalkNodeEvaluator.m_77604_(world, pos.m_122032_());
      if (pathNodeType != BlockPathTypes.WALKABLE) {
         return false;
      }

      if (!this.isAreaSafe(world, pos.m_7495_())) {
         return false;
      }

      BlockPos blockPos = pos.m_121996_(entity.m_20183_());
      return world.m_45756_(entity, entity.m_20191_().m_82338_(blockPos));
   }

   private int getRandomInt(Mob entity, int min, int max) {
      return entity.m_217043_().m_188503_(max - min + 1) + min;
   }

   private boolean isAreaSafe(ServerLevel world, BlockPos pos) {
      BlockState aboveState = world.m_8055_(pos);
      ResourceLocation aboveId = BuiltInRegistries.f_256975_.m_7981_(aboveState.m_60734_());

      for (String blockId : Config.getInstance().villagerPathfindingBlacklist) {
         if (blockId.equals(aboveId.toString())) {
            return false;
         }

         if (blockId.charAt(0) == '#') {
            ResourceLocation identifier = new ResourceLocation(blockId.substring(1));
            TagKey<Block> tag = TagKey.m_203882_(Registries.f_256747_, identifier);
            if (tag == null || RegistryHelper.isTagEmpty(tag)) {
               throw new JsonSyntaxException("Unknown block tag in villagerPathfindingBlacklist '" + identifier + "'");
            }

            if (aboveState.m_204336_(tag)) {
               return false;
            }
         }
      }

      return true;
   }
}
