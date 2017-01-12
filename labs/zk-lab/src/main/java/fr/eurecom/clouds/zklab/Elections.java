package fr.eurecom.clouds.zklab;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

class Elections {

    private final int processId;
    private final ZooKeeperClient zooKeeperClient;

    /* Addresses of Zookeeper cluster: DO NOT CHANGE */
    private static final String ZK_QUORUM_ADDRESSES = "192.168.45.15:5181,192.168.45.150:5181,192.168.45.151:5181,192.168.45.152:5181,192.168.45.153:5181";

    Elections(final String groupName, final int processId) throws IOException
    {
        this.processId = processId;

        this.log("Starting ZooKeeper client...");
        zooKeeperClient = new ZooKeeperClient(ZK_QUORUM_ADDRESSES, groupName, new ZKWatcher());
    }

    void register() {
        // Create an election node if it does not already exists
        String electionPath = zooKeeperClient.createNode("/election", false, false);

        /* ... missing code here: this process must register to participate in the election */
    }

    void attemptLeadership() {
        /* ... missing code here ...

         the implementation of the leader election goes here.

         Hint: to sort a list of strings you can use Collections.sort(aListOfStrings)

         */
    }

    /* ZooKeeper watcher object, the method process() will be called every time a watch is triggered */
    public class ZKWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event)
        {
            log("Watch fired on path: " + event.getPath() + " type " + event.getType());

            final Event.EventType eventType = event.getType();

            /* ... missing code here ...

               ZooKeeper documentation on event types:
               https://zookeeper.apache.org/doc/r3.4.9/api/org/apache/zookeeper/Watcher.Event.EventType.html
             */
        }
    }

    private void log(String s)
    {
        /* Simple function to prefix the process ID to debugging info */
        System.out.println("ID " + this.processId + ": " + s);
    }
}