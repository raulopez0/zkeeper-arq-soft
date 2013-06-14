package zkexamples;

import zkexamples.SyncPrimitive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
//import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;


/**
 * Producer-Consumer queue
 */
public class MonitorClean extends zkexamples.SyncPrimitive {

   /**
    * Constructor of producer-consumer queue
    *
    * @param address
    * @param name
    */
   MonitorClean(String address) throws KeeperException, IOException {
      super(address);
      this.root = "/monitor";
      // Create ZK node name
      if (zk != null) {
         try {
            Stat s = zk.exists(root, false);
            if( s!=null) {
                List<String> nodes = zk.getChildren(root, false);
                for(String node_name : nodes){
                	zk.delete(root+"/"+node_name, -1);
                }
                zk.delete(root, -1);
            }
            
         } catch (KeeperException e) {
            System.out
               .println("Keeper exception when instantiating queue: "
                        + e.toString());
         } catch (InterruptedException e) {
            System.out.println("Interrupted exception");
         }
      }
      
   }   
   
   public static void main(String args[]) {
	   
	      try {
	    	  //MonitorClean mnclean = new MonitorClean("localhost");
	    	  MonitorClean mn = new MonitorClean("localhost");
	      } catch(Exception ex) {
	         ex.printStackTrace();
	      }
	    }

}
