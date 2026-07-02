package yesman.epicfight.api.client.animation.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.StringUtil;
import java.util.Locale;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;

public record TrailInfo(
   Vec3 start,
   Vec3 end,
   String joint,
   SimpleParticleType particle,
   float startTime,
   float endTime,
   float fadeTime,
   float rCol,
   float gCol,
   float bCol,
   int interpolateCount,
   int trailLifetime,
   int updateInterval,
   int blockLight,
   int skyLight,
   ResourceLocation texturePath,
   InteractionHand hand
) {
   public static final ResourceLocation GENERIC_TRAIL_TEXTURE = EpicFightMod.identifier("textures/particle/swing_trail.png");
   public static final ResourceLocation SWORDMASTER_SWING_TRAIL_TEX = EpicFightMod.identifier("textures/particle/swordmaster_trail.png");
   public static final TrailInfo PREVIEWER_DEFAULT_TRAIL = builder()
      .startPos(new Vec3(0.0, 0.0, 0.0))
      .endPos(new Vec3(0.0, 0.0, -1.0))
      .interpolations(4)
      .lifetime(4)
      .r(0.75F)
      .g(0.75F)
      .b(0.75F)
      .texture(GENERIC_TRAIL_TEXTURE)
      .type((SimpleParticleType)EpicFightParticles.SWING_TRAIL.get())
      .create();
   public static final TrailInfo ANIMATION_DEFAULT_TRAIL = builder().time(0.1F, 0.2F).joint("Tool_R").itemSkinHand(InteractionHand.MAIN_HAND).create();

   private TrailInfo(TrailInfo.Builder builder) {
      this(
         builder.start,
         builder.end,
         builder.joint,
         builder.particle,
         builder.startTime,
         builder.endTime,
         builder.fadeTime,
         builder.rCol,
         builder.gCol,
         builder.bCol,
         builder.interpolateCount,
         builder.trailLifetime,
         builder.updateInterval,
         builder.blockLight,
         builder.skyLight,
         builder.texturePath,
         builder.hand
      );
   }

   public TrailInfo.Builder unpackAsBuilder() {
      return new TrailInfo.Builder()
         .startPos(this.start)
         .endPos(this.end)
         .joint(this.joint)
         .type(this.particle)
         .time(this.startTime, this.endTime)
         .fadeTime(this.fadeTime)
         .r(this.rCol)
         .g(this.gCol)
         .b(this.bCol)
         .interpolations(this.interpolateCount)
         .lifetime(this.trailLifetime)
         .updateInterval(this.updateInterval)
         .blockLight(this.blockLight)
         .skyLight(this.skyLight)
         .texture(this.texturePath)
         .itemSkinHand(this.hand);
   }

   public TrailInfo overwrite(TrailInfo trailInfo) {
      boolean validTime = isValidTime(trailInfo.startTime) && isValidTime(trailInfo.endTime);
      boolean validColor = trailInfo.rCol >= 0.0F && trailInfo.gCol >= 0.0F && trailInfo.bCol >= 0.0F;
      TrailInfo.Builder builder = new TrailInfo.Builder();
      builder.startPos(trailInfo.start == null ? this.start : trailInfo.start);
      builder.endPos(trailInfo.end == null ? this.end : trailInfo.end);
      builder.joint(trailInfo.joint == null ? this.joint : trailInfo.joint);
      builder.type(trailInfo.particle == null ? this.particle : trailInfo.particle);
      builder.time(!validTime ? this.startTime : trailInfo.startTime, !validTime ? this.endTime : trailInfo.endTime);
      builder.fadeTime(!isValidTime(trailInfo.fadeTime) ? this.fadeTime : trailInfo.fadeTime);
      builder.r(!validColor ? this.rCol : trailInfo.rCol);
      builder.g(!validColor ? this.gCol : trailInfo.gCol);
      builder.b(!validColor ? this.bCol : trailInfo.bCol);
      builder.blockLight(Math.max(this.blockLight, trailInfo.blockLight));
      builder.skyLight(Math.max(this.skyLight, trailInfo.skyLight));
      builder.interpolations(trailInfo.interpolateCount < 0 ? this.interpolateCount : trailInfo.interpolateCount);
      builder.lifetime(trailInfo.trailLifetime < 0 ? this.trailLifetime : trailInfo.trailLifetime);
      builder.texture(trailInfo.texturePath == null ? this.texturePath : trailInfo.texturePath);
      builder.itemSkinHand(trailInfo.hand == null ? this.hand : trailInfo.hand);
      return builder.create();
   }

   public static boolean isValidTime(float time) {
      return !Float.isNaN(time) && time >= 0.0F;
   }

   public boolean playable() {
      return this.particle() == EpicFightParticles.SWING_TRAIL.get()
         ? this.start != null
            && this.end != null
            && this.particle != null
            && this.updateInterval > 0
            && !StringUtil.isNullOrEmpty(this.joint)
            && isValidTime(this.startTime)
            && isValidTime(this.endTime)
            && this.interpolateCount > 0
            && this.trailLifetime > 0
            && this.texturePath != null
         : this.start != null
            && this.end != null
            && this.particle != null
            && this.updateInterval > 0
            && this.interpolateCount > 0
            && this.trailLifetime > 0
            && this.texturePath != null;
   }

   public static TrailInfo.Builder builder() {
      return new TrailInfo.Builder();
   }

   public static TrailInfo deserialize(JsonElement json) {
      JsonObject trailObj = json.getAsJsonObject();
      TrailInfo.Builder trailBuilder = builder();
      if (trailObj.has("start_time") && trailObj.has("end_time")) {
         float startTime = GsonHelper.m_13915_(trailObj, "start_time");
         float endTime = GsonHelper.m_13915_(trailObj, "end_time");
         trailBuilder.time(startTime, endTime);
      }

      if (trailObj.has("fade_time")) {
         float fadeTime = trailObj.get("fade_time").getAsFloat();
         trailBuilder.fadeTime(fadeTime);
      }

      if (trailObj.has("lifetime")) {
         trailBuilder.lifetime(GsonHelper.m_13927_(trailObj, "lifetime"));
      }

      if (trailObj.has("interpolations")) {
         trailBuilder.interpolations(GsonHelper.m_13927_(trailObj, "interpolations"));
      }

      if (trailObj.has("joint")) {
         trailBuilder.joint(GsonHelper.m_13906_(trailObj, "joint"));
      }

      if (trailObj.has("texture_path")) {
         trailBuilder.texture(GsonHelper.m_13906_(trailObj, "texture_path"));
      }

      if (trailObj.has("particle_type")) {
         String particleTypeName = GsonHelper.m_13906_(trailObj, "particle_type");
         SimpleParticleType particleType = (SimpleParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(particleTypeName));
         trailBuilder.type(particleType);
      }

      if (trailObj.has("color")) {
         JsonArray color = trailObj.get("color").getAsJsonArray();
         Vec3f colorVec = ParseUtil.toVector3f(color);
         trailBuilder.r(colorVec.x / 255.0F);
         trailBuilder.g(colorVec.y / 255.0F);
         trailBuilder.b(colorVec.z / 255.0F);
      }

      if (trailObj.has("begin_pos")) {
         JsonArray beginPos = trailObj.get("begin_pos").getAsJsonArray();
         Vec3 begin = ParseUtil.toVector3d(beginPos);
         trailBuilder.startPos(begin);
      }

      if (trailObj.has("end_pos")) {
         JsonArray endPos = trailObj.get("end_pos").getAsJsonArray();
         Vec3 end = ParseUtil.toVector3d(endPos);
         trailBuilder.endPos(end);
      }

      if (trailObj.has("item_skin_hand")) {
         String itemSkinHand = trailObj.get("item_skin_hand").getAsString();
         InteractionHand hand = InteractionHand.valueOf(itemSkinHand.toUpperCase(Locale.ROOT));
         trailBuilder.itemSkinHand(hand);
      }

      if (trailObj.has("block_light")) {
         int blockLight = trailObj.get("block_light").getAsInt();
         trailBuilder.blockLight(blockLight);
      }

      if (trailObj.has("sky_light")) {
         int skyLight = trailObj.get("sky_light").getAsInt();
         trailBuilder.skyLight(skyLight);
      }

      return trailBuilder.create();
   }

   public static TrailInfo deserialize(CompoundTag compoundTag) {
      TrailInfo.Builder trailBuilder = builder();
      if (compoundTag.m_128441_("start_time") && compoundTag.m_128441_("end_time")) {
         float startTime = compoundTag.m_128457_("start_time");
         float endTime = compoundTag.m_128457_("end_time");
         trailBuilder.time(startTime, endTime);
      }

      if (compoundTag.m_128441_("fade_time")) {
         float fadeTime = compoundTag.m_128457_("fade_time");
         trailBuilder.fadeTime(fadeTime);
      }

      if (compoundTag.m_128441_("lifetime")) {
         trailBuilder.lifetime(compoundTag.m_128451_("lifetime"));
      }

      if (compoundTag.m_128441_("interpolations")) {
         trailBuilder.interpolations(compoundTag.m_128451_("interpolations"));
      }

      if (compoundTag.m_128441_("update_interval")) {
         trailBuilder.updateInterval(compoundTag.m_128451_("update_interval"));
      }

      if (compoundTag.m_128441_("joint")) {
         trailBuilder.joint(compoundTag.m_128461_("joint"));
      }

      if (compoundTag.m_128441_("texture_path")) {
         trailBuilder.texture(compoundTag.m_128461_("texture_path"));
      }

      if (compoundTag.m_128441_("particle_type")) {
         String particleTypeName = compoundTag.m_128461_("particle_type");
         SimpleParticleType particleType = (SimpleParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(particleTypeName));
         trailBuilder.type(particleType);
      }

      if (compoundTag.m_128441_("color")) {
         ListTag color = compoundTag.m_128437_("color", 3);
         trailBuilder.r(color.m_128763_(0) / 255.0F);
         trailBuilder.g(color.m_128763_(1) / 255.0F);
         trailBuilder.b(color.m_128763_(2) / 255.0F);
      }

      if (compoundTag.m_128441_("begin_pos")) {
         ListTag beginPos = compoundTag.m_128437_("begin_pos", 6);
         trailBuilder.startPos(new Vec3(beginPos.m_128772_(0), beginPos.m_128772_(1), beginPos.m_128772_(2)));
      }

      if (compoundTag.m_128441_("end_pos")) {
         ListTag endPos = compoundTag.m_128437_("end_pos", 6);
         trailBuilder.endPos(new Vec3(endPos.m_128772_(0), endPos.m_128772_(1), endPos.m_128772_(2)));
      }

      if (compoundTag.m_128441_("block_light")) {
         int blockLight = compoundTag.m_128451_("block_light");
         trailBuilder.blockLight(blockLight);
      }

      if (compoundTag.m_128441_("sky_light")) {
         int skyLight = compoundTag.m_128451_("sky_light");
         trailBuilder.skyLight(skyLight);
      }

      if (compoundTag.m_128441_("item_skin_hand")) {
         String itemSkinHand = compoundTag.m_128461_("item_skin_hand");
         InteractionHand hand = InteractionHand.valueOf(itemSkinHand.toUpperCase(Locale.ROOT));
         trailBuilder.itemSkinHand(hand);
      }

      return trailBuilder.create();
   }

   public static class Builder {
      private Vec3 start;
      private Vec3 end;
      private SimpleParticleType particle;
      private String joint;
      private float startTime = Float.NaN;
      private float endTime = Float.NaN;
      private float fadeTime = Float.NaN;
      private float rCol = -1.0F;
      private float gCol = -1.0F;
      private float bCol = -1.0F;
      private int interpolateCount = -1;
      private int trailLifetime = -1;
      private int updateInterval = 1;
      private int blockLight;
      private int skyLight;
      private ResourceLocation texturePath;
      private InteractionHand hand = InteractionHand.MAIN_HAND;

      public TrailInfo.Builder startPos(Vec3 start) {
         this.start = start;
         return this;
      }

      public TrailInfo.Builder endPos(Vec3 end) {
         this.end = end;
         return this;
      }

      public TrailInfo.Builder type(SimpleParticleType particle) {
         this.particle = particle;
         return this;
      }

      public TrailInfo.Builder joint(String joint) {
         this.joint = joint;
         return this;
      }

      public TrailInfo.Builder time(float startTime, float endTime) {
         this.startTime = startTime;
         this.endTime = endTime;
         return this;
      }

      public TrailInfo.Builder fadeTime(float fadeTime) {
         this.fadeTime = fadeTime;
         return this;
      }

      public TrailInfo.Builder r(float rCol) {
         this.rCol = rCol;
         return this;
      }

      public TrailInfo.Builder g(float gCol) {
         this.gCol = gCol;
         return this;
      }

      public TrailInfo.Builder b(float bCol) {
         this.bCol = bCol;
         return this;
      }

      public TrailInfo.Builder interpolations(int interpolateCount) {
         this.interpolateCount = interpolateCount;
         return this;
      }

      public TrailInfo.Builder lifetime(int trailLifetime) {
         this.trailLifetime = trailLifetime;
         return this;
      }

      public TrailInfo.Builder updateInterval(int updateInterval) {
         this.updateInterval = updateInterval;
         return this;
      }

      public TrailInfo.Builder blockLight(int blockLight) {
         this.blockLight = blockLight;
         return this;
      }

      public TrailInfo.Builder skyLight(int skyLight) {
         this.skyLight = skyLight;
         return this;
      }

      public TrailInfo.Builder texture(String texturePath) {
         this.texturePath = ResourceLocation.parse(texturePath);
         return this;
      }

      public TrailInfo.Builder texture(ResourceLocation texturePath) {
         this.texturePath = texturePath;
         return this;
      }

      public TrailInfo.Builder itemSkinHand(InteractionHand itemSkinHand) {
         this.hand = itemSkinHand;
         return this;
      }

      public TrailInfo create() {
         return new TrailInfo(this);
      }
   }
}
