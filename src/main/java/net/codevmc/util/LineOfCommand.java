package net.codevmc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class LineOfCommand {

    private final String command;
    private final String[] arguments;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public LineOfCommand(String lineOfCommand) {
        LinkedList<String> splitCmdLine = new LinkedList<>(Arrays.asList(lineOfCommand.split(" ")));
        command = splitCmdLine.size() > 0 ? splitCmdLine.removeFirst() : "";
        arguments = splitCmdLine.toArray(EMPTY_STRING_ARRAY);
    }

    public LineOfCommand(String label, String[] arguments) {
        command = label;
        this.arguments = new String[arguments.length];
        System.arraycopy(arguments, 0, this.arguments, 0, arguments.length);
    }

    private LineOfCommand(String[] arguments, String label) {
        command = label;
        this.arguments = arguments;
    }

    /**
     * Create an array with all empty strings removed in the original array. </br>
     * @param original The original array
     * @return The array with empty strings removed
     */
    private static String[] removeEmptyStrings(String[] original) {
        ArrayList<String> list = new ArrayList<>(original.length);
        for (String string : original) {
            if(string.length() > 0) list.add(string);
        }
        return list.toArray(EMPTY_STRING_ARRAY);
    }

    public String getCommand() {
        return command;
    }

    public int numOfArgs() {
        return arguments.length;
    }

    public String getArg(int index) {
        return arguments[index];
    }

    public LineOfCommand purgeEmptyArgs() {
        return new LineOfCommand(removeEmptyStrings(arguments), command);
    }

    /*private static void checkLabelValidity(String label) {
        if(label.length() <= 0) throw new IllegalArgumentException("A command label cannot be empty!");
        if(!isLabelValid(label)) throw new IllegalArgumentException("CommandHandler only accept labels with '-', '_', alphabetic, and numeric characters!");
    }

    private static boolean isLabelValid(String label) {
        if(label.length() <= 0) return false;
        for(char character : label.toCharArray()) {
            if(!isValidLabelChar(character)) return false;
        }
        return true;
    }

    private static boolean isValidLabelChar(char character) {
        return (character > 0x40 && character < 0x5B) | (character > 0x60 && character < 0x7B) | (character > 0x2F && character < 0x3A) | (character == '_') | (character == '-');
    } */

}
