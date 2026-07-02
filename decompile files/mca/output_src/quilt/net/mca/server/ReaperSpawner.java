package quilt.net.mca.server;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1299;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2487;
import net.minecraft.class_2512;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3414;
import net.minecraft.class_3481;
import net.minecraft.class_3730;
import net.minecraft.class_2338.class_2339;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.SoundsMCA;
import quilt.net.mca.block.BlocksMCA;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.entity.GrimReaperEntity;
import quilt.net.mca.server.world.data.VillageManager;
import quilt.net.mca.util.NbtHelper;
import quilt.net.mca.util.WorldUtils;

public class ReaperSpawner {
   private static final class_2350[] HORIZONTALS = new class_2350[]{
      class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039
   };
   private final Object lock = new Object();
   private final Map<Long, ReaperSpawner.ActiveSummon> activeSummons = new ConcurrentHashMap<>();
   private final VillageManager manager;

   public ReaperSpawner(VillageManager manager) {
      this.manager = manager;
   }

   public ReaperSpawner(VillageManager manager, class_2487 nbt) {
      this.manager = manager;
      NbtHelper.toList(nbt.method_10554("summons", 10), n -> new ReaperSpawner.ActiveSummon((class_2487)n))
         .forEach(summon -> this.activeSummons.put(summon.position.spawnPosition.method_10063(), summon));
   }

   private void warn(class_1937 world, class_2338 pos, String phrase) {
      world.method_18456()
         .stream()
         .min(Comparator.comparingInt(a -> a.method_24515().method_19455(pos)))
         .ifPresent(p -> p.method_7353(class_2561.method_43471(phrase).method_27692(class_124.field_1061), true));
   }

   public void trySpawnReaper(class_3218 world, class_2338 pos) {
      if (Config.getInstance().allowGrimReaper) {
         class_1923 chunkPos = new class_1923(pos);
         if (WorldUtils.isAreaLoaded(world, chunkPos, 1)) {
            if (world.method_8320(pos).method_26204() == class_2246.field_10234) {
               MCA.LOGGER.info("Attempting to spawn reaper at {} in {}", pos, world.method_27983().method_29177());
               if (!this.isNightTime(world)) {
                  this.warn(world, pos, "reaper.day");
               } else {
                  Set<class_2338> totems = this.getTotemsFires(world, pos);
                  MCA.LOGGER.info("It is night time, found {} totems", totems.size());
                  if (totems.size() < 3) {
                     this.warn(world, pos, "reaper.totems");
                  } else {
                     this.start(new ReaperSpawner.SummonPosition(pos.method_10084(), totems));
                     class_1299.field_6112.method_47821(world, pos, class_3730.field_16461);
                     world.method_8652(pos, class_2246.field_22090.method_9564(), 3);
                     world.method_8652(pos.method_10084(), ((class_2248)BlocksMCA.INFERNAL_FLAME.get()).method_9564(), 3);
                     totems.forEach(totem -> world.method_8652(totem, ((class_2248)BlocksMCA.INFERNAL_FLAME.get()).method_9564(), 18));
                  }
               }
            }
         }
      }
   }

   private void start(ReaperSpawner.SummonPosition pos) {
      this.activeSummons.computeIfAbsent(pos.spawnPosition.method_10063(), ReaperSpawner.ActiveSummon::new).start(pos);
      this.manager.method_80();
   }

   public void tick(class_3218 world) {
      boolean empty = this.activeSummons.isEmpty();
      this.activeSummons.values().removeIf(summon -> {
         try {
            return summon.tick(world);
         } catch (Exception e) {
            MCA.LOGGER.error("Exception ticking summon", e);
            return true;
         }
      });
      if (!empty) {
         this.manager.method_80();
      }
   }

   private boolean isNightTime(class_1937 world) {
      long time = world.method_8532() % 24000L;
      MCA.LOGGER.info("Current time is {}", time);
      return time >= 13000L && time <= 23000L;
   }

   private Set<class_2338> getTotemsFires(class_1937 world, class_2338 pos) {
      int groundY = pos.method_10264() - 1;
      int leftSkyHeight = world.method_31600() - groundY;
      int minPillarHeight = Math.min(Config.getInstance().minPillarHeight, leftSkyHeight);
      class_2339 target = new class_2339();
      return Stream.of(HORIZONTALS).map(d -> target.method_10101(pos).method_33098(groundY).method_10104(d, 3)).filter(pillarPos -> {
         for (int height = 1; height <= leftSkyHeight; height++) {
            pillarPos.method_33098(groundY + height);
            if (!world.method_8320(pillarPos).method_27852(class_2246.field_10540)) {
               if (world.method_8320(pillarPos).method_26164(class_3481.field_21952)) {
                  return height - 1 >= minPillarHeight;
               }

               return false;
            }
         }

         return false;
      }).<class_2338>map(class_2338::method_10062).collect(Collectors.toSet());
   }

   public class_2487 writeNbt() {
      synchronized (this.lock) {
         class_2487 nbt = new class_2487();
         nbt.method_10566("summons", NbtHelper.fromList(this.activeSummons.values(), ReaperSpawner.ActiveSummon::write));
         return nbt;
      }
   }

   static class ActiveSummon {
      private int ticks;
      private ReaperSpawner.SummonPosition position;

      ActiveSummon(long l) {
      }

      ActiveSummon(class_2487 nbt) {
         this.ticks = nbt.method_10550("ticks");
         this.position = new ReaperSpawner.SummonPosition(nbt.method_10562("position"));
      }

      public void start(ReaperSpawner.SummonPosition pos) {
         if (this.ticks <= 0) {
            this.position = pos;
            this.ticks = 100;
         }
      }

      public boolean tick(class_3218 world) {
         if (this.ticks <= 0 || this.position == null) {
            return true;
         }

         if (this.position.isCancelled(world)) {
            this.position.totems.forEach(totem -> {
               if (this.position.check(totem, world)) {
                  world.method_8501(totem, class_2246.field_10036.method_9564());
               }
            });
            this.position = null;
            this.ticks = 0;
            return true;
         }

         if (--this.ticks % 20 == 0) {
            class_1299.field_6112.method_47821(world, this.position.spawnPosition, class_3730.field_16461);
         }

         if (this.ticks == 0) {
            GrimReaperEntity reaper = (GrimReaperEntity)((class_1299)EntitiesMCA.GRIM_REAPER.get())
               .method_47821(world, this.position.spawnPosition, class_3730.field_16461);
            if (reaper != null) {
               reaper.method_5783((class_3414)SoundsMCA.REAPER_SUMMON.get(), 1.0F, 1.0F);
            }

            return true;
         } else {
            return false;
         }
      }

      public class_2487 write() {
         class_2487 nbt = new class_2487();
         nbt.method_10569("ticks", this.ticks);
         nbt.method_10566("position", this.position.toNbt());
         return nbt;
      }
   }

   static class SummonPosition {
      public final class_2338 spawnPosition;
      public final class_2338 fire;
      public final Set<class_2338> totems;

      public SummonPosition(class_2487 tag) {
         if (!tag.method_10545("fire") && !tag.method_10545("totems") && !tag.method_10545("spawnPosition")) {
            this.totems = new HashSet<>();
            this.spawnPosition = class_2512.method_10691(tag);
            this.fire = this.spawnPosition.method_10087(10);
         } else {
            this.fire = class_2512.method_10691(tag.method_10562("fire"));
            this.totems = new HashSet<>(NbtHelper.toList(tag.method_10562("totems"), v -> class_2512.method_10691((class_2487)v)));
            this.spawnPosition = class_2512.method_10691(tag.method_10562("spawnPosition"));
         }
      }

      public SummonPosition(class_2338 fire, Set<class_2338> totems) {
         this.fire = fire;
         this.spawnPosition = fire.method_10086(10);
         this.totems = totems;
      }

      public boolean isCancelled(class_1937 world) {
         return !this.check(this.fire, world);
      }

      private boolean check(class_2338 pos, class_1937 world) {
         return world.method_8320(pos).method_27852((class_2248)BlocksMCA.INFERNAL_FLAME.get());
      }

      public class_2487 toNbt() {
         class_2487 tag = new class_2487();
         tag.method_10566("fire", class_2512.method_10692(this.fire));
         tag.method_10566("totems", NbtHelper.fromList(this.totems, class_2512::method_10692));
         tag.method_10566("spawnPosition", class_2512.method_10692(this.spawnPosition));
         return tag;
      }
   }
}
