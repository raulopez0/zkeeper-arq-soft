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
public class Database extends zkexamples.SyncPrimitive  {

	private Logger logger = Logger.getLogger("Database");
	private String root;
	private String root_aceptadas;
	private String root_rechazadas;

   /**
    * Constructor of producer-consumer queue
    *
    * @param address
    * @param name
    */
   Database(String address) throws KeeperException, IOException {
      super(address);

      this.root = "/database";
      this.root_aceptadas = "/database/solicitudes_aceptadas";
      this.root_rechazadas = "/database/solicitudes_rechazadas";

      // Create ZK node name
      if (zk != null) {
         try {
            Stat s = zk.exists(root, true);
            if (s == null) {
               zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                         CreateMode.PERSISTENT);
            }
            
            s = zk.exists(root_aceptadas, true);
            if (s == null) {
            	zk.create(root_aceptadas, new byte[0], Ids.OPEN_ACL_UNSAFE,
            			CreateMode.PERSISTENT);
            }

            s = zk.exists(root_rechazadas, true);
            if (s == null) {
            	zk.create(root_rechazadas, new byte[0], Ids.OPEN_ACL_UNSAFE,
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

   public void store(SolicitudInscripcion solicitud, boolean aceptada) {
	   try {
		   String node_name = solicitud.getPadron()+":"+solicitud.getCodigoMateria();
		   
		   if(aceptada)
			   zk.create(root_aceptadas+"/"+node_name, SerializationUtils.serialize(solicitud), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		   else
			   zk.create(root_rechazadas+"/"+node_name, SerializationUtils.serialize(solicitud), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

	   } catch (KeeperException | InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   
   private void remove_childs(String path) {
	   if (zk != null) {
		   try {
			   Stat s = zk.exists(path, false);
			   if( s!=null) {
	                List<String> nodes = zk.getChildren(path, false);
	                
	                for(String node_name : nodes){
	                	remove_childs(path+"/"+node_name);
	                	System.out.println("delete "+path+"/"+node_name);
	                }
	                
	                zk.delete(path, -1);
                	System.out.println("delete "+path);
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
   
   public void clean_all() {
	   remove_childs("/database");
   }

   public static void main(String args[]) {
	   
	      try {
	    	  //MonitorClean mnclean = new MonitorClean("localhost");
	    	  Database db = new Database("localhost");
	    	  db.clean_all();
	      } catch(Exception ex) {
	         ex.printStackTrace();
	      }
	    }

}
	   
	
   

