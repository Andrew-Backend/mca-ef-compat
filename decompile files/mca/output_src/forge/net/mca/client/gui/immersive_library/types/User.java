package forge.net.mca.client.gui.immersive_library.types;

import java.util.List;

public record User(int userid, String username, int likes_received, List<LiteContent> likes, List<LiteContent> submissions, boolean moderator) {
}
