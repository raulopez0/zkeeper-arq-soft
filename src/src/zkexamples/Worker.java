package zkexamples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;


public class Worker extends zkexamples.SyncPrimitive implements Runnable {
	Random random = new Random( (new Date()).getTime() );
	private Boolean continuar;
	private Queue queue = null;
	private Integer id;
	Logger logger = null;
	
	public Worker(String monitor_address, int worker_id, String queue_address, String queue_name) throws KeeperException, IOException {
		super(monitor_address);
		this.id = worker_id;
		this.continuar = true;
		this.queue = new Queue(queue_address, queue_name);
		//logger = Logger.getLogger("Worker_"+(new Integer(this.id)).toString());
		
		// creo nodo que me identifica al monitor de workers
		if (zk != null) {
	         try {
	            
	              zk.create("/monitor/worker_"+id.toString(), new byte[0], Ids.OPEN_ACL_UNSAFE,
	                       	CreateMode.PERSISTENT);
	         } catch (KeeperException e) {
	            System.out
	               .println("Keeper exception when instantiating queue: "
	                        + e.toString());
	         } catch (InterruptedException e) {
	            System.out.println("Interrupted exception");
	                }
	      }
	}
	
	public Integer get_id() {
		return this.id;
	}
	
	public void stop_worker() {
		try {
			zk.delete("/monitor/worker_"+id.toString(), -1);
		} catch (InterruptedException | KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.continuar = false;
	}

	public void run() {
		// TODO Auto-generated method stub
		BasicConfigurator.configure();
		
		while( continuar ) {
                byte[] data;
				try {
					data = queue.consume();
	                SolicitudInscripcion solicitud = (SolicitudInscripcion) SerializationUtils.deserialize(data);
	                this.procesarSolicitud(solicitud);
	                Thread.sleep( random.nextInt(3000));

				} catch (KeeperException e)  {
					e.printStackTrace();
					logger.error("Error al procesar solicitud"+e.getCause().toString());
				} catch (InterruptedException e){
					this.stop_worker();
					logger.debug("Recibo interrupcion");
				}
		}
		
		logger.info("Terminal el worker");
	}
		
	public void procesarSolicitud(SolicitudInscripcion solicitud) {
        logger.info("Procesando solicitud "+solicitud.toString());
        
        if( random.nextBoolean() ) {
        	logger.info("Solicitud "+solicitud.toString()+" aceptada.");
        } else {
        	logger.info("Solicitud "+solicitud.toString()+" rechazada.");
        }
        logger.info("Fin Procesando solicitud "+solicitud.toString());

	}
	
	public static void main(String[] args) {
		
		try {
		
			Thread th1 = new Thread( new Worker("localhost", 1,"localhost", "/solicitudes"));
			Thread th2 = new Thread( new Worker("localhost", 2, "localhost", "/solicitudes"));
			
			th1.start();
			th2.start();
			
			th1.join();
			th2.join();
			
		} catch ( InterruptedException | KeeperException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
