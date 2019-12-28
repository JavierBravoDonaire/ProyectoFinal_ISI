package urjc.isi.controladores;

import static spark.Spark.get;

import static spark.Spark.post;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;
import urjc.isi.entidades.Generos;
import urjc.isi.entidades.Peliculas;
import urjc.isi.entidades.RelacionesGeneros;
import urjc.isi.service.GenerosService;
import urjc.isi.service.PeliculasGenerosService;
import urjc.isi.service.PeliculasService;

public class PeliculasGenerosController {
	private static GenerosService gs;
	private static PeliculasGenerosService pgs;
	private static PeliculasService ps;
	private static String adminkey = "1234";
	
	/**
	 * Constructor por defecto
	 */
	public PeliculasGenerosController() {
		gs = new GenerosService();
		pgs = new PeliculasGenerosService();
		ps = new PeliculasService();
	}
	
	/**
	 * Maneja las peticiones que llegan al endpoint /generos/uploadTable
	 * @param request
	 * @param response
	 * @return El formulario para subir el fichero con las pseudoqueries o una redireccion al endpoint /welcome
	 * @throws SQLException 
	 */

	/**
	 * Maneja las peticiones que llegan al endpoint /peliculas/uploadTable
	 * @param request
	 * @param response
	 * @return El formulario para subir el fichero con las pseudoqueries o una redireccion al endpoint /welcome
	 */
	public static String uploadTable(Request request, Response response) {
		if(!adminkey.equals(request.queryParams("key"))) {
			response.redirect("/welcome"); //Se necesita pasar un parametro (key) para poder subir la tabla
		}
		return "<form action='/peliculasgeneros/upload' method='post' enctype='multipart/form-data'>" 
			    + "    <input type='file' name='uploaded_peliculasgeneros_file' accept='.txt'>"
			    + "    <button>Upload file</button>" + "</form>";
	}
	
	/**
	 * Metodo que se encarga de manejar las peticiones a /peliculas/upload
	 * @param request
	 * @param response
	 * @return Mensaje de estado sobre la subida de los registros
	 */	
	
	public static String selectGenero(Request request, Response response) throws SQLException {
		List<Generos> output = gs.getAllGeneros();
		String result = "<form action='/peliculasgeneros/selectGenero' method='get' enctype='multipart/form-data'>" + "  <select name=\"item\">\n";
		{
			for(int i = 0; i < output.size(); i++) {
				String[] tokens= output.get(i).toHTMLString().split("\\s");
			    result = result + "<option value=\"" + tokens[1] + "\">" + tokens[1] + "</option>\n";
			}
		    result = result + "  </select>\n" + 
		    "  <input type=\"submit\" value=\"Submit\">"
		    + "</form>";
		}
		return result;
	}
	
	/**
	 * Metodo que se encarga de manejar las peticiones a /generos/upload
	 * @param request
	 * @param response
	 * @return Mensaje de estado sobre la subida de los registros
	 */
	public static String upload(Request request, Response response) {
		return pgs.uploadTable(request);
	}
	
	/**
	 * Maneja las peticiones al endpoint /generos/selectAll
	 * @param request
	 * @param response
	 * @return La lista de generos que hay en la tabla Generos de la base de datos en formato HTML
	 * @throws SQLException
	 */
	public static String selectAllPeliculasGeneros(Request request, Response response) throws SQLException {
		List<Peliculas> output;
		String result = "";
		String genero = request.queryParams("item");
		output = ps.getAllPeliculasByGenero(genero);
		for(int i = 0; i < output.size(); i++) {
		    result = result + output.get(i).toHTMLString() +"</br>";
		}
		return result;
	}
	
	public static String selectAllGeneros(Request request, Response response) throws SQLException {
		List<RelacionesGeneros> output = pgs.getAllGeneros();
		String result = "";

		for(int i = 0; i < output.size(); i++) {
		    result = result + output.get(i).toHTMLString() +"</br>";
		}
		return result;
	}
	

	
	/**
	 * Metodo que se encarga de manejar todos los endpoints que cuelgan de /peliculasgeneros
	 */
	public void peliculasHandler() {
		get("/selectGenero", PeliculasGenerosController::selectAllPeliculasGeneros);
		get("/selectAll", PeliculasGenerosController::selectAllGeneros);
		get("/uploadTable", PeliculasGenerosController::uploadTable);
		post("/upload", PeliculasGenerosController::upload);
		get("/uploadGenero", PeliculasGenerosController::selectGenero);
	}
	
}
