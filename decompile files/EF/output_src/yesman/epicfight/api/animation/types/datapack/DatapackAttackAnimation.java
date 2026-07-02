package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class DatapackAttackAnimation extends AttackAnimation implements DatapackAnimation<DatapackAttackAnimation> {
   protected AnimationClip clip;
   protected EditorAnimation fakeAnimation;
   protected ResourceLocation registryName;

   private static AttackAnimation.Phase[] convertListTagToPhases(ListTag listTag, Armature armature) {
      float start = 0.0F;
      AttackAnimation.Phase[] phases = new AttackAnimation.Phase[listTag.size()];
      int i = 0;

      for (Tag phaseTag : listTag) {
         CompoundTag phaseCompTag = (CompoundTag)phaseTag;
         if (!phaseCompTag.m_128441_("antic")) {
            throw new NoSuchElementException("Phase" + i + ": Antic not specified");
         }

         if (!phaseCompTag.m_128441_("preDelay")) {
            throw new NoSuchElementException("Phase" + i + ": Pre-Delay not specified");
         }

         if (!phaseCompTag.m_128441_("contact")) {
            throw new NoSuchElementException("Phase" + i + ": Contact not specified");
         }

         if (!phaseCompTag.m_128441_("recovery")) {
            throw new NoSuchElementException("Phase" + i + ": Recovery not specified");
         }

         if (!phaseCompTag.m_128441_("hand")) {
            throw new NoSuchElementException("Phase" + i + ": Hand not specified");
         }

         if (!phaseCompTag.m_128441_("joint")) {
            throw new NoSuchElementException("Phase" + i + ": Joint not specified");
         }

         float antic = phaseCompTag.m_128457_("antic");
         float preDelay = phaseCompTag.m_128457_("preDelay");
         float contact = phaseCompTag.m_128457_("contact");
         float recovery = phaseCompTag.m_128457_("recovery");
         InteractionHand hand = InteractionHand.valueOf(phaseCompTag.m_128461_("hand").toUpperCase(Locale.ROOT));
         String armature$joint = phaseCompTag.m_128461_("joint");
         String joinName = armature$joint.substring(armature$joint.lastIndexOf(46) + 1);
         Joint joint = armature.searchJointByName(joinName);
         Collider collider = null;

         try {
            collider = ColliderPreset.deserializeSimpleCollider(phaseCompTag.m_128469_("collider"));
         } catch (Exception var18) {
         }

         phases[i] = new AttackAnimation.Phase(start, antic, preDelay, contact, recovery, recovery, hand, joint, collider);
         start = recovery;
         i++;
      }

      return phases;
   }

   public DatapackAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, ListTag phases) {
      super(convertTime, path, armature, convertListTagToPhases(phases, armature.get()));
      this.setRegistryName(ResourceLocation.parse(path));
      this.accessor = this;
   }

   public DatapackAttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, AttackAnimation.Phase... phases) {
      super(convertTime, path, armature, phases);
      this.setRegistryName(ResourceLocation.parse(path));
      this.accessor = this;
   }

   @Override
   public void setCreator(EditorAnimation fakeAnimation) {
      this.fakeAnimation = fakeAnimation;
   }

   @Override
   public EditorAnimation getCreator() {
      return this.fakeAnimation;
   }

   @Override
   public void setAnimationClip(AnimationClip clip) {
      this.clip = clip;
   }

   @Override
   public AnimationClip getAnimationClip() {
      return this.clip;
   }

   @Override
   public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
      animationPlayer.setPlayAnimation(this);
      animationPlayer.tick(entitypatch);
   }

   @Override
   public EditorAnimation readAnimationFromJson(JsonArray rawAnimationJson) {
      EditorAnimation fakeAnimation = new EditorAnimation(this.registryName().toString(), this.armature, this.clip, rawAnimationJson);
      fakeAnimation.setAnimationClass(EditorAnimation.AnimationType.ATTACK);
      fakeAnimation.setParameter("convertTime", this.transitionTime);
      fakeAnimation.setParameter("path", this.registryName().toString());
      fakeAnimation.setParameter("armature", this.armature);
      ListTag listTag = new ListTag();

      for (AttackAnimation.Phase phase : this.phases) {
         CompoundTag compTag = new CompoundTag();
         compTag.m_128350_("start", phase.start);
         compTag.m_128350_("antic", phase.antic);
         compTag.m_128350_("preDelay", phase.preDelay);
         compTag.m_128350_("contact", phase.contact);
         compTag.m_128350_("recovery", phase.recovery);
         compTag.m_128350_("end", phase.end);
         compTag.m_128359_("hand", phase.hand.toString());
         if (phase.colliders[0].getSecond() != null) {
            compTag.m_128365_("collider", ((Collider)phase.colliders[0].getSecond()).serialize(new CompoundTag()));
         }

         compTag.m_128359_("joint", this.armature.registryName() + "." + ((Joint)phase.colliders[0].getFirst()).getName());
         listTag.add(compTag);
      }

      fakeAnimation.setParameter("phases", listTag);
      this.getProperty(ClientAnimationProperties.TRAIL_EFFECT).ifPresent(trailInfos -> {
         JsonArray trailArray = new JsonArray();

         for (TrailInfo trailInfo : trailInfos) {
            JsonObject trailObj = new JsonObject();
            trailObj.addProperty("start_time", trailInfo.startTime());
            trailObj.addProperty("end_time", trailInfo.endTime());
            trailObj.addProperty("joint", trailInfo.joint());
            trailObj.addProperty("item_skin_hand", trailInfo.hand().toString());
            trailArray.add(trailObj);
         }

         fakeAnimation.getPropertiesJson().add("trail_effects", trailArray);
         fakeAnimation.addProperty(ClientAnimationProperties.TRAIL_EFFECT, (List<TrailInfo>)trailInfos);
      });
      this.fakeAnimation = fakeAnimation;
      return fakeAnimation;
   }

   public DatapackAttackAnimation get() {
      return this;
   }

   @Override
   public void setRegistryName(ResourceLocation registryName) {
      this.registryName = registryName;
   }

   @Override
   public ResourceLocation registryName() {
      return this.registryName;
   }

   @Override
   public boolean isPresent() {
      return true;
   }
}
