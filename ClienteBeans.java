package sistemafacturacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Bean de la tabla cliente con sus operaciones CRUD. */
public class ClienteBeans {
    private int idCliente;
    private String cedulaRuc, nombres, apellidos, direccion, telefono;
    private final accesobd bd;

    public ClienteBeans() throws Exception {
        // CAMBIA "TU_CLAVE" por la contraseña de tu MySQL
        bd = new accesobd("localhost", "root", "TU_CLAVE", "tienda_facturacion");
        bd.conectaBD();
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getCedulaRuc() { return cedulaRuc; }
    public void setCedulaRuc(String cedulaRuc) { this.cedulaRuc = cedulaRuc; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public void insertarCliente() throws SQLException {
        String sql = "INSERT INTO cliente (cedula_ruc, nombres, apellidos, direccion, telefono) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, cedulaRuc);
            ps.setString(2, nombres);
            ps.setString(3, apellidos);
            ps.setString(4, direccion);
            ps.setString(5, telefono);
            ps.executeUpdate();
        }
    }

    public void actualizarCliente() throws SQLException {
        String sql = "UPDATE cliente SET cedula_ruc=?, nombres=?, apellidos=?, direccion=?, telefono=? WHERE id_cliente=?";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, cedulaRuc);
            ps.setString(2, nombres);
            ps.setString(3, apellidos);
            ps.setString(4, direccion);
            ps.setString(5, telefono);
            ps.setInt(6, idCliente);
            ps.executeUpdate();
        }
    }

    public void eliminarCliente() throws SQLException {
        try (PreparedStatement ps = bd.getConexion().prepareStatement("DELETE FROM cliente WHERE id_cliente=?")) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    /** Busca por cédula/RUC y carga los datos en el bean. */
    public boolean consultarCliente(String cedula) throws SQLException {
        try (PreparedStatement ps = bd.getConexion().prepareStatement("SELECT * FROM cliente WHERE cedula_ruc=?")) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                cargar(rs, this);
                return true;
            }
        }
    }

    public List<ClienteBeans> listarClientes() throws Exception {
        List<ClienteBeans> lista = new ArrayList<>();
        try (Statement st = bd.getConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM cliente ORDER BY apellidos, nombres")) {
            while (rs.next()) {
                ClienteBeans c = new ClienteBeans();
                cargar(rs, c);
                lista.add(c);
            }
        }
        return lista;
    }

    private static void cargar(ResultSet rs, ClienteBeans c) throws SQLException {
        c.idCliente = rs.getInt("id_cliente");
        c.cedulaRuc = rs.getString("cedula_ruc");
        c.nombres = rs.getString("nombres");
        c.apellidos = rs.getString("apellidos");
        c.direccion = rs.getString("direccion");
        c.telefono = rs.getString("telefono");
    }
}
