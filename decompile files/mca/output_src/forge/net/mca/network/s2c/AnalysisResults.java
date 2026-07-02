package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.resources.data.analysis.Analysis;

public class AnalysisResults implements Message {
   private static final long serialVersionUID = 2451914344295985363L;
   public final Analysis<?> analysis;

   public AnalysisResults(Analysis<?> analysis) {
      this.analysis = analysis;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleSkinListResponse(this);
   }
}
