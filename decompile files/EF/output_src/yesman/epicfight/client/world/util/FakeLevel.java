package yesman.epicfight.client.world.util;

import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.main.EpicFightMod;

public class FakeLevel extends ClientLevel {
   private static FakeLevel instance;
   private static final Map<GameProfile, FakeLevel.FakeClientPlayer> FAKE_PLAYERS = new HashMap<>();
   private final ClientLevel refLevel;
   private boolean appliedGetBiomeManagerWorkaround;

   public static FakeLevel getFakeLevel(ClientLevel refLevel) {
      if (instance == null || instance.m_9598_() != refLevel) {
         instance = new FakeLevel(refLevel, Minecraft.m_91087_());
      }

      return instance;
   }

   public static void unloadFakeLevel() {
      instance = null;
   }

   public static FakeLevel.FakeClientPlayer getFakePlayer(GameProfile playerprofile) {
      return FAKE_PLAYERS.computeIfAbsent(playerprofile, key -> new FakeLevel.FakeClientPlayer(instance, key));
   }

   public FakeLevel(ClientLevel refLevel, Minecraft minecraft) {
      super(
         refLevel.f_104561_,
         new ClientLevelData(Difficulty.NORMAL, false, false),
         Level.f_46428_,
         refLevel.m_9598_().m_175515_(Registries.f_256787_).m_246971_(BuiltinDimensionTypes.f_223538_),
         0,
         0,
         minecraft::m_91307_,
         minecraft.f_91060_,
         true,
         0L
      );
      this.refLevel = refLevel;
   }

   public void m_7462_() {
   }

   public void m_5503_(Packet<?> packet) {
   }

   @Nullable
   public ChunkAccess m_6522_(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
      return this.refLevel.m_6522_(pX, pZ, pRequiredStatus, pNonnull);
   }

   public boolean m_7232_(int pChunkX, int pChunkZ) {
      return this.refLevel.m_7232_(pChunkX, pChunkZ);
   }

   public int m_6924_(Types pHeightmapType, int pX, int pZ) {
      return this.refLevel.m_6924_(pHeightmapType, pX, pZ);
   }

   public int m_7445_() {
      return this.refLevel.m_7445_();
   }

   @NotNull
   public BiomeManager m_7062_() {
      ClientLevel level = this.refLevel;
      if (level == null) {
         if (!this.appliedGetBiomeManagerWorkaround) {
            EpicFightMod.LOGGER
               .warn(
                  "FakeLevel.refLevel is null, so Epic Fight can't override getBiomeManager().\nThis issue may happens when some mods are installed, such as BadOptimizations.\nFor more technical details, refer to: https://github.com/imthosea/BadOptimizations/issues/108"
               );
         }

         this.appliedGetBiomeManagerWorkaround = true;
         return super.m_7062_();
      } else {
         return level.m_7062_();
      }
   }

   public boolean m_5776_() {
      return true;
   }

   @Nullable
   public BlockEntity m_7702_(BlockPos pPos) {
      return this.refLevel.m_7702_(pPos);
   }

   public BlockState m_8055_(BlockPos bPos) {
      return this.refLevel.m_8055_(bPos);
   }

   public FluidState m_6425_(BlockPos pPos) {
      return this.refLevel.m_6425_(pPos);
   }

   public List<Entity> m_6249_(@Nullable Entity pEntity, AABB pArea, Predicate<? super Entity> pPredicate) {
      return this.refLevel.m_6249_(pEntity, pArea, pPredicate);
   }

   public <T extends Entity> List<T> m_142425_(EntityTypeTest<Entity, T> pEntityTypeTest, AABB pBounds, Predicate<? super T> pPredicate) {
      return this.refLevel.m_142425_(pEntityTypeTest, pBounds, pPredicate);
   }

   public List<AbstractClientPlayer> m_6907_() {
      return this.refLevel.m_6907_();
   }

   public boolean m_6933_(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
      return false;
   }

   public boolean m_7471_(BlockPos pPos, boolean pIsMoving) {
      return false;
   }

   public boolean m_7740_(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity, int pRecursionLeft) {
      return false;
   }

   public static class FakeClientPlayer extends AbstractClientPlayer {
      public FakeClientPlayer(FakeLevel fakeLevel, GameProfile gameProfile) {
         super(fakeLevel, gameProfile);
      }

      public boolean m_7578_() {
         return true;
      }
   }
}
