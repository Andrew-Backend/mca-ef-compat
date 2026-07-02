package forge.net.mca.client.gui.immersive_library.types;

import java.util.Set;

public record Content(int contentid, int userid, String username, int likes, Set<String> tags, String title, int version, String meta, String data)
   implements Tagged {
}
