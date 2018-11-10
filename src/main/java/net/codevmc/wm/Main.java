package net.codevmc.wm;

import ink.aquar.util.objnotation.ObjectNotationUtil;
import io.hekate.core.HekateFutureException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        File configFile = new File("config.yml");
        Path pathToConfig = Paths.get(configFile.toURI());
        if (!configFile.exists()) {
            Files.copy(Main.class.getResourceAsStream("/config.yml"), pathToConfig, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("New config file created. Edit it please.");
            return;
        } else if (!configFile.isFile()) {
            System.err.println("config.yml is not a file!");
            return;
        }

        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try {
            config = yaml.load(Files.newInputStream(pathToConfig, StandardOpenOption.READ));
        } catch (YAMLException ex) {
            System.err.println("Cannot parse the config.");
            ex.printStackTrace();
            return;
        }

        Object confObj = config.get("cluster");

        try {
            ObjectNotationUtil.requireElements(confObj,
                    new ObjectNotationUtil.ExpectedElement(String.class, false, "cluster.clusterName"),
                    new ObjectNotationUtil.ExpectedElement(String.class, false, "cluster.nodeName"));
        } catch (ObjectNotationUtil.MissingElementException ex) {
            System.err.println("Missing Element in config!");
            ex.printStackTrace();
            return;
        }

        Map<String, Object> clusterConf = ObjectNotationUtil.getFromJsonObject((Map) confObj, "cluster");

        WorldManager manager = new WorldManager(
                ObjectNotationUtil.getFromJsonObject(clusterConf, "clusterName"),
                ObjectNotationUtil.getFromJsonObject(clusterConf, "nodeName")
        );

        try {
            manager.instantiate();
        } catch (HekateFutureException ex) {
            System.err.println("Cannot join cluster.");
            ex.printStackTrace();
            try {
                manager.shutdown();
            } catch (HekateFutureException e) {
                e.printStackTrace();
            }
        }

        Scanner in = new Scanner(System.in);
        while(true) {
            String cmd = in.next();
            if(cmd.equalsIgnoreCase("exit")) {
                try {
                    manager.shutdown();
                } catch (HekateFutureException ex) {
                    System.err.println("Exception occur during exiting from the cluster...");
                    ex.printStackTrace();
                }
                break;
            }
        }

    }

}
