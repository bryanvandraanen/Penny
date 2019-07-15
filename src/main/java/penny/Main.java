package main.java.penny;

import main.java.penny.commands.Analyze;
import main.java.penny.commands.LiveScan;
import main.java.penny.commands.Help;
import main.java.penny.commands.Spoof;
import main.java.penny.constants.CLIConstants;

import java.util.Arrays;

/**
 * Main method of execution for Penny.
 */
public class Main {

    /**
     * Runs the specific command following the usage outlined in README and usage.txt
     */
    public static void main(String[] args) {
        String command = args.length > 0 ? args[0] : "";

        // Remove major command argument from arguments (if one exists) for command-specific arguments
        String[] commandArgs = args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : args;

        try {
            switch (command) {
                case CLIConstants.LIVE_SCAN_COMMAND:
                    LiveScan.execute(commandArgs);
                    break;
                case CLIConstants.HELP_COMMAND:
                    Help.execute(commandArgs);
                    break;
                case CLIConstants.ANALYZE_COMMAND:
                    Analyze.execute(commandArgs);
                    break;
                case CLIConstants.SPOOF_COMMAND:
                    Spoof.execute(commandArgs);
                    break;
                default:
                    System.out.println("No command-line arguments specified.  Use \"--help\" to see program usages.");
                    Help.execute(commandArgs);
                    break;
            }
        } catch (Exception e) {
            System.out.print("Command failed because of an exception with arguments: ");
            System.out.println(Arrays.toString(commandArgs));
            System.out.println("Use --help for information on argument format");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.exit(0);
    }
}
