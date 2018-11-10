package net.codevmc.wm;

import io.hekate.core.Hekate;
import io.hekate.core.HekateBootstrap;
import io.hekate.core.HekateFutureException;

public class ClusterManager {

    private final HekateBootstrap bootstrap;
    private final WorldManager worldManager;
    private Hekate hekate;

    public ClusterManager(String clusterName, String nodeName, WorldManager worldManager) {
        this.worldManager = worldManager;
        bootstrap = new HekateBootstrap();
        bootstrap.withClusterName(clusterName).withNodeName(nodeName).withLifecycleListener(hekate -> {
            if(hekate.state() == Hekate.State.UP) ClusterManager.this.hekate = hekate;
        });
    }


    public void instantiate() throws HekateFutureException {
        try {
            bootstrap.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Hekate getHekate() {
        return hekate;
    }



}
