package net.codevmc.wm;

import ink.aquar.util.lock.Locker;
import io.hekate.cluster.ClusterNode;
import io.hekate.cluster.ClusterNodeId;
import io.hekate.core.Hekate;
import io.hekate.core.HekateFutureException;
import io.hekate.messaging.MessagingFutureException;
import io.hekate.messaging.broadcast.AggregateFuture;
import io.hekate.messaging.broadcast.AggregateResult;
import io.hekate.messaging.unicast.ResponseFuture;
import net.codevmc.util.LineOfCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WorldManager {

    private final ClusterManager clusterManager;

    private final ConcurrentMap<String, WorldMeta> worlds = new ConcurrentHashMap<>();

    private final Locker<String> locker = new Locker<>();

    public WorldManager(String clusterName, String nodeName) {
        clusterManager = new ClusterManager(clusterName, nodeName, this);
    }

    public String getTeleportServer(String worldName) {
        locker.lock(worldName);
        try {
            WorldMeta meta = worlds.get(worldName);
            if (meta == null) {
                return aggregateBestServerAndRegister(worldName);
            } else {
                /*
                 * To unload a world, send "unload" in "home-unloading" channel to the world manager,
                 * with argument index 0 the expected expire time(EET), or -1 EET to force unload the world.
                 *
                 * EET should normally 30000(30s) greater than 0, as System.currentTimeMillis() > meta.lastRenewal + EET,
                 * response will be true, meaning allows the spigot to unload the world,
                 * and unload the world on the world manager before making the response.
                 * If EET is less than 0, it would theoretically always respond true, mind the opts.
                 *
                 * Turning off the spigot node should unload the world and send the player to another home server,
                 * where when responding "get-load" request(local aggregation) returns 32768,
                 * which will be impossible to be selected as the server that loads the world.
                 *
                 * Doing STP may get to a not running server, as the player get a bungeeCord Id right before it shutdowns.
                 * When stp fails, just the whole teleport request process.
                 */
                List<ClusterNode> nodes = clusterManager.getHekate().cluster().topology().nodes();
                for (ClusterNode node : nodes) {
                    if (node.id().equals(meta.handlerId)) {
                        meta.renew();
                        return meta.bungeeCordId;
                    }
                }
                worlds.remove(worldName);
                return aggregateBestServerAndRegister(worldName);
            }
        } finally {
            locker.unlock(worldName);
        }
        // DONE ConcurrentMap is doing shits, make a mapped locker.
    }

    public void instantiate() throws HekateFutureException {
        clusterManager.instantiate();
    }

    public void shutdown() throws HekateFutureException {
        try {
            Hekate hekate= clusterManager.getHekate();
            if(hekate != null)
                hekate.leave();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String aggregateBestServerAndRegister(String worldName) {
        for(int i=0;i<3;i++) { // try 3 times
            AggregateFuture<String> aggregateFuture = clusterManager.getHekate().messaging().channel("home-loading", String.class).aggregate("get-load");
            try {
                AggregateResult<String> aggregateResult = aggregateFuture.get();
                ClusterNode bestNode = new ServerElection<>(aggregateResult.resultsByNode()).get();
                if (bestNode == null) {
                    return null;
                }
                String bungeeCordId = new LineOfCommand(aggregateResult.resultsByNode().get(bestNode)).getArg(0);
                ResponseFuture<String> respFuture = clusterManager.getHekate().messaging().channel("home-loading", String.class).request("load " + worldName);
                respFuture.response();
                worlds.put(worldName, new WorldMeta(worldName, bungeeCordId, bestNode.id()));
                return bungeeCordId;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (MessagingFutureException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean tryUnload(String worldName, long expectedExpireTime) {
        locker.lock(worldName);
        try {
            WorldMeta meta = worlds.get(worldName);
            if(meta == null) {
                return true;
            } else if(meta.lastRenewal + expectedExpireTime < System.currentTimeMillis()) {
                worlds.remove(worldName);
                return true;
            } else {
                return false;
            }
        } finally {
            locker.unlock(worldName);
        }
        // DONE ConcurrentMap is doing shits, make a mapped locker.
    }

    private class WorldMeta {

        WorldMeta(String worldName, String bungeeCordId, ClusterNodeId handlerId) {
            this.worldName = worldName;
            this.bungeeCordId = bungeeCordId;
            this.handlerId = handlerId;
            renew();
        }

        final String worldName;
        final String bungeeCordId;
        final ClusterNodeId handlerId;
        volatile long lastRenewal;

        void renew() {
            lastRenewal = System.currentTimeMillis();
        }

    }

    private static class ServerElection<K> {
        int minLoad = Short.MAX_VALUE;
        K bestServerId;

        ServerElection(Map<K, String> aggregationResult) {
            aggregationResult.forEach((id, result) -> {
                LineOfCommand cmd = new LineOfCommand(result);
                int load = Integer.parseInt(cmd.getCommand());
                if(load < minLoad) {
                    bestServerId = id;
                    minLoad = load;
                }
            });
        }

        K get() {
            return bestServerId;
        }
    }

}
