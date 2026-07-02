package yesman.epicfight.api.client.forgeevent;

import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.client.gui.screen.SkillBookScreen;

public class AttributeIconRegisterEvent extends Event implements IModBusEvent {
   final Map<Attribute, SkillBookScreen.TextureInfo> registry;

   public AttributeIconRegisterEvent(Map<Attribute, SkillBookScreen.TextureInfo> registry) {
      this.registry = registry;
   }

   public void registerAttribute(Attribute attirubte, SkillBookScreen.TextureInfo textureInfo) {
      this.registry.put(attirubte, textureInfo);
   }
}
