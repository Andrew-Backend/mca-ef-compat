package forge.net.mca.client.gui.immersive_library.types;

import java.util.Set;

public record LiteContent(int contentid, int userid, String username, int likes, Set<String> tags, String title, int version) implements Tagged {
}
