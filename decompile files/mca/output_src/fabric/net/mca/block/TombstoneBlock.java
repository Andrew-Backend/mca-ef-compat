package fabric.net.mca.block;

import fabric.net.mca.entity.Infectable;
import fabric.net.mca.entity.ai.relationship.CompassionateEntity;
import fabric.net.mca.entity.ai.relationship.EntityRelationship;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.server.world.data.GraveyardManager;
import fabric.net.mca.util.NbtHelper;
import fabric.net.mca.util.VoxelShapeUtil;
import fabric.net.mca.util.localization.FlowingText;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1538;
import net.minecraft.class_1641;
import net.minecraft.class_1750;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_181;
import net.minecraft.class_1922;
import net.minecraft.class_1936;
import net.minecraft.class_1937;
import net.minecraft.class_2237;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2398;
import net.minecraft.class_2415;
import net.minecraft.class_243;
import net.minecraft.class_2464;
import net.minecraft.class_2470;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_259;
import net.minecraft.class_2591;
import net.minecraft.class_2622;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3612;
import net.minecraft.class_3726;
import net.minecraft.class_3737;
import net.minecraft.class_4538;
import net.minecraft.class_5431;
import net.minecraft.class_5558;
import net.minecraft.class_5712;
import net.minecraft.class_5819;
import net.minecraft.class_2350.class_2351;
import net.minecraft.class_2561.class_2562;
import net.minecraft.class_2689.class_2690;
import net.minecraft.class_4970.class_2251;
import net.minecraft.class_5712.class_7397;
import net.minecraft.class_8567.class_8568;
import org.jetbrains.annotations.Nullable;

public class TombstoneBlock extends class_2237 implements class_3737 {
   public static final class_265 GRAVELLING_SHAPE = class_2248.method_9541(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
   public static final class_265 UPRIGHT_SHAPE = class_259.method_1084(
      class_2248.method_9541(2.0, 2.0, 7.0, 14.0, 15.0, 9.0), class_2248.method_9541(1.0, 0.0, 6.0, 15.0, 2.0, 10.0)
   );
   public static final class_265 CROSS_SHAPE = class_259.method_1084(
      class_2248.method_9541(6.0, 0.0, 2.0, 10.0, 28.0, 4.0), class_2248.method_9541(-1.0, 18.0, 2.0, 17.0, 21.0, 4.0)
   );
   public static final class_265 SLANTED_SHAPE = class_2248.method_9541(0.0, 0.0, 2.0, 16.0, 7.0, 14.0);
   public static final class_265 WALL_SHAPE = class_2248.method_9541(1.0, 1.0, 0.0, 15.0, 15.0, 1.0);
   private final Map<class_2350, class_265> shapes;
   private final int lineWidth;
   private final int maxNameHeight;
   private final class_243 nameplateOffset;
   private final boolean requiresSolid;
   private final float rotation;

   public TombstoneBlock(
      class_2251 properties, int lineWidth, int maxNameHeight, class_243 nameplateOffset, float rotation, boolean requiresSolid, class_265 baseShape
   ) {
      super(properties);
      this.method_9590((class_2680)this.method_9564().method_11657(class_2741.field_12508, false));
      this.lineWidth = lineWidth;
      this.maxNameHeight = maxNameHeight;
      this.nameplateOffset = nameplateOffset;
      this.rotation = rotation;
      this.requiresSolid = requiresSolid;
      this.shapes = Arrays.stream(class_2350.values())
         .filter(d -> d.method_10166() != class_2351.field_11052)
         .collect(Collectors.toMap(Function.identity(), VoxelShapeUtil.rotator(baseShape)));
   }

   public int getLineWidth() {
      return this.lineWidth;
   }

   public int getMaxNameHeight() {
      return this.maxNameHeight;
   }

   public class_243 getNameplateOffset() {
      return this.nameplateOffset;
   }

   public float getRotation() {
      return this.rotation;
   }

   public boolean method_9526(class_2680 state) {
      return true;
   }

   @Deprecated
   public class_265 method_9530(class_2680 state, class_1922 view, class_2338 pos, class_3726 ePos) {
      if (this == BlocksMCA.SLANTED_HEADSTONE.get()) {
         this.shapes.replaceAll((i, v) -> VoxelShapeUtil.rotator(SLANTED_SHAPE).apply(i));
      }

      return this.shapes.getOrDefault(state.method_11654(class_2741.field_12481), class_259.method_1077());
   }

   public class_2464 method_9604(class_2680 state) {
      return class_2464.field_11458;
   }

   public void method_9615(class_2680 state, class_1937 world, class_2338 pos, class_2680 oldState, boolean notify) {
      this.updateTombstoneState(world, pos);
   }

   public void method_9567(class_1937 world, class_2338 pos, class_2680 state, class_1309 placer, class_1799 stack) {
      TombstoneBlock.Data.of(world.method_8321(pos)).ifPresent(data -> data.readFromStack(stack));
      this.updateTombstoneState(world, pos);
   }

   private void updateTombstoneState(class_1937 world, class_2338 pos) {
      if (!world.field_9236) {
         GraveyardManager.get((class_3218)world)
            .setTombstoneState(pos, this.hasEntity(world, pos) ? GraveyardManager.TombstoneState.FILLED : GraveyardManager.TombstoneState.EMPTY);
      }
   }

   @Deprecated
   public void method_9536(class_2680 state, class_1937 world, class_2338 pos, class_2680 newState, boolean moved) {
      super.method_9536(state, world, pos, newState, moved);
      if (!world.field_9236 && !state.method_27852(newState.method_26204())) {
         this.updateNeighbors(state, world, pos);
         GraveyardManager.get((class_3218)world).removeTombstoneState(pos);
      }
   }

   public class_2586 method_10123(class_2338 pos, class_2680 state) {
      return ((class_2591)BlockEntityTypesMCA.TOMBSTONE.get()).method_11032(pos, state);
   }

   @Nullable
   public <T extends class_2586> class_5558<T> method_31645(class_1937 world, class_2680 state, class_2591<T> type) {
      return world.field_9236 ? null : (w, pos, s, data) -> ((TombstoneBlock.Data)data).tick();
   }

   protected void method_9515(class_2690<class_2248, class_2680> builder) {
      builder.method_11667(new class_2769[]{class_2741.field_12508}).method_11667(new class_2769[]{class_2741.field_12481});
   }

   @Deprecated
   public class_2680 method_9559(class_2680 state, class_2350 direction, class_2680 neighborState, class_1936 world, class_2338 pos, class_2338 neighborPos) {
      if ((Boolean)state.method_11654(class_2741.field_12508)) {
         world.method_39281(pos, class_3612.field_15910, class_3612.field_15910.method_15789(world));
      }

      return direction == class_2350.field_11033 && !this.method_9558(state, world, pos)
         ? class_2246.field_10124.method_9564()
         : super.method_9559(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean method_9558(class_2680 state, class_4538 world, class_2338 pos) {
      if (this.requiresSolid) {
         pos = pos.method_10074();
         return world.method_8320(pos).method_30368(world, pos, class_2350.field_11036, class_5431.field_25822);
      } else {
         return true;
      }
   }

   @Deprecated
   public class_3610 method_9545(class_2680 state) {
      return state.method_11654(class_2741.field_12508) ? class_3612.field_15910.method_15729(false) : super.method_9545(state);
   }

   private void updateNeighbors(class_2680 state, class_1937 world, class_2338 pos) {
      world.method_8452(pos, this);
      world.method_8452(pos.method_10093((class_2350)state.method_11654(class_2741.field_12481)), this);
   }

   @Nullable
   public class_2680 method_9605(class_1750 context) {
      return (class_2680)this.method_9564().method_11657(class_2741.field_12481, context.method_8042().method_10153());
   }

   public class_2680 method_9598(class_2680 state, class_2470 rot) {
      return (class_2680)state.method_11657(class_2741.field_12481, rot.method_10503((class_2350)state.method_11654(class_2741.field_12481)));
   }

   public class_2680 method_9569(class_2680 state, class_2415 mirror) {
      return state.method_26186(mirror.method_10345((class_2350)state.method_11654(class_2741.field_12481)));
   }

   public int method_9524(class_2680 state, class_1922 world, class_2338 pos, class_2350 direction) {
      return state.method_26203(world, pos, direction);
   }

   public int method_9603(class_2680 state, class_1922 world, class_2338 pos, class_2350 direction) {
      return direction == state.method_11654(class_2741.field_12481) && this.hasEntity(world, pos) ? 15 : 0;
   }

   protected boolean hasEntity(class_1922 world, class_2338 pos) {
      return TombstoneBlock.Data.of(world.method_8321(pos)).map(TombstoneBlock.Data::hasEntity).orElse(false);
   }

   public boolean method_9506(class_2680 state) {
      return true;
   }

   public void method_9496(class_2680 state, class_1937 world, class_2338 pos, class_5819 random) {
      TombstoneBlock.Data.of(world.method_8321(pos))
         .filter(TombstoneBlock.Data::isResurrecting)
         .ifPresent(
            data -> {
               for (int i = 0; i < random.method_43048(8) + 1; i++) {
                  world.method_8406(
                     random.method_43056() ? class_2398.field_11237 : class_2398.field_11251,
                     pos.method_10263() + random.method_43057(),
                     pos.method_10264() + random.method_43057(),
                     pos.method_10260() + random.method_43057(),
                     (random.method_43057() - 0.5) / 10.0,
                     0.0,
                     (random.method_43057() - 0.5) / 10.0
                  );
               }
            }
         );
   }

   @Deprecated
   public List<class_1799> method_9560(class_2680 state, class_8568 builder) {
      List<class_1799> stacks = super.method_9560(state, builder);
      Optional<TombstoneBlock.Data> data = TombstoneBlock.Data.of((class_2586)builder.method_51876(class_181.field_1228))
         .filter(TombstoneBlock.Data::hasEntity);
      data.flatMap(TombstoneBlock.Data::getEntityName).ifPresent(name -> stacks.stream().filter(TombstoneBlock::isRemains).forEach(stack -> {
         stack.method_7925();
         stack.method_7977(class_2561.method_43469("block.mca.tombstone.remains", new Object[]{stack.method_7964(), name}));
      }));
      data.ifPresent(be -> stacks.stream().filter(s -> s.method_7909() == this.method_8389()).findFirst().ifPresent(be::writeToStack));
      return stacks;
   }

   static boolean isRemains(class_1799 stack) {
      return stack.method_7909() == class_1802.field_8606 || stack.method_7909() == class_1802.field_8398;
   }

   public static class Data extends class_2586 {
      private Optional<TombstoneBlock.Data.EntityData> entityData = Optional.empty();
      @Nullable
      private FlowingText computedName;
      private int resurrectionProgress;
      private boolean cure;

      public Data(class_2338 pos, class_2680 state) {
         super((class_2591)BlockEntityTypesMCA.TOMBSTONE.get(), pos, state);
      }

      public boolean isResurrecting() {
         return this.resurrectionProgress > 0;
      }

      public void startResurrecting(boolean cure) {
         this.resurrectionProgress = 1;
         this.cure = cure;
         this.generateLightning();
         this.method_5431();
         this.sync();
      }

      public void tick() {
         if (this.hasEntity() && this.resurrectionProgress > 0) {
            this.resurrectionProgress++;
            this.method_5431();
            this.sync();
            if (this.resurrectionProgress % 30 == 0) {
               this.field_11863
                  .method_43128(
                     null,
                     this.field_11867.method_10263(),
                     this.field_11867.method_10264(),
                     this.field_11867.method_10260(),
                     this.cure ? class_3417.field_17265 : class_3417.field_15078,
                     class_3419.field_15245,
                     1.0F,
                     1.0F
                  );
               this.field_11863.method_20290(2001, this.field_11867, class_2248.method_9507(this.method_11010()));
            }

            if (this.field_11863.field_9229.method_43048(10) > 5 && this.resurrectionProgress % 20 == 0) {
               this.generateLightning();
            }

            if (this.resurrectionProgress > 500) {
               this.resurrectionProgress = 0;
               this.createEntityWithoutActiveEffects(this.field_11863, true)
                  .ifPresent(
                     entity -> {
                        this.generateLightning();
                        entity.method_5646();
                        entity.method_30229();
                        entity.method_5814(
                           this.field_11867.method_10263() + 0.5F, this.field_11867.method_10264() + 0.5F, this.field_11867.method_10260() + 0.5F
                        );
                        if (entity instanceof class_1309 l) {
                           l.method_6033(l.method_6063());
                           l.field_6017 = 0.0F;
                           l.field_6213 = 0;
                        }

                        if (entity instanceof class_1296 mob) {
                           mob.method_5614(mob.method_5618());
                        }

                        boolean alreadySpawned = false;
                        if (this.cure && entity instanceof class_1641 zombie) {
                           entity = zombie.method_29243(class_1299.field_6077, true);
                           alreadySpawned = true;
                        }

                        if (entity instanceof CompassionateEntity<?> compassionateEntity) {
                           compassionateEntity.getRelationships().getFamilyEntry().setDeceased(false);
                        }

                        if (entity instanceof Infectable infectable) {
                           infectable.setInfectionProgress(
                              this.cure
                                 ? 0.0F
                                 : Math.max(class_3532.method_16439(this.field_11863.field_9229.method_43057(), 0.2F, 0.6F), infectable.getInfectionProgress())
                           );
                        }

                        if (!alreadySpawned) {
                           this.field_11863.method_8649(entity);
                        }
                     }
                  );
            }
         }
      }

      private void generateLightning() {
         this.field_11863.method_8509(10);
         class_1538 bolt = (class_1538)class_1299.field_6112.method_5883(this.field_11863);
         bolt.method_29498(true);
         bolt.method_30634(this.field_11867.method_10263() + 0.5F, this.field_11867.method_10264(), this.field_11867.method_10260() + 0.5F);
         this.field_11863.method_8649(bolt);
      }

      public void setEntity(@Nullable class_1297 entity) {
         this.entityData = Optional.ofNullable(entity)
            .map(
               e -> new TombstoneBlock.Data.EntityData(
                  this.writeEntityToNbt(e), e.method_5477(), EntityRelationship.of(e).map(EntityRelationship::getGender).orElse(Gender.MALE)
               )
            );
         this.computedName = null;
         this.method_5431();
         if (this.method_11002()) {
            this.field_11863
               .method_43128(
                  null,
                  this.field_11867.method_10263(),
                  this.field_11867.method_10264(),
                  this.field_11867.method_10260(),
                  class_3417.field_17265,
                  class_3419.field_15245,
                  1.0F,
                  1.0F
               );
            this.field_11863.method_20290(2001, this.field_11867, class_2248.method_9507(this.method_11010()));
            this.field_11863.method_43276(class_5712.field_28733, this.field_11867, class_7397.method_43287(this.method_11010()));
            ((TombstoneBlock)this.method_11010().method_26204()).updateNeighbors(this.method_11010(), this.field_11863, this.field_11867);
            if (!this.field_11863.field_9236) {
               GraveyardManager.get((class_3218)this.field_11863)
                  .setTombstoneState(this.field_11867, this.hasEntity() ? GraveyardManager.TombstoneState.FILLED : GraveyardManager.TombstoneState.EMPTY);
               this.sync();
            }
         }
      }

      public boolean hasEntity() {
         return this.entityData.isPresent();
      }

      public Gender getGender() {
         return this.entityData.<Gender>map(e -> e.gender).orElse(Gender.MALE);
      }

      public Optional<class_2561> getEntityName() {
         return this.entityData.map(e -> e.name);
      }

      public FlowingText getOrCreateEntityName(Function<class_2561, FlowingText> factory) {
         if (this.computedName == null) {
            this.computedName = factory.apply(this.getEntityName().orElse(class_2561.method_43470("")));
         }

         return this.computedName;
      }

      public Optional<class_1297> createEntity(class_1937 world, boolean remove) {
         return this.createEntity(world, remove, Function.identity());
      }

      private Optional<class_1297> createEntityWithoutActiveEffects(class_1937 world, boolean remove) {
         return this.createEntity(world, remove, this::withoutActiveEffects);
      }

      private Optional<class_1297> createEntity(class_1937 world, boolean remove, Function<class_2487, class_2487> nbtTransform) {
         try {
            return this.entityData.flatMap(data -> class_1299.method_5892(nbtTransform.apply(data.nbt), world));
         } finally {
            if (remove) {
               this.setEntity(null);
            }
         }
      }

      private class_2487 withoutActiveEffects(class_2487 nbt) {
         class_2487 copy = nbt.method_10553();
         copy.method_10551("ActiveEffects");
         return copy;
      }

      private class_2487 writeEntityToNbt(class_1297 entity) {
         class_2487 nbt = new class_2487();
         entity.method_5647(nbt);
         nbt.method_10582("id", class_1299.method_5890(entity.method_5864()).toString());
         return nbt;
      }

      protected void sync() {
         this.method_5431();
         this.field_11863.method_8413(this.method_11016(), this.method_11010(), this.method_11010(), 3);
      }

      public void method_11014(class_2487 tag) {
         this.entityData = tag.method_10573("entityData", 10) ? Optional.of(new TombstoneBlock.Data.EntityData(tag)) : Optional.empty();
         this.resurrectionProgress = tag.method_10550("resurrectionProgress");
         this.cure = tag.method_10577("cure");
      }

      public void method_11007(class_2487 nbt) {
         this.entityData.ifPresent(data -> data.writeNbt(nbt));
         nbt.method_10569("resurrectionProgress", this.resurrectionProgress);
         nbt.method_10556("cure", this.cure);
      }

      public class_2487 method_16887() {
         class_2487 tag = new class_2487();
         this.method_11007(tag);
         return tag;
      }

      public class_2622 toUpdatePacket() {
         return class_2622.method_39026(this, class_2586::method_16887);
      }

      public void readFromStack(class_1799 stack) {
         this.entityData = Optional.ofNullable(stack).map(s -> s.method_7941("entityData")).map(TombstoneBlock.Data.EntityData::new);
      }

      public void writeToStack(class_1799 stack) {
         this.entityData
            .ifPresent(
               data -> {
                  data.writeNbt(stack.method_7911("entityData"));
                  this.getEntityName()
                     .ifPresent(
                        name -> ((class_2499)NbtHelper.computeIfAbsent(stack.method_7911("display"), "Lore", 9, class_2499::new))
                           .method_10531(0, class_2519.method_23256(name.getString()))
                     );
               }
            );
      }

      public static Optional<TombstoneBlock.Data> of(@Nullable class_2586 be) {
         return Optional.ofNullable(be).filter(p -> p instanceof TombstoneBlock.Data).map(TombstoneBlock.Data.class::cast);
      }

      static final class EntityData {
         private final class_2487 nbt;
         private final class_2561 name;
         private final Gender gender;

         public EntityData(class_2487 nbt, class_2561 name, Gender gender) {
            this.nbt = nbt;
            this.name = name == null ? class_2561.method_43470("") : class_2561.method_43470(name.getString());
            this.gender = gender;
         }

         EntityData(class_2487 nbt) {
            this(nbt.method_10562("entityData"), class_2562.method_10877(nbt.method_10558("entityName")), Gender.byId(nbt.method_10550("entityGender")));
         }

         void writeNbt(class_2487 nbt) {
            nbt.method_10566("entityData", this.nbt);
            nbt.method_10582("entityName", class_2562.method_10867(this.name));
            nbt.method_10569("entityGender", this.gender.ordinal());
         }
      }
   }
}
