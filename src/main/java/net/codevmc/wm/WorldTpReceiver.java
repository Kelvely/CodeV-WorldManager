package net.codevmc.wm;

import io.hekate.messaging.Message;
import io.hekate.messaging.MessageReceiver;
import net.codevmc.util.LineOfCommand;

public class WorldTpReceiver implements MessageReceiver<String> {

    private final WorldManager worldManager;

    public WorldTpReceiver(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public void receive(Message<String> msg) {

        LineOfCommand cmd = new LineOfCommand(msg.get()).purgeEmptyArgs();

        switch(cmd.getCommand()) {
            case "teleport":
                msg.reply(worldManager.getTeleportServer(cmd.getArg(0)));
                break;
        }

    }
}
