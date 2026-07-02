package forge.net.mca.block;

import forge.net.mca.entity.Infectable;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.server.world.data.GraveyardManager;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.VoxelShapeUtil;
import forge.net.mca.util.localization.FlowingText;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TombstoneBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final VoxelShape GRAVELLING_SHAPE = Block.m_49796_(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
   public static final VoxelShape UPRIGHT_SHAPE = Shapes.m_83110_(
      Block.m_49796_(2.0, 2.0, 7.0, 14.0, 15.0, 9.0), Block.m_49796_(1.0, 0.0, 6.0, 15.0, 2.0, 10.0)
   );
   public static final VoxelShape CROSS_SHAPE = Shapes.m_83110_(
      Block.m_49796_(6.0, 0.0, 2.0, 10.0, 28.0, 4.0), Block.m_49796_(-1.0, 18.0, 2.0, 17.0, 21.0, 4.0)
   );
   public static final VoxelShape SLANTED_SHAPE = Block.m_49796_(0.0, 0.0, 2.0, 16.0, 7.0, 14.0);
   public static final VoxelShape WALL_SHAPE = Block.m_49796_(1.0, 1.0, 0.0, 15.0, 15.0, 1.0);
   private final Map<Direction, VoxelShape> shapes;
   private final int lineWidth;
   private final int maxNameHeight;
   private final Vec3 nameplateOffset;
   private final boolean requiresSolid;
   private final float rotation;

   public TombstoneBlock(
      Properties properties, int lineWidth, int maxNameHeight, Vec3 nameplateOffset, float rotation, boolean requiresSolid, VoxelShape baseShape
   ) {
      super(properties);
      this.m_49959_((BlockState)this.m_49966_().m_61124_(BlockStateProperties.f_61362_, false));
      this.lineWidth = lineWidth;
      this.maxNameHeight = maxNameHeight;
      this.nameplateOffset = nameplateOffset;
      this.rotation = rotation;
      this.requiresSolid = requiresSolid;
      this.shapes = Arrays.stream(Direction.values())
         .filter(d -> d.m_122434_() != Axis.Y)
         .collect(Collectors.toMap(Function.identity(), VoxelShapeUtil.rotator(baseShape)));
   }

   public int getLineWidth() {
      return this.lineWidth;
   }

   public int getMaxNameHeight() {
      return this.maxNameHeight;
   }

   public Vec3 getNameplateOffset() {
      return this.nameplateOffset;
   }

   public float getRotation() {
      return this.rotation;
   }

   public boolean m_7923_(BlockState state) {
      return true;
   }

   @Deprecated
   public VoxelShape m_5940_(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
      if (this == BlocksMCA.SLANTED_HEADSTONE.get()) {
         this.shapes.replaceAll((i, v) -> VoxelShapeUtil.rotator(SLANTED_SHAPE).apply(i));
      }

      return this.shapes.getOrDefault(state.m_61143_(BlockStateProperties.f_61374_), Shapes.m_83144_());
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.MODEL;
   }

   public void m_6807_(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
      this.updateTombstoneState(world, pos);
   }

   public void m_6402_(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      TombstoneBlock.Data.of(world.m_7702_(pos)).ifPresent(data -> data.readFromStack(stack));
      this.updateTombstoneState(world, pos);
   }

   private void updateTombstoneState(Level world, BlockPos pos) {
      if (!world.f_46443_) {
         GraveyardManager.get((ServerLevel)world)
            .setTombstoneState(pos, this.hasEntity(world, pos) ? GraveyardManager.TombstoneState.FILLED : GraveyardManager.TombstoneState.EMPTY);
      }
   }

   @Deprecated
   public void m_6810_(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
      super.m_6810_(state, world, pos, newState, moved);
      if (!world.f_46443_ && !state.m_60713_(newState.m_60734_())) {
         this.updateNeighbors(state, world, pos);
         GraveyardManager.get((ServerLevel)world).removeTombstoneState(pos);
      }
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return ((BlockEntityType)BlockEntityTypesMCA.TOMBSTONE.get()).m_155264_(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level world, BlockState state, BlockEntityType<T> type) {
      return world.f_46443_ ? null : (w, pos, s, data) -> ((TombstoneBlock.Data)data).tick();
   }

   protected void m_7926_(Builder<Block, BlockState> builder) {
      builder.m_61104_(new Property[]{BlockStateProperties.f_61362_}).m_61104_(new Property[]{BlockStateProperties.f_61374_});
   }

   @Deprecated
   public BlockState m_7417_(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.m_61143_(BlockStateProperties.f_61362_)) {
         world.m_186469_(pos, Fluids.f_76193_, Fluids.f_76193_.m_6718_(world));
      }

      return direction == Direction.DOWN && !this.m_7898_(state, world, pos)
         ? Blocks.f_50016_.m_49966_()
         : super.m_7417_(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean m_7898_(BlockState state, LevelReader world, BlockPos pos) {
      if (this.requiresSolid) {
         pos = pos.m_7495_();
         return world.m_8055_(pos).m_60659_(world, pos, Direction.UP, SupportType.FULL);
      } else {
         return true;
      }
   }

   @Deprecated
   public FluidState m_5888_(BlockState state) {
      return state.m_61143_(BlockStateProperties.f_61362_) ? Fluids.f_76193_.m_76068_(false) : super.m_5888_(state);
   }

   private void updateNeighbors(BlockState state, Level world, BlockPos pos) {
      world.m_46672_(pos, this);
      world.m_46672_(pos.m_121945_((Direction)state.m_61143_(BlockStateProperties.f_61374_)), this);
   }

   @Nullable
   public BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)this.m_49966_().m_61124_(BlockStateProperties.f_61374_, context.m_8125_().m_122424_());
   }

   public BlockState m_6843_(BlockState state, Rotation rot) {
      return (BlockState)state.m_61124_(BlockStateProperties.f_61374_, rot.m_55954_((Direction)state.m_61143_(BlockStateProperties.f_61374_)));
   }

   public BlockState m_6943_(BlockState state, Mirror mirror) {
      return state.m_60717_(mirror.m_54846_((Direction)state.m_61143_(BlockStateProperties.f_61374_)));
   }

   public int m_6378_(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
      return state.m_60775_(world, pos, direction);
   }

   public int m_6376_(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
      return direction == state.m_61143_(BlockStateProperties.f_61374_) && this.hasEntity(world, pos) ? 15 : 0;
   }

   protected boolean hasEntity(BlockGetter world, BlockPos pos) {
      return TombstoneBlock.Data.of(world.m_7702_(pos)).map(TombstoneBlock.Data::hasEntity).orElse(false);
   }

   public boolean m_7899_(BlockState state) {
      return true;
   }

   public void m_214162_(BlockState state, Level world, BlockPos pos, RandomSource random) {
      TombstoneBlock.Data.of(world.m_7702_(pos))
         .filter(TombstoneBlock.Data::isResurrecting)
         .ifPresent(
            data -> {
               for (int i = 0; i < random.m_188503_(8) + 1; i++) {
                  world.m_7106_(
                     random.m_188499_() ? ParticleTypes.f_123755_ : ParticleTypes.f_123762_,
                     pos.m_123341_() + random.m_188501_(),
                     pos.m_123342_() + random.m_188501_(),
                     pos.m_123343_() + random.m_188501_(),
                     (random.m_188501_() - 0.5) / 10.0,
                     0.0,
                     (random.m_188501_() - 0.5) / 10.0
                  );
               }
            }
         );
   }

   @Deprecated
   public List<ItemStack> m_49635_(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      List<ItemStack> stacks = super.m_49635_(state, builder);
      Optional<TombstoneBlock.Data> data = TombstoneBlock.Data.of((BlockEntity)builder.m_287159_(LootContextParams.f_81462_))
         .filter(TombstoneBlock.Data::hasEntity);
      data.flatMap(TombstoneBlock.Data::getEntityName).ifPresent(name -> stacks.stream().filter(TombstoneBlock::isRemains).forEach(stack -> {
         stack.m_41787_();
         stack.m_41714_(Component.m_237110_("block.mca.tombstone.remains", new Object[]{stack.m_41786_(), name}));
      }));
      data.ifPresent(be -> stacks.stream().filter(s -> s.m_41720_() == this.m_5456_()).findFirst().ifPresent(be::writeToStack));
      return stacks;
   }

   static boolean isRemains(ItemStack stack) {
      return stack.m_41720_() == Items.f_42500_ || stack.m_41720_() == Items.f_42678_;
   }

   public static class Data extends BlockEntity {
      private Optional<TombstoneBlock.Data.EntityData> entityData = Optional.empty();
      @Nullable
      private FlowingText computedName;
      private int resurrectionProgress;
      private boolean cure;

      public Data(BlockPos pos, BlockState state) {
         super((BlockEntityType)BlockEntityTypesMCA.TOMBSTONE.get(), pos, state);
      }

      public boolean isResurrecting() {
         return this.resurrectionProgress > 0;
      }

      public void startResurrecting(boolean cure) {
         this.resurrectionProgress = 1;
         this.cure = cure;
         this.generateLightning();
         this.m_6596_();
         this.sync();
      }

      public void tick() {
         if (this.hasEntity() && this.resurrectionProgress > 0) {
            this.resurrectionProgress++;
            this.m_6596_();
            this.sync();
            if (this.resurrectionProgress % 30 == 0) {
               this.f_58857_
                  .m_6263_(
                     null,
                     this.f_58858_.m_123341_(),
                     this.f_58858_.m_123342_(),
                     this.f_58858_.m_123343_(),
                     this.cure ? SoundEvents.f_11699_ : SoundEvents.f_12280_,
                     SoundSource.BLOCKS,
                     1.0F,
                     1.0F
                  );
               this.f_58857_.m_46796_(2001, this.f_58858_, Block.m_49956_(this.m_58900_()));
            }

            if (this.f_58857_.f_46441_.m_188503_(10) > 5 && this.resurrectionProgress % 20 == 0) {
               this.generateLightning();
            }

            if (this.resurrectionProgress > 500) {
               this.resurrectionProgress = 0;
               this.createEntityWithoutActiveEffects(this.f_58857_, true)
                  .ifPresent(
                     entity -> {
                        this.generateLightning();
                        entity.m_20095_();
                        entity.m_20091_();
                        entity.m_6034_(this.f_58858_.m_123341_() + 0.5F, this.f_58858_.m_123342_() + 0.5F, this.f_58858_.m_123343_() + 0.5F);
                        if (entity instanceof LivingEntity l) {
                           l.m_21153_(l.m_21233_());
                           l.f_19789_ = 0.0F;
                           l.f_20919_ = 0;
                        }

                        if (entity instanceof AgeableMob mob) {
                           mob.m_146762_(mob.m_146764_());
                        }

                        boolean alreadySpawned = false;
                        if (this.cure && entity instanceof ZombieVillager zombie) {
                           entity = zombie.m_21406_(EntityType.f_20492_, true);
                           alreadySpawned = true;
                        }

                        if (entity instanceof CompassionateEntity<?> compassionateEntity) {
                           compassionateEntity.getRelationships().getFamilyEntry().setDeceased(false);
                        }

                        if (entity instanceof Infectable infectable) {
                           infectable.setInfectionProgress(
                              this.cure ? 0.0F : Math.max(Mth.m_14179_(this.f_58857_.f_46441_.m_188501_(), 0.2F, 0.6F), infectable.getInfectionProgress())
                           );
                        }

                        if (!alreadySpawned) {
                           this.f_58857_.m_7967_(entity);
                        }
                     }
                  );
            }
         }
      }

      private void generateLightning() {
         this.f_58857_.m_6580_(10);
         LightningBolt bolt = (LightningBolt)EntityType.f_20465_.m_20615_(this.f_58857_);
         bolt.m_20874_(true);
         bolt.m_20248_(this.f_58858_.m_123341_() + 0.5F, this.f_58858_.m_123342_(), this.f_58858_.m_123343_() + 0.5F);
         this.f_58857_.m_7967_(bolt);
      }

      public void setEntity(@Nullable Entity entity) {
         this.entityData = Optional.ofNullable(entity)
            .map(
               e -> new TombstoneBlock.Data.EntityData(
                  this.writeEntityToNbt(e), e.m_7755_(), EntityRelationship.of(e).map(EntityRelationship::getGender).orElse(Gender.MALE)
               )
            );
         this.computedName = null;
         this.m_6596_();
         if (this.m_58898_()) {
            this.f_58857_
               .m_6263_(
                  null, this.f_58858_.m_123341_(), this.f_58858_.m_123342_(), this.f_58858_.m_123343_(), SoundEvents.f_11699_, SoundSource.BLOCKS, 1.0F, 1.0F
               );
            this.f_58857_.m_46796_(2001, this.f_58858_, Block.m_49956_(this.m_58900_()));
            this.f_58857_.m_220407_(GameEvent.f_157792_, this.f_58858_, Context.m_223722_(this.m_58900_()));
            ((TombstoneBlock)this.m_58900_().m_60734_()).updateNeighbors(this.m_58900_(), this.f_58857_, this.f_58858_);
            if (!this.f_58857_.f_46443_) {
               GraveyardManager.get((ServerLevel)this.f_58857_)
                  .setTombstoneState(this.f_58858_, this.hasEntity() ? GraveyardManager.TombstoneState.FILLED : GraveyardManager.TombstoneState.EMPTY);
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

      public Optional<Component> getEntityName() {
         return this.entityData.map(e -> e.name);
      }

      public FlowingText getOrCreateEntityName(Function<Component, FlowingText> factory) {
         if (this.computedName == null) {
            this.computedName = factory.apply(this.getEntityName().orElse(Component.m_237113_("")));
         }

         return this.computedName;
      }

      public Optional<Entity> createEntity(Level world, boolean remove) {
         return this.createEntity(world, remove, Function.identity());
      }

      private Optional<Entity> createEntityWithoutActiveEffects(Level world, boolean remove) {
         return this.createEntity(world, remove, this::withoutActiveEffects);
      }

      private Optional<Entity> createEntity(Level world, boolean remove, Function<CompoundTag, CompoundTag> nbtTransform) {
         try {
            return this.entityData.flatMap(data -> EntityType.m_20642_(nbtTransform.apply(data.nbt), world));
         } finally {
            if (remove) {
               this.setEntity(null);
            }
         }
      }

      private CompoundTag withoutActiveEffects(CompoundTag nbt) {
         CompoundTag copy = nbt.m_6426_();
         copy.m_128473_("ActiveEffects");
         return copy;
      }

      private CompoundTag writeEntityToNbt(Entity entity) {
         CompoundTag nbt = new CompoundTag();
         entity.m_20240_(nbt);
         nbt.m_128359_("id", EntityType.m_20613_(entity.m_6095_()).toString());
         return nbt;
      }

      protected void sync() {
         this.m_6596_();
         this.f_58857_.m_7260_(this.m_58899_(), this.m_58900_(), this.m_58900_(), 3);
      }

      public void m_142466_(CompoundTag tag) {
         this.entityData = tag.m_128425_("entityData", 10) ? Optional.of(new TombstoneBlock.Data.EntityData(tag)) : Optional.empty();
         this.resurrectionProgress = tag.m_128451_("resurrectionProgress");
         this.cure = tag.m_128471_("cure");
      }

      public void m_183515_(CompoundTag nbt) {
         this.entityData.ifPresent(data -> data.writeNbt(nbt));
         nbt.m_128405_("resurrectionProgress", this.resurrectionProgress);
         nbt.m_128379_("cure", this.cure);
      }

      public CompoundTag m_5995_() {
         CompoundTag tag = new CompoundTag();
         this.m_183515_(tag);
         return tag;
      }

      public ClientboundBlockEntityDataPacket toUpdatePacket() {
         return ClientboundBlockEntityDataPacket.m_195642_(this, BlockEntity::m_5995_);
      }

      public void readFromStack(ItemStack stack) {
         this.entityData = Optional.ofNullable(stack).map(s -> s.m_41737_("entityData")).map(TombstoneBlock.Data.EntityData::new);
      }

      public void writeToStack(ItemStack stack) {
         this.entityData
            .ifPresent(
               data -> {
                  data.writeNbt(stack.m_41698_("entityData"));
                  this.getEntityName()
                     .ifPresent(
                        name -> ((ListTag)NbtHelper.computeIfAbsent(stack.m_41698_("display"), "Lore", 9, ListTag::new))
                           .add(0, StringTag.m_129297_(name.getString()))
                     );
               }
            );
      }

      public static Optional<TombstoneBlock.Data> of(@Nullable BlockEntity be) {
         return Optional.ofNullable(be).filter(p -> p instanceof TombstoneBlock.Data).map(TombstoneBlock.Data.class::cast);
      }

      static final class EntityData {
         private final CompoundTag nbt;
         private final Component name;
         private final Gender gender;

         public EntityData(CompoundTag nbt, Component name, Gender gender) {
            this.nbt = nbt;
            this.name = name == null ? Component.m_237113_("") : Component.m_237113_(name.getString());
            this.gender = gender;
         }

         EntityData(CompoundTag nbt) {
            this(nbt.m_128469_("entityData"), Serializer.m_130701_(nbt.m_128461_("entityName")), Gender.byId(nbt.m_128451_("entityGender")));
         }

         void writeNbt(CompoundTag nbt) {
            nbt.m_128365_("entityData", this.nbt);
            nbt.m_128359_("entityName", Serializer.m_130703_(this.name));
            nbt.m_128405_("entityGender", this.gender.ordinal());
         }
      }
   }
}
