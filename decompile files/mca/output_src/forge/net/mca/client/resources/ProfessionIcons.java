package forge.net.mca.client.resources;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ProfessionIcons {
   public static final Map<String, ItemStack> ICONS = new HashMap<>();

   static {
      ICONS.put(null, Items.f_42410_.m_7968_());
      ICONS.put("nitwit", Items.f_42618_.m_7968_());
      ICONS.put("armorer", Items.f_42770_.m_7968_());
      ICONS.put("butcher", Items.f_42769_.m_7968_());
      ICONS.put("cartographer", Items.f_42771_.m_7968_());
      ICONS.put("cleric", Items.f_42543_.m_7968_());
      ICONS.put("farmer", Items.f_42726_.m_7968_());
      ICONS.put("fisherman", Items.f_42768_.m_7968_());
      ICONS.put("fletcher", Items.f_42772_.m_7968_());
      ICONS.put("leatherworker", Items.f_42544_.m_7968_());
      ICONS.put("librarian", Items.f_42774_.m_7968_());
      ICONS.put("mason", Items.f_42776_.m_7968_());
      ICONS.put("shepherd", Items.f_42719_.m_7968_());
      ICONS.put("toolsmith", Items.f_42775_.m_7968_());
      ICONS.put("weaponsmith", Items.f_42773_.m_7968_());
      ICONS.put("mca.outlaw", Items.f_42728_.m_7968_());
      ICONS.put("mca.guard", Items.f_42383_.m_7968_());
      ICONS.put("mca.archer", Items.f_42411_.m_7968_());
      ICONS.put("mca.adventurer", Items.f_42676_.m_7968_());
      ICONS.put("mca.mercenary", Items.f_42616_.m_7968_());
      ICONS.put("mca.cultist", Items.f_42517_.m_7968_());
   }
}
