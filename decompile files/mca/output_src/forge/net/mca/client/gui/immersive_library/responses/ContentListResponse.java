package forge.net.mca.client.gui.immersive_library.responses;

import forge.net.mca.client.gui.immersive_library.types.LiteContent;

public record ContentListResponse(LiteContent[] contents) implements Response {
}
