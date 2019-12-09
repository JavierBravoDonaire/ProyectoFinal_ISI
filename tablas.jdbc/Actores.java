package jdbc_spark.jdbc_spark;

import static spark.Spark.*;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.util.StringTokenizer;

import javax.servlet.MultipartConfigElement;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Actores {

    // Connection to the SQLite database. Used by insert and select methods.
    // Initialized in main
    private static Connection connection;
	private static int Max_Actores = 10;

    public static String doSelect(Request request, Response response)
	{
		return select (connection, request.params(":table"),
                                   		request.params(":actor_id"));
    }

    public static String select(Connection conn, String table, String id)
	{
		String sql = "SELECT * FROM " + table + " WHERE id_actor=?";

		String result = new String();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			ResultSet rs = pstmt.executeQuery();
	        // Commit after query is executed
			connection.commit();

			while(rs.next()){
			    // read the result set
			    result += "id = " + rs.getString("id_actor") + "\n";
			    //System.out.println("id = "+rs.getString("id_actor") + "\n");
			    result += "nombre = " + rs.getString("nombre") + "\n";
			    //System.out.println("nombre = "+rs.getString("nombre")+"\n");
			    result += "apellido = " + rs.getString("apellido") + "\n";
			    //System.out.println("apellido = "+rs.getString("apellido")+"\n");
			    result += "fecha_nac = " + rs.getString("fecha_nac") + "\n";
			    //System.out.println("fecha_nac = "+rs.getString("fecha_nac")+"\n");
			    result += "fecha_muer = " + rs.getString("fecha_muer") + "\n";
			    //System.out.println("fecha_muer = " + rs.getString ("fecha_muer")+"\n");
		    	result += "pais = " + rs.getString("pais") + "\n";
			    //System.out.println("pais = "+rs.getString("pais")+"\n");
			}
		}catch(SQLException e){
		    System.out.println(e.getMessage());
		}

	return result;
    }


    public static void insert(Connection conn, int id, String nombre, String apellido, String fecha_nac, String fecha_muer, String pais)
	{
		String sql = "INSERT INTO actores(id_actor, nombre, apellido, fecha_nac, fecha_muer, pais) VALUES(?,?,?,?,?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.setString(2, nombre);
			pstmt.setString(3, apellido);
			pstmt.setString(4, fecha_nac);
			pstmt.setString(5, fecha_muer);
			pstmt.setString(6, pais);
			pstmt.executeUpdate();
	    }catch(SQLException e){
	    	System.out.println(e.getMessage());
		}
    }

    public static void main(String[] args) throws
									ClassNotFoundException, SQLException
	{
		port(getHerokuAssignedPort());


		// Connect to SQLite sample.db database
		// connection will be reused by every query in this simplistic example
		connection = DriverManager.getConnection("jdbc:sqlite:sample.db");

		// SQLite default is to auto-commit (1 transaction / statement execution)
	    // Set it to false to improve performance
		connection.setAutoCommit(false);


		// GET /:table/:actor_id HTTP request, llamará al metodo Actores::doSelect
		// que devuelve el resultado de la query (:table --> tabla actores, :actor_id --> id del actor)
		get("/:table/:actor_id", Actores::doSelect);

		// GET /upload_actors HTTP request, devuelve formulario para intruducir el fichero .txt
		get("/upload_actors", (req, res) ->
		    "<form action='/upload_actors' method='post' enctype='multipart/form-data'>"
		    + "    <input type='file' name='uploaded_actors_file' accept='.txt'>"
		    + "    <button>Upload file</button>" + "</form>");
		// You must use the name "uploaded_actors_file" in the call to
		// getPart to retrieve the uploaded file. See next call:


		// A partir del fichero en el anterior formulario, lee linea a linea
		// y crea la tabla y guarda en ella los datos
		post("/upload_actors", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
			String result = "Actores guardados con éxito!";
			try (InputStream input = req.raw().getPart("uploaded_actors_file").getInputStream()) {
				// getPart needs to use the same name "uploaded_actors_file" used in the form

				// Prepare SQL to create table
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.
				statement.executeUpdate("drop table if exists actores"); // Podemos cambiar esto para no eliminar la tabla siempre que llegue un fichero nuevo
				statement.executeUpdate("create table actores (id_actor INT, nombre string, apellido string, fecha_nac string, fecha_muer string, pais string, PRIMARY KEY (id_actor))");

				// Read contents of input stream that holds the uploaded file
				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				String s;
				int id_pel = 0;
				while((s = br.readLine()) != null && id_pel != Max_Actores){
				    // Se tokeniza la linea con ',' que es lo que separa los campos
				    StringTokenizer tokenizer = new StringTokenizer(s, ",");
				    // El primer token es el apellido del actor
				    String apellido = tokenizer.nextToken();
					// El segundo token es el nombre del actor
				    String nombre = tokenizer.nextToken();
					// El tercer token es el id de la película en la que participa (No se necesita aqui)
	 			    id_pel = Integer.parseInt (tokenizer.nextToken());
					// El cuarto token es el id del actor
	 			    int id = Integer.parseInt(tokenizer.nextToken());
					// El quinto token es la fecha de nacimiento
	 			    String fecha_nac = tokenizer.nextToken();
					// El sexto token es la fecha de muerte
	 			    String fecha_muer = tokenizer.nextToken();
					// El octavo token es el pais del actor
	 			    String pais = tokenizer.nextToken();
					// Se inserta en la tabla su id, nombre, apellido, fechas y pais
				    insert(connection, id, nombre, apellido, fecha_nac, fecha_muer, pais);
				    connection.commit();
				}
				input.close();
			}
			return result;
		});

    }

    static int getHerokuAssignedPort()
	{
		ProcessBuilder processBuilder = new ProcessBuilder();
		if(processBuilder.environment().get("PORT") != null){
	    	return Integer.parseInt(processBuilder.environment().get("PORT"));
		}
		return 4567; // return default port if heroku-port isn't set (i.e. on localhost)
    }
}
