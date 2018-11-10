package net.codevmc.wm;

import io.hekate.messaging.Message;
import io.hekate.messaging.MessageReceiver;
import net.codevmc.util.LineOfCommand;

public class WorldUnloadReceiver implements MessageReceiver<String> {

    private final WorldManager worldManager;

    public WorldUnloadReceiver(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void receive(Message<String> msg) {

        LineOfCommand cmd = new LineOfCommand(msg.get()).purgeEmptyArgs();

        switch(cmd.getCommand()) {
            case "unload":
                msg.reply(String.valueOf(worldManager.tryUnload( /* World */ cmd.getArg(0), /* EET */ Long.parseLong(cmd.getArg(1)))));
                break;
        }

    }
}
