package fr.eurecom.clouds.zklab;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Process {

    private static final String GROUP_NAME = "groupXX"; // <----- CHANGE THIS: substitute XX with your group number

    public static void main(String[] args) throws IOException, InterruptedException
    {
        /* Check input arguments */
        if(args.length < 1) {
            System.err.println("Usage: java -jar <jar_file_name> <process id integer>");
            System.exit(2);
        }

        /* Initialize Log4J to see ZooKeeper client messages */
        BasicConfigurator.configure();
        Logger logger = Logger.getRootLogger();
        logger.setLevel(Level.INFO);

        final int id = Integer.valueOf(args[0]);

        final Elections myElection = new Elections(GROUP_NAME, id);

        /* ... missing code here: the process should register and check if an election is needed */

        while (true) { // An infinite loop to keep the process alive
            TimeUnit.SECONDS.sleep(1);
        }
    }
}