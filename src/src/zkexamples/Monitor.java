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
public class Monitor extends zkexamples.SyncPrimitive implements Watcher, Runnable {

	private Logger logger = Logger.getLogger("Monitor");
	private boolean continuar = true;
	private Integer min_workers;
	private Integer worker_next_id = 0;
	private List<Worker> workers = new LinkedList();
   /**
    * Constructor of producer-consumer queue
    *
    * @param address
    * @param name
    */
   Monitor(String address, int min_workers) throws KeeperException, IOException {
      super(address);
      this.min_workers = min_workers;
      this.root = "/monitor";
      // Create ZK node name
      if (zk != null) {
         try {
            Stat s = zk.exists(root, true);
            if (s == null) {
               zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                         CreateMode.PERSISTENT);
               zk.getChildren("/monitor", this);

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

   public void process(WatchedEvent event) {
	   logger.info("Hay modiicacion, monitoreo workers");
	   try {
		   List<String> running_workers = zk.getChildren("/monitor", this);
		   logger.info("Total de workers trabajando: "+(new Integer(running_workers.size())).toString()+"/"+min_workers.toString());

		   if( running_workers.size()<min_workers ) {
			   
			   Worker wk = new Worker(worker_next_id,"localhost", "/solicitudes", "localhost");
			   Thread th = new Thread( wk);
			   th.start();
			   workers.add(wk);
			   logger.info("Se cayo un worker, levanto uno nuevo con id "+worker_next_id.toString());
			   worker_next_id++;
		   }
	} catch (KeeperException | InterruptedException | IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   
   @SuppressWarnings("deprecation")
@Override
   public void run() {
	   Random random = new Random( (new Date()).getTime() );
	   
	   try {
		   synchronized (this) {
			   while (this.continuar) {
				   if ( random.nextInt(5) == 1 ) {
					   if(workers.size()>0) {
						   workers.get(0).stop_worker();
						   workers.remove(0);
					   }
						   
				   } else {
				   }
				   Thread.sleep(2500);
			   }
		   	}
	   } catch (InterruptedException e) {
	   }   	
   }
   
   public static void main(String args[]) {
	   
      try {
    	  //MonitorClean mnclean = new MonitorClean("localhost");
    	  Monitor mn = new Monitor("localhost", 5);
    	  mn.run();
      } catch(Exception ex) {
         ex.printStackTrace();
      }
    }


}
