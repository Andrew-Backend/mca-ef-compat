package yesman.epicfight.gameasset;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Collections;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.main.EpicFightMod;

public class ColliderPreset implements PreparableReloadListener {
   private static final BiMap<ResourceLocation, Collider> PRESETS = HashBiMap.create();
   public static final Collider DAGGER = registerCollider(EpicFightMod.identifier("dagger"), new MultiOBBCollider(3, 0.4, 0.4, 0.6, 0.0, 0.0, -0.1));
   public static final Collider DUAL_DAGGER_DASH = registerCollider(EpicFightMod.identifier("dual_dagger_dash"), new OBBCollider(0.8, 0.5, 1.0, 0.0, 1.0, -0.6));
   public static final Collider BIPED_BODY_COLLIDER = registerCollider(
      EpicFightMod.identifier("biped_body_collider"),
      new MultiOBBCollider(new OBBCollider(0.8, 0.5, 1.0, 0.0, 1.0, -0.6), new OBBCollider(0.8, 0.5, 1.0, 0.0, 1.0, -0.6))
   );
   public static final Collider DRAGON_BODY = registerCollider(EpicFightMod.identifier("dragon_body"), new OBBCollider(2.0, 1.5, 4.0, 0.0, 1.5, -0.5));
   public static final Collider DRAGON_LEG = registerCollider(EpicFightMod.identifier("dragon_leg"), new MultiOBBCollider(3, 0.8, 1.6, 0.8, 0.0, -0.6, 0.7));
   public static final Collider DUAL_SWORD = registerCollider(EpicFightMod.identifier("dual_sword"), new OBBCollider(0.8, 0.5, 1.0, 0.0, 0.5, -1.0));
   public static final Collider DUAL_SWORD_DASH = registerCollider(EpicFightMod.identifier("dual_sword_dash"), new OBBCollider(0.8, 0.5, 1.0, 0.0, 1.0, -1.0));
   public static final Collider BATTOJUTSU = registerCollider(EpicFightMod.identifier("battojutsu"), new OBBCollider(3.0, 0.4, 1.5, 0.0, 1.2, -1.0));
   public static final Collider BATTOJUTSU_DASH = registerCollider(
      EpicFightMod.identifier("battojutsu_dash"),
      new MultiOBBCollider(
         new OBBCollider(0.7, 0.7, 1.0, 0.0, 1.0, -1.0),
         new OBBCollider(0.7, 0.7, 1.0, 0.0, 1.0, -1.0),
         new OBBCollider(0.7, 0.7, 1.0, 0.0, 1.0, -1.0),
         new OBBCollider(0.7, 0.7, 1.0, 0.0, 1.0, -1.0),
         new OBBCollider(1.5, 0.7, 1.0, 0.0, 1.0, -1.0)
      )
   );
   public static final Collider FIST = registerCollider(EpicFightMod.identifier("fist"), new MultiOBBCollider(3, 0.4, 0.4, 0.4, 0.0, 0.0, 0.0));
   public static final Collider GREATSWORD = registerCollider(EpicFightMod.identifier("greatsword"), new MultiOBBCollider(3, 0.5, 0.8, 1.0, 0.0, 0.0, -1.0));
   public static final Collider HEAD = registerCollider(EpicFightMod.identifier("head"), new OBBCollider(0.4, 0.4, 0.4, 0.0, 0.0, -0.3));
   public static final Collider HEADBUTT_RAVAGER = registerCollider(EpicFightMod.identifier("headbutt_ravager"), new OBBCollider(0.8, 0.8, 0.8, 0.0, 0.0, -0.3));
   public static final Collider UCHIGATANA = registerCollider(EpicFightMod.identifier("uchigatana"), new MultiOBBCollider(5, 0.4, 0.4, 0.7, 0.0, 0.0, -0.7));
   public static final Collider TACHI = registerCollider(EpicFightMod.identifier("tachi"), new MultiOBBCollider(3, 0.4, 0.4, 0.95, 0.0, 0.0, -0.95));
   public static final Collider SWORD = registerCollider(EpicFightMod.identifier("sword"), new MultiOBBCollider(3, 0.4, 0.4, 0.7, 0.0, 0.0, -0.35));
   public static final Collider LONGSWORD = registerCollider(EpicFightMod.identifier("longsword"), new MultiOBBCollider(3, 0.4, 0.4, 0.8, 0.0, 0.0, -0.75));
   public static final Collider SPEAR = registerCollider(EpicFightMod.identifier("spear"), new MultiOBBCollider(3, 0.6, 0.6, 1.0, 0.0, 0.0, -1.0));
   public static final Collider SPIDER = registerCollider(EpicFightMod.identifier("spider"), new OBBCollider(0.8, 0.8, 0.8, 0.0, 0.0, -0.4));
   public static final Collider STEEL_WHIRLWIND = registerCollider(
      EpicFightMod.identifier("steel_whirlwind"),
      new MultiOBBCollider(
         new OBBCollider(1.8, 0.6, 1.5, 0.0, 1.0, -0.5),
         new OBBCollider(1.8, 0.6, 1.5, 0.0, 1.0, -0.5),
         new OBBCollider(1.8, 0.6, 1.5, 0.0, 1.0, -0.5),
         new OBBCollider(1.8, 0.6, 1.5, 0.0, 1.0, -0.5)
      )
   );
   public static final Collider TOOLS = registerCollider(EpicFightMod.identifier("tools"), new MultiOBBCollider(3, 0.4, 0.4, 0.55, 0.0, 0.0, -0.25));
   public static final Collider ENDERMAN_LIMB = registerCollider(EpicFightMod.identifier("enderman_limb"), new OBBCollider(0.4, 0.8, 0.4, 0.0, 0.0, 0.0));
   public static final Collider GOLEM_SMASHDOWN = registerCollider(
      EpicFightMod.identifier("golem_smashdown"), new MultiOBBCollider(3, 0.75, 0.5, 0.5, 0.6, 0.5, 0.0)
   );
   public static final Collider GOLEM_SWING_ARM = registerCollider(
      EpicFightMod.identifier("golem_swing_arm"), new MultiOBBCollider(2, 0.6, 0.9, 0.6, 0.0, 0.0, 0.0)
   );
   public static final Collider FIST_FIXED = registerCollider(EpicFightMod.identifier("fist_fixed"), new OBBCollider(0.4, 0.4, 0.5, 0.0, 1.25, -0.85));
   public static final Collider DUAL_SWORD_AIR_SLASH = registerCollider(
      EpicFightMod.identifier("dual_sword_air_slash"), new OBBCollider(0.8, 0.4, 1.0, 0.0, 0.5, -0.5)
   );
   public static final Collider DUAL_DAGGER_AIR_SLASH = registerCollider(
      EpicFightMod.identifier("dual_dagger_air_slash"), new OBBCollider(0.8, 0.4, 0.75, 0.0, 0.5, -0.5)
   );
   public static final Collider WITHER_CHARGE = registerCollider(
      EpicFightMod.identifier("wither_charge"), new MultiOBBCollider(5, 0.7, 0.9, 0.7, 0.0, 1.0, -0.35)
   );
   public static final Collider VEX_CHARGE = registerCollider(EpicFightMod.identifier("vex_charge"), new MultiOBBCollider(3, 0.4, 0.4, 0.95, 0.0, 0.2, -0.85));

   public static Collider registerCollider(ResourceLocation rl, Collider collider) {
      if (PRESETS.containsKey(rl)) {
         throw new IllegalStateException("Collider named " + rl + " already registered.");
      }

      PRESETS.put(rl, collider);
      return collider;
   }

   public static Set<Entry<ResourceLocation, Collider>> entries() {
      return Collections.unmodifiableSet(PRESETS.entrySet());
   }

   public static ResourceLocation getKey(Collider collider) {
      return (ResourceLocation)PRESETS.inverse().get(collider);
   }

   public static Collider get(ResourceLocation rl) {
      return (Collider)PRESETS.get(rl);
   }

   public static Collider deserializeSimpleCollider(CompoundTag tag) throws IllegalArgumentException {
      int number = tag.m_128451_("number");
      if (number < 1) {
         throw new IllegalArgumentException("Datapack deserialization error: the number of colliders must bigger than 0!");
      } else {
         ListTag sizeVector = tag.m_128437_("size", 6);
         ListTag centerVector = tag.m_128437_("center", 6);
         if (sizeVector.size() != 3) {
            throw new IllegalArgumentException("The size list tag must consist of three double elements.");
         } else if (centerVector.size() != 3) {
            throw new IllegalArgumentException("The center list tag must consist of three double elements.");
         } else {
            double sizeX = sizeVector.m_128772_(0);
            double sizeY = sizeVector.m_128772_(1);
            double sizeZ = sizeVector.m_128772_(2);
            double centerX = centerVector.m_128772_(0);
            double centerY = centerVector.m_128772_(1);
            double centerZ = centerVector.m_128772_(2);
            if (!(sizeX < 0.0) && !(sizeY < 0.0) && !(sizeZ < 0.0) && (sizeX != 0.0 || sizeY != 0.0 || sizeZ != 0.0)) {
               return number == 1
                  ? new OBBCollider(sizeX, sizeY, sizeZ, centerX, centerY, centerZ)
                  : new MultiOBBCollider(number, sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
            } else {
               throw new IllegalArgumentException("Datapack deserialization error: the size of the collider must be non-negative value!");
            }
         }
      }
   }

   public CompletableFuture<Void> m_5540_(
      PreparationBarrier stage,
      ResourceManager resourceManager,
      ProfilerFiller preparationsProfiler,
      ProfilerFiller reloadProfiler,
      Executor backgroundExecutor,
      Executor gameExecutor
   ) {
      return CompletableFuture.runAsync(() -> {}, gameExecutor).thenCompose(stage::m_6769_);
   }
}
