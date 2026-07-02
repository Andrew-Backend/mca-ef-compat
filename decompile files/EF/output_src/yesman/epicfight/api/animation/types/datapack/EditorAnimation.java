package yesman.epicfight.api.animation.types.datapack;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.AirSlashAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.api.animation.types.DashAttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.HitAnimation;
import yesman.epicfight.api.animation.types.KnockdownAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class EditorAnimation extends StaticAnimation implements AnimationManager.AnimationAccessor<EditorAnimation> {
   private EditorAnimation.AnimationType animationType;
   private AnimationClip animationClip;
   private Map<String, Object> constructorParams = Maps.newLinkedHashMap();
   private JsonArray rawAnimation;
   private JsonObject properties = new JsonObject();
   private static final Map<EditorAnimation.AnimationType, Map<String, Class<?>>> PARAMETERS = Maps.newHashMap();
   private static final Map<EditorAnimation.AnimationType, Class<? extends DatapackAnimation<? extends StaticAnimation>>> FAKE_ANIMATIONS = Maps.newHashMap();

   public EditorAnimation(String path, AssetAccessor<? extends Armature> armature, AnimationClip clip, JsonArray rawAnimation) {
      super(ResourceLocation.withDefaultNamespace(""), 0.0F, false, "", armature);
      this.animationClip = clip;
      this.rawAnimation = rawAnimation;
      this.constructorParams.put("path", path);
      this.constructorParams.put("armature", armature);
   }

   public <T> T getParameter(String key) {
      return (T)this.constructorParams.get(key);
   }

   public void setParameter(String key, Object value) {
      if (this.constructorParams.containsKey(key)) {
         this.constructorParams.put(key, value);
      } else {
         throw new IllegalStateException("No key " + key);
      }
   }

   public EditorAnimation.AnimationType getAnimationClass() {
      return this.animationType;
   }

   public JsonObject getPropertiesJson() {
      return this.properties;
   }

   public void setAnimationClass(EditorAnimation.AnimationType animationClass) {
      String prevPath = this.getParameter("path");
      this.constructorParams.clear();
      PARAMETERS.get(animationClass).keySet().forEach(k -> this.constructorParams.put(k, null));
      this.setParameter("armature", this.getArmature());
      this.setParameter("path", prevPath);
      if (AttackAnimation.class.isAssignableFrom(animationClass.getAnimationClass())) {
         this.setParameter("phases", new ListTag());
      }

      this.animationType = animationClass;
   }

   @Override
   public float getTransitionTime() {
      Object convTime = this.getParameter("convertTime");
      return convTime == null ? 0.0F : (Float)convTime;
   }

   @Override
   public AnimationClip getAnimationClip() {
      return this.animationClip;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return ResourceLocation.parse((String)this.constructorParams.get("path"));
   }

   @Override
   public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
      animationPlayer.setPlayAnimation(this.getAccessor());
      animationPlayer.tick(entitypatch);
   }

   public JsonArray getRawAnimationJson() {
      return this.rawAnimation;
   }

   public String getInvocationCommand() throws Exception {
      if (this.animationType == null) {
         throw new IllegalStateException("Animation type is not defined.");
      }

      switch (this.animationType) {
         case STATIC:
         case MOVEMENT:
            return String.format(
               "(%s#F,%b#Z,%s#java.lang.String,%s#" + Armature.class.getTypeName() + ")#%s",
               this.constructorParams.get("convertTime"),
               this.constructorParams.get("isRepeat"),
               this.constructorParams.get("path"),
               this.constructorParams.get("armature"),
               this.animationType.animCls.getTypeName()
            );
         case SHORT_HIT:
         case LONG_HIT:
         case KNOCK_DOWN:
            return String.format(
               "(%s#F,%s#java.lang.String,%s#" + Armature.class.getTypeName() + ")#%s",
               this.constructorParams.get("convertTime"),
               this.constructorParams.get("path"),
               this.constructorParams.get("armature"),
               this.animationType.animCls.getTypeName()
            );
         case ATTACK:
         case BASIC_ATTACK:
         case DASH_ATTACK:
         case AIR_SLASH:
            ListTag phasesTag = this.getParameter("phases");
            StringBuilder sb = new StringBuilder("[");
            float start = 0.0F;

            for (int i = 0; i < phasesTag.size(); i++) {
               CompoundTag phaseCompound = phasesTag.m_128728_(i);
               float antic = phaseCompound.m_128457_("antic");
               float preDelay = phaseCompound.m_128457_("preDelay");
               float contact = phaseCompound.m_128457_("contact");
               float recovery = phaseCompound.m_128457_("recovery");
               float end;
               if (i < phasesTag.size() - 1) {
                  CompoundTag nextTag = phasesTag.m_128728_(i + 1);
                  end = nextTag.m_128457_("antic");
               } else {
                  end = recovery;
               }

               String hand = phaseCompound.m_128461_("hand");
               String joint = phaseCompound.m_128461_("joint");
               String colliderInvokeCommand;
               if (phaseCompound.m_128441_("collider")) {
                  CompoundTag colliderTag = phaseCompound.m_128469_("collider");
                  int colliderCount = colliderTag.m_128451_("number");
                  ListTag center = colliderTag.m_128437_("center", 6);
                  ListTag size = colliderTag.m_128437_("size", 6);
                  if (colliderCount == 1) {
                     colliderInvokeCommand = String.format(
                        "(%s#D,%s#D,%s#D,%s#D,%s#D,%s#D)#%s",
                        size.get(0),
                        size.get(1),
                        size.get(2),
                        center.get(0),
                        center.get(1),
                        center.get(2),
                        OBBCollider.class.getTypeName()
                     );
                  } else {
                     colliderInvokeCommand = String.format(
                        "(%d#I,%s#D,%s#D,%s#D,%s#D,%s#D,%s#D)#%s",
                        colliderCount,
                        size.get(0),
                        size.get(1),
                        size.get(2),
                        center.get(0),
                        center.get(1),
                        center.get(2),
                        MultiOBBCollider.class.getTypeName()
                     );
                  }
               } else {
                  colliderInvokeCommand = "null#" + Collider.class.getTypeName();
               }

               sb.append(
                  String.format(
                     "(%s#F,%s#F,%s#F,%s#F,%s#F,%s#F,%s#net.minecraft.world.InteractionHand,%s#" + Joint.class.getTypeName() + ",%s)",
                     start,
                     antic,
                     preDelay,
                     contact,
                     recovery,
                     end,
                     hand,
                     joint,
                     colliderInvokeCommand
                  )
               );
               if (i < phasesTag.size() - 1) {
                  sb.append(",");
                  start = end;
               }
            }

            sb.append("]#" + AttackAnimation.Phase.class.getTypeName());
            return String.format(
               "(%s#F,%s#java.lang.String,%s#" + Armature.class.getTypeName() + ",%s)#%s",
               this.constructorParams.get("convertTime"),
               this.constructorParams.get("path"),
               this.constructorParams.get("armature"),
               sb.toString(),
               this.animationType.animCls.getTypeName()
            );
         default:
            throw new IllegalStateException("Invalid animation type: " + this.animationType);
      }
   }

   public EditorAnimation deepCopy() {
      EditorAnimation fakeAnimation = new EditorAnimation(this.getParameter("path"), this.armature, this.animationClip, this.rawAnimation);
      fakeAnimation.animationType = this.animationType;
      fakeAnimation.constructorParams.clear();
      fakeAnimation.constructorParams.putAll(this.constructorParams);
      fakeAnimation.rawAnimation = this.rawAnimation;
      fakeAnimation.properties = this.properties;
      return fakeAnimation;
   }

   public DatapackAnimation<? extends StaticAnimation> createAnimation() throws Throwable {
      try {
         if (this.animationType == null) {
            throw new IllegalStateException("Animation type is not defined.");
         }

         Map<String, Class<?>> map = PARAMETERS.get(this.animationType);
         Class[] paramClasses = map.values().toArray(new Class[0]);
         Object[] params = this.constructorParams.values().toArray();
         Constructor<? extends DatapackAnimation> constructor = switchType(this.animationType).getConstructor(paramClasses);
         DatapackAnimation<? extends StaticAnimation> animation = (DatapackAnimation<? extends StaticAnimation>)constructor.newInstance(params);
         animation.setAnimationClip(this.animationClip);
         animation.setCreator(this);
         AnimationSubFileReader.SUBFILE_CLIENT_PROPERTY.apply(this.properties, animation.get());
         return animation;
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
         StringBuilder sb = new StringBuilder();
         Iterator<Entry<String, Object>> iter = this.constructorParams.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<String, Object> entry = iter.next();
            sb.append(
               String.format(
                  iter.hasNext() ? "%s(%s:%s), " : "%s(%s:%s)",
                  entry.getKey(),
                  entry.getValue(),
                  entry.getValue() == null ? null : entry.getValue().getClass().getSimpleName()
               )
            );
         }

         throw new IllegalArgumentException(
            String.format("Invalid arguments for %s: %s", ParseUtil.snakeToSpacedCamel(this.animationType.toString()), sb.toString())
         );
      } catch (InvocationTargetException e) {
         e.printStackTrace();
         throw e.getTargetException();
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   public static Class<? extends DatapackAnimation<? extends StaticAnimation>> switchType(EditorAnimation.AnimationType cls) {
      return FAKE_ANIMATIONS.get(cls);
   }

   public static Class<? extends DatapackAnimation<? extends StaticAnimation>> switchType(Class<? extends StaticAnimation> cls) {
      for (EditorAnimation.AnimationType animType : EditorAnimation.AnimationType.values()) {
         if (animType.animCls == cls) {
            return FAKE_ANIMATIONS.get(animType);
         }
      }

      return DatapackStaticAnimation.class;
   }

   public EditorAnimation get() {
      return this;
   }

   @Override
   public ResourceLocation registryName() {
      return this.getRegistryName();
   }

   @Override
   public boolean isPresent() {
      return true;
   }

   @Override
   public int id() {
      return -1;
   }

   @Override
   public boolean inRegistry() {
      return false;
   }

   @Override
   public <A extends DynamicAnimation> AnimationManager.AnimationAccessor<A> getAccessor() {
      return this;
   }

   static {
      Map<String, Class<?>> staticAnimationParameters = Maps.newLinkedHashMap();
      staticAnimationParameters.put("convertTime", float.class);
      staticAnimationParameters.put("isRepeat", boolean.class);
      staticAnimationParameters.put("path", String.class);
      staticAnimationParameters.put("armature", AssetAccessor.class);
      Map<String, Class<?>> hitAnimationParameters = Maps.newLinkedHashMap();
      hitAnimationParameters.put("convertTime", float.class);
      hitAnimationParameters.put("path", String.class);
      hitAnimationParameters.put("armature", AssetAccessor.class);
      Map<String, Class<?>> attackAnimationParameters = Maps.newLinkedHashMap();
      attackAnimationParameters.put("convertTime", float.class);
      attackAnimationParameters.put("path", String.class);
      attackAnimationParameters.put("armature", AssetAccessor.class);
      attackAnimationParameters.put("phases", ListTag.class);
      PARAMETERS.put(EditorAnimation.AnimationType.STATIC, staticAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.MOVEMENT, staticAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.ATTACK, attackAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.BASIC_ATTACK, attackAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.DASH_ATTACK, attackAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.AIR_SLASH, attackAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.SHORT_HIT, hitAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.LONG_HIT, hitAnimationParameters);
      PARAMETERS.put(EditorAnimation.AnimationType.KNOCK_DOWN, hitAnimationParameters);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.STATIC, DatapackStaticAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.MOVEMENT, DatapackMovementAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.ATTACK, DatapackAttackAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.BASIC_ATTACK, DatapackBasicAttackAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.DASH_ATTACK, DatapackDashAttackAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.AIR_SLASH, DatapackAirSlashAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.SHORT_HIT, DatapackHitAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.LONG_HIT, DatapackLongHitAnimation.class);
      FAKE_ANIMATIONS.put(EditorAnimation.AnimationType.KNOCK_DOWN, DatapackKnockdownAnimation.class);
   }

   @OnlyIn(Dist.CLIENT)
   public enum AnimationType {
      STATIC(StaticAnimation.class),
      MOVEMENT(MovementAnimation.class),
      ATTACK(AttackAnimation.class),
      BASIC_ATTACK(BasicAttackAnimation.class),
      DASH_ATTACK(DashAttackAnimation.class),
      AIR_SLASH(AirSlashAnimation.class),
      SHORT_HIT(HitAnimation.class),
      LONG_HIT(LongHitAnimation.class),
      KNOCK_DOWN(KnockdownAnimation.class);

      final Class<? extends StaticAnimation> animCls;

      AnimationType(Class<? extends StaticAnimation> animCls) {
         this.animCls = animCls;
      }

      public Class<? extends StaticAnimation> getAnimationClass() {
         return this.animCls;
      }

      @Override
      public String toString() {
         return ParseUtil.snakeToSpacedCamel(this.name() + "_ANIMATION");
      }
   }
}
