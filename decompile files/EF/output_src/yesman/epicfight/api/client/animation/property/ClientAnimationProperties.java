package yesman.epicfight.api.client.animation.property;

import java.util.List;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.Layer;

public class ClientAnimationProperties {
   public static final AnimationProperty.StaticAnimationProperty<Layer.LayerType> LAYER_TYPE = new AnimationProperty.StaticAnimationProperty<>();
   public static final AnimationProperty.StaticAnimationProperty<Layer.Priority> PRIORITY = new AnimationProperty.StaticAnimationProperty<>();
   public static final AnimationProperty.StaticAnimationProperty<JointMaskEntry> JOINT_MASK = new AnimationProperty.StaticAnimationProperty<>();
   public static final AnimationProperty.StaticAnimationProperty<List<TrailInfo>> TRAIL_EFFECT = new AnimationProperty.StaticAnimationProperty<>();
   public static final AnimationProperty.StaticAnimationProperty<DirectStaticAnimation> POV_ANIMATION = new AnimationProperty.StaticAnimationProperty<>();
   public static final AnimationProperty.StaticAnimationProperty<AnimationSubFileReader.PovSettings> POV_SETTINGS = new AnimationProperty.StaticAnimationProperty<>();
   public static final AnimationProperty.StaticAnimationProperty<DirectStaticAnimation> MULTILAYER_ANIMATION = new AnimationProperty.StaticAnimationProperty<>();
}
