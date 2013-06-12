package zkexamples;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class SyncPrimitive implements Watcher {
    public static ZooKeeper zk = null;
    public static final Object mutex = new Object();

    protected String root;

    SyncPrimitive(String address) 
    throws KeeperException, IOException {
        if(zk == null){
                System.out.println("Starting ZK:");
                zk = new ZooKeeper(address, 3000, this);
                System.out.println("Finished starting ZK: " + zk);
        }
    }

    public void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notify();
        }
    }

   public static void main(String args[]) {
      System.out.println("SyncPrimitive.class");
   }
}
