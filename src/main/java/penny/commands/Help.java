package main.java.penny.commands;

import main.java.penny.constants.ResourceConstants;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Displays the program usage of Penny.
 *
 * Help displays the detailed description and program usage of Penny to the console.  The program usage is
 * defined in the usage file included in the resources of Penny.
 */
public class Help {

    /**
     * Executes this command with the provided (minor) arguments.  Throws an Exception if this command
     * fails to execute.
     */
    public static void execute(String[] args) throws IOException {
        Scanner usage = new Scanner(new File(ResourceConstants.RESOURCE_FILE_PATH + ResourceConstants.USAGE_FILE));

        while (usage.hasNextLine()) {
            System.out.println(usage.nextLine());
        }

        usage.close();
    }
}
