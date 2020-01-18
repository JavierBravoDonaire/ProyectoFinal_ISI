package urjc.isi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import spark.Request;
import urjc.isi.dao.implementaciones.*;
import urjc.isi.entidades.*;

public class GuionistasService {

	/**
	 * Constructor por defecto
	 */
	public GuionistasService() {}

	/**
	 * Metodo encargado de procesar un selectAll de la tabla guionistas
	 * @return Lista de guionistas de la tabla Guionistas
	 * @throws SQLException
	 */
	public List<Personas> getAllGuionistas() throws SQLException{
		GuionistasDAOImpl guionistas = new GuionistasDAOImpl();
		List<Personas> result = guionistas.selectAll();
		guionistas.close();
		return result;
	}

	/**
	 * Metodo encargado de procesar la subida de los registros de la tabla Guionistas
	 * @param req
	 * @return Estado de la subida
	 */
	public String uploadTable(Request req){
		GuionistasDAOImpl guionistas = new GuionistasDAOImpl();
		req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
		String result = "File uploaded!";
		try (InputStream input = req.raw().getPart("uploaded_guionistas_file").getInputStream()) {
		    guionistas.dropTable();
		    guionistas.createTable();
			InputStreamReader isr = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(isr);
			guionistas.uploadTable(br);
		} catch (IOException | ServletException | SQLException e) {
			System.out.println(e.getMessage());
		}
		guionistas.close();
		return result;
	}

	public List<Personas> getGuionistasByFechaNac (String fecha) throws SQLException {
		GuionistasDAOImpl guionistas = new GuionistasDAOImpl ();
		List<Personas> result = guionistas.selectPerByFechaNac (fecha);
		guionistas.close();
		return result;
	}

	public List<Personas> getGuionistasMuertos () throws SQLException {
		GuionistasDAOImpl guionistas = new GuionistasDAOImpl ();
		List<Personas> result = guionistas.selectPerMuertas ();
		guionistas.close();
		return result;
	}

	public List<Personas> getGuionistasByIntervaloNac (String fechaIn, String fechaFin) throws SQLException {
		GuionistasDAOImpl guionistas = new GuionistasDAOImpl ();
		List<Personas> result = guionistas.selectPerByIntervaloNac (fechaIn, fechaFin);
		guionistas.close();
		return result;
	}

	public 	Dictionary<String,Object> fullGuionistasInfo(String name) throws SQLException{
		GuionistasDAOImpl guionistasDAO = new GuionistasDAOImpl();
		PeliculasDAOImpl peliDAO = new PeliculasDAOImpl();
		Personas persona = new Personas();
		persona = guionistasDAO.selectByName(name);
		String id = persona.getId();

		Dictionary<String,Object> result = new Hashtable<String,Object>();
		if(id.length()>0){
			result.put("guionista", (Object)guionistasDAO.selectByID(id));
			result.put("peliculas", (Object)peliDAO.selectByGuionistaID(id));
		}
		guionistasDAO.close();
		peliDAO.close();
		return result;
	}
}
