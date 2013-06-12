package zkexamples;

import java.io.Serializable;

public class SolicitudInscripcion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String padron;
	private String codigo_materia;
	
	public SolicitudInscripcion() {
		
	}
	public SolicitudInscripcion( String padron, String codigo_materia) {
		this.setPadron(padron);
		this.setCodigoMateria(codigo_materia);
	}
	
	public String getPadron() {
		return padron;
	}
	
	public void setPadron(String padron) {
		this.padron = padron;
	}
	
	public String getCodigoMateria() {
		return codigo_materia;
	}
	
	public void setCodigoMateria(String codigo_materia) {
		this.codigo_materia = codigo_materia;
	}
	
	public String toString() {
		return padron+":"+codigo_materia;
	}
}
