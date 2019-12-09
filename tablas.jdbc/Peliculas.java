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

public class Peliculas {

    private static Connection connection;
	private static int Max_Peliculas = 500;

    public static String doSelect(Request request, Response response)
	{
		return select (connection, request.params(":table"),
                                   		request.params(":film_id"));
    }

    public static String select(Connection conn, String table, String film_id)
	{
		String sql = "SELECT * FROM " + table + " WHERE id_pelicula=?";

		String result = new String();

		try(PreparedStatement pstmt = conn.prepareStatement(sql)){
			pstmt.setString(1, film_id); // Bug solucionado antes ponia setInt
			ResultSet rs = pstmt.executeQuery();
	        // Commit after query is executed
			connection.commit();

			while (rs.next()) {
			    result += "id = " + rs.getString("id_pelicula") + "\n";
			    //System.out.println("id = "+rs.getString("id_pelicula") + "\n");
			    result += "nombre = " + rs.getString("nombre") + "\n";
			    //System.out.println("nombre = "+rs.getString("nombre")+"\n");
			    result += "fecha = " + rs.getString("fecha") + "\n";
			    //System.out.println("fecha = "+rs.getString("fecha")+"\n");
			    result += "duracion = " + rs.getString("duracion") + "\n";
			    //System.out.println("duracion = "+rs.getString("duracion")+"\n");
			    result += "rating = " + rs.getString("rating") + "\n";
			    //System.out.println("rating = "+rs.getString("rating")+"\n");
			}
	    }catch(SQLException e){
	    	System.out.println(e.getMessage());
		}
		return result;
    }


    public static void insert(Connection conn, int id, String nombre, String fecha, String duracion, int rating)
	{
		String sql = "INSERT INTO peliculas(id_pelicula, nombre, fecha, duracion, rating) VALUES(?,?,?,?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.setString(2, nombre);
			pstmt.setString(3, fecha);
			pstmt.setString(4, duracion);
			pstmt.setInt(5, rating);
			pstmt.executeUpdate();
	    }catch (SQLException e){
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


		// GET /:table/:film_id HTTP request, llamará al metodo Peliculas::doSelect
		// que devuelve el resultado de la query (:table --> tabla peliculas, :film_id --> id de peli)
		get("/:table/:film_id", Peliculas::doSelect);

		// GET /upload_films HTTP request, devuelve formulario para intruducir el fichero .txt
		get("/upload_films", (req, res) ->
		    "<form action='/upload_films' method='post' enctype='multipart/form-data'>"
		    + "    <input type='file' name='uploaded_films_file' accept='.txt'>"
		    + "    <button>Upload file</button>" + "</form>");
		// You must use the name "uploaded_films_file" in the call to
		// getPart to retrieve the uploaded file. See next call:


		// A partir del fichero en el anterior formulario, lee linea a linea
		// y crea la tabla y guarda en ella los datos
		post("/upload_films", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
			String result = "Películas guardadas con éxito!";
			try (InputStream input = req.raw().getPart("uploaded_films_file").getInputStream()) {
				// getPart needs to use the same name "uploaded_films_file" used in the form

				// Prepare SQL to create table
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.
				statement.executeUpdate("drop table if exists peliculas");
				statement.executeUpdate("create table peliculas (id_pelicula INT, nombre string, fecha string, duracion string, rating INT, PRIMARY KEY (id_pelicula))");

				// Lee el contenido del fichero linea a linea
				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				String s;
				int id = 0;
				while ((s = br.readLine()) != null && id < Max_Peliculas){

				    // Tokeniza con la separacion "|"
				    StringTokenizer tokenizer = new StringTokenizer(s, "|");
				    // Primer token es el id de la película
				    id = Integer.parseInt(tokenizer.nextToken());
					// Segundo token es el nombre
				    String nombre = tokenizer.nextToken();
					// Tercer token fecha de la película
	 			    String fecha = tokenizer.nextToken();
					// Cuarto token duración
	 			    String duracion = tokenizer.nextToken();
					//Quinto token rating
	 			    int rating = Integer.parseInt(tokenizer.nextToken());

				    insert(connection, id, nombre, fecha, duracion, rating);
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
