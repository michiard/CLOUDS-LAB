package fr.eurecom.clouds.zklab;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooDefs.Ids;

class ZooKeeperClient {
    /* This class wraps the ZooKeeper APIs to offer a simplified interface. To prevent ZNode clashes between groups
       running the same exercise on the same ZooKeeper servers, methods of this class add a prefix to all paths passed
       in input.

       This file should be used as-is, without changes.
     */

    private ZooKeeper zk;
    private String basePath = "";

    ZooKeeperClient(final String connectString, final String groupName, Watcher watcher) throws IOException
    {
        String aux_path;

        this.zk = new ZooKeeper(connectString, 3000, watcher);
        aux_path = this.createNode("/lab", false, false);
        if (aux_path == null) {
            throw new IllegalStateException("Unable to create/access leader election root node with path: /lab");
        }
        aux_path = this.createNode("/lab/" + groupName, false, false);
        if (aux_path == null) {
            throw new IllegalStateException("Unable to create/access leader election root node with path: /lab/" + groupName);
        }
        this.basePath = "/lab/" + groupName;
    }

    String createNode(final String node, final boolean ephemeral, final boolean sequential)
    {
        String fullPath = basePath + node;
        String createdNodePath;
        try {
            final Stat nodeStat = zk.exists(fullPath, false);

            if(nodeStat == null) {
                CreateMode createMode = CreateMode.PERSISTENT;
                if (ephemeral && sequential)
                    createMode = CreateMode.EPHEMERAL_SEQUENTIAL;
                else if (ephemeral)
                    createMode = CreateMode.EPHEMERAL;
                else if (sequential)
                    createMode = CreateMode.PERSISTENT_SEQUENTIAL;

                createdNodePath = zk.create(fullPath, new byte[0], Ids.OPEN_ACL_UNSAFE, createMode);
            } else {
                createdNodePath = fullPath;
            }
        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        return createdNodePath;
    }

    String createNode(final String node, int value, final boolean ephemeral, final boolean sequential)
    {
        String fullPath = basePath + node;
        String createdNodePath;
        try {
            final Stat nodeStat = zk.exists(fullPath, false);

            if(nodeStat == null) {
                CreateMode createMode = CreateMode.PERSISTENT;
                if (ephemeral && sequential)
                    createMode = CreateMode.EPHEMERAL_SEQUENTIAL;
                else if (ephemeral)
                    createMode = CreateMode.EPHEMERAL;
                else if (sequential)
                    createMode = CreateMode.PERSISTENT_SEQUENTIAL;

                createdNodePath = zk.create(fullPath, BigInteger.valueOf(value).toByteArray(), Ids.OPEN_ACL_UNSAFE, createMode);
            } else {
                createdNodePath = fullPath;
            }
        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        return createdNodePath;
    }

    String watchNode(final String node)
    {
        String fullPath = basePath + node;

        try {
            final Stat nodeStat = zk.exists(fullPath, true);
            if (nodeStat != null) {
                return fullPath;
            } else {
                return null;
            }

        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    List<String> getChildren(final String node)
    {
        String fullPath = this.basePath + node;

        List<String> childNodes = null;

        try {
            childNodes = zk.getChildren(fullPath, false);
        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        return childNodes;
    }
}
