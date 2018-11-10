package net.codevmc.wm;

import io.hekate.core.Hekate;
import io.hekate.core.HekateBootstrap;
import io.hekate.core.HekateFutureException;
import io.hekate.messaging.MessagingChannelConfig;
import io.hekate.messaging.MessagingServiceFactory;

public class ClusterManager {

    private final HekateBootstrap bootstrap;
    private final WorldManager worldManager;
    private Hekate hekate;

    public ClusterManager(String clusterName, String nodeName, WorldManager worldManager) {
        this.worldManager = worldManager;
        bootstrap = new HekateBootstrap().withClusterName(clusterName).withNodeName(nodeName).withRole("world-manager").withLifecycleListener(hekate -> {
            if(hekate.state() == Hekate.State.UP) {
                if(hekate.cluster().forRemotes().forRole("world-manager").topology().size() > 0) {
                    System.out.println("Another world manager has been detected, disabling local...");
                    try {
                        hekate.leave();
                    } catch (InterruptedException | HekateFutureException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                ClusterManager.this.hekate = hekate;
            }
        }).withService(
                new MessagingServiceFactory()
                        .withChannel(MessagingChannelConfig.of(String.class).withName("home-join").withReceiver(new WorldTpReceiver(worldManager)))
                        .withChannel(MessagingChannelConfig.of(String.class).withName("home-loading"))
                        .withChannel(MessagingChannelConfig.of(String.class).withName("home-unloading").withReceiver(new WorldUnloadReceiver(worldManager)))
        );
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
