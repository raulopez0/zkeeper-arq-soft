package zkexamples;

import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.zookeeper.KeeperException;


public class WebServer implements Runnable {
	private Random random = new Random();
	private static int WEB_SERVER_TIMEOUT = 2000;
	private static int WEB_SERVER_SLEEP = 5000;

	private BlockingQueue<SolicitudInscripcion> solicitudes;
	private Queue queue = null;
	private Boolean continuar;
	
	private int id;
	private Logger logger = null;
	
	public WebServer(	int id, BlockingQueue<SolicitudInscripcion> solicitudes, 
						String queue_address, String queue_name) throws KeeperException, IOException {

		this.id = id;
		this.continuar = true;
		this.solicitudes = solicitudes;
		logger = Logger.getLogger("WebServer_"+(new Integer(id)).toString());

		this.queue = new Queue(queue_address, queue_name);
	}
	
	public void stop() {
		this.continuar = false;
	}

	public void run() {
		
		while( continuar ) {
			try {
				SolicitudInscripcion proxima_solicitud = solicitudes.poll( WEB_SERVER_TIMEOUT, TimeUnit.MILLISECONDS );

				if (proxima_solicitud != null) {
					logger.info( "Llego una solicitud,envìo a cola de workers:  "+proxima_solicitud.toString() );
					queue.produce( SerializationUtils.serialize(proxima_solicitud));
					Thread.sleep( random.nextInt(WEB_SERVER_SLEEP));
				} else {
					logger.info("No hay mas inscripciones, terrmino" );
					this.stop();
				}
				
			} catch (InterruptedException | KeeperException e) {
				e.printStackTrace();
			}
		}
	}
		
	private static void cargarInscripciones(FileInputStream file, BlockingQueue<SolicitudInscripcion> solicitudes) {
		Logger logger = Logger.getLogger("");

		Integer cant_inscripciones = 0;
		Scanner input = new Scanner(file);
		Pattern p = Pattern.compile("^([0-9]*):([0-9]*)");

		logger.info("Cargando solicitudes de inscripcion del archivo de entrada");
		
		while(input.hasNext()) {			
		    
		    Matcher m = p.matcher(input.nextLine());
		    
		    if (m.lookingAt()) {
		    	String padron = m.group(1);
		    	String codigo_materia = m.group(2);
		    	try {
					solicitudes.put( new SolicitudInscripcion(padron, codigo_materia));
					cant_inscripciones++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    }
		}

		input.close();
		logger.info("Total solicitudes al web server: " +cant_inscripciones.toString());

	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		String file_name = "./data/web_servers_input.txt";
		
		BlockingQueue<SolicitudInscripcion> solicitudes = new LinkedBlockingQueue();
		
		try {
			FileInputStream file = new FileInputStream(file_name);
			cargarInscripciones(file, solicitudes);
		
			Thread th1 = new Thread( new WebServer(1, solicitudes, "localhost", "/solicitudes"));
			Thread th2 = new Thread( new WebServer(2, solicitudes, "localhost", "/solicitudes"));
			
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
