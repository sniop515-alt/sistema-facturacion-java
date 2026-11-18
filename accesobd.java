package sistemafacturacion;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Clase de acceso a la base de datos (equivalente a "accesobd" del ejemplo).
 * Requiere el conector JDBC de MySQL (mysql-connector-j) en las Bibliotecas del proyecto.
 */
public class accesobd {
    private final String host, usuario, clave, baseDatos;
    private Connection con;

    public accesobd(String host, String usuario, String clave, String baseDatos) {
        this.host = host;
        this.usuario = usuario;
        this.clave = clave;
        this.baseDatos = baseDatos;
    }

    public void conectaBD() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://" + host + ":3306/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil";
        con = DriverManager.getConnection(url, usuario, clave);
        System.out.println("Se ha realizado la conexion con la BD....");
    }

    public Connection getConexion() {
        return con;
    }

    public void cerrar() {
        try {
            if (con != null) con.close();
        } catch (Exception e) {
            // se ignora
        }
    }
}
