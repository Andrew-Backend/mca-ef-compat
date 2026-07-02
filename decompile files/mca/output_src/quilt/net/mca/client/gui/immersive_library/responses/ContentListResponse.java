package quilt.net.mca.client.gui.immersive_library.responses;

import quilt.net.mca.client.gui.immersive_library.types.LiteContent;

public record ContentListResponse(LiteContent[] contents) implements Response {
}
