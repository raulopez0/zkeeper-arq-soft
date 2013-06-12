package zkexamples;

import zkexamples.SyncPrimitive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
//import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;


/**
 * Producer-Consumer queue
 */
public class Queue extends zkexamples.SyncPrimitive {

   /**
    * Constructor of producer-consumer queue
    *
    * @param address
    * @param name
    */
   Queue(String address, String name) throws KeeperException, IOException {
      super(address);
      this.root = name;
      // Create ZK node name
      if (zk != null) {
         try {
            Stat s = zk.exists(root, false);
            if (s == null) {
               zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                         CreateMode.PERSISTENT);
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

   /**
    * Add element to the queue.
    *
    * @param i
    * @return
    */

   boolean produce(byte[] data) throws KeeperException, InterruptedException{

      zk.create(root + "/element", data, Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);
      
      return true;
   }
   
   
   /**
    * Remove first element from the queue.
    *
    * @return
    * @throws KeeperException
    * @throws InterruptedException
    */
   	byte[] consume() throws KeeperException, InterruptedException{
      int retvalue = -1;
      Stat stat = null;
            
      // Get the first element available
      while (true) {
         synchronized (mutex) {
            List<String> list = zk.getChildren(root, true);
            if (list.size() == 0) {
               System.out.println("Going to wait");
               mutex.wait();
            } else {
               String min = new String(list.get(0));
               for(String s : list){
                  String tempValue = new String(s);
                  //System.out.println("Temporary value: " + tempValue);
                  if(tempValue.compareTo(min)<0) min = tempValue;
               }
               //System.out.println("Temporary value: " + root + "/" + min);
               byte[] b = zk.getData(root + "/"+min,
                                     false, stat);
               zk.delete(root + "/" + min, 0);
               
               return b;
            }
         }
      }
   }

   public static void queueTest(String args[]) throws KeeperException, IOException {
	  
	   
      Queue q = new Queue(args[1], "/solicitudes");

      int i;
      Integer max = new Integer(args[2]);
      
      if (args[3].equals("p")) {
         System.out.println("Producer");
         for (i = 0; i < max; i++)
            try{
            	System.out.println("Produce "+new Integer(10+i).toString());
            	q.produce((new Integer(10 + i)).toString().getBytes());
            } catch (KeeperException e){
               
            } catch (InterruptedException e){
               
            }
      } else {
            System.out.println("Consumer");
            
            for (i = 0; i < max; i++) {
               try{
                  byte[] data = q.consume();
                  SolicitudInscripcion yourObject = (SolicitudInscripcion) SerializationUtils.deserialize(data);
                  System.out.println("Item: " + yourObject.toString());
               } catch (KeeperException e){
                  i--;
               } catch (InterruptedException e){
                  
               }
            }
        }
   }
   
   public static void main(String args[]) {	   
      try {
         queueTest(args);
      } catch(Exception ex) {
         ex.printStackTrace();
      }
    }
}
