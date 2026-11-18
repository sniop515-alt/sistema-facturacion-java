package sistemafacturacion;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Proceso principal: registrar una factura.
 * Elige cliente y empleado, agrega productos, calcula el total,
 * guarda factura + detalle y descuenta el stock (todo en una transacción).
 */
public class FrmFactura extends JFrame {

    /** Elemento de combo: guarda el id y lo que se muestra. */
    private static class Item {
        final int id; final String texto; final double precio; final int stock;
        Item(int id, String texto, double precio, int stock) {
            this.id = id; this.texto = texto; this.precio = precio; this.stock = stock;
        }
        @Override public String toString() { return texto; }
    }

    private final JComboBox<Item> cbCliente = new JComboBox<>();
    private final JComboBox<Item> cbEmpleado = new JComboBox<>();
    private final JComboBox<Item> cbProducto = new JComboBox<>();
    private final JSpinner spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JLabel lblTotal = new JLabel("Total: $0.00");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID prod.", "Producto", "Cantidad", "Precio", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private accesobd bd;

    public FrmFactura() {
        setTitle("Nueva factura");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel datos = new JPanel(new GridLayout(2, 2, 6, 6));
        datos.setBorder(BorderFactory.createTitledBorder("Datos de la factura"));
        datos.add(new JLabel("Cliente:"));  datos.add(cbCliente);
        datos.add(new JLabel("Empleado:")); datos.add(cbEmpleado);

        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linea.setBorder(BorderFactory.createTitledBorder("Agregar producto"));
        JButton bAgregar = new JButton("Agregar"), bQuitar = new JButton("Quitar seleccionado");
        linea.add(new JLabel("Producto:")); linea.add(cbProducto);
        linea.add(new JLabel("Cantidad:")); linea.add(spCantidad);
        linea.add(bAgregar); linea.add(bQuitar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(datos, BorderLayout.NORTH);
        norte.add(linea, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton bGuardar = new JButton("Guardar factura");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));
        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        sur.add(lblTotal, BorderLayout.WEST);
        sur.add(bGuardar, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);

        try {
            bd = new accesobd("localhost", "root", "", "tienda_facturacion");
            bd.conectaBD();
            cargarCombos();
        } catch (Exception e) {
            error(e.getMessage());
        }

        bAgregar.addActionListener(e -> agregar());
        bQuitar.addActionListener(e -> {
            int f = tabla.getSelectedRow();
            if (f >= 0) { modelo.removeRow(f); recalcular(); }
        });
        bGuardar.addActionListener(e -> guardar());
    }

    private void cargarCombos() throws SQLException {
        try (Statement st = bd.getConexion().createStatement()) {
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_cliente, CONCAT(cedula_ruc,' - ',apellidos,' ',nombres) t FROM cliente ORDER BY apellidos")) {
                while (rs.next()) cbCliente.addItem(new Item(rs.getInt(1), rs.getString(2), 0, 0));
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_empleado, CONCAT(apellidos,' ',nombres) t FROM empleado ORDER BY apellidos")) {
                while (rs.next()) cbEmpleado.addItem(new Item(rs.getInt(1), rs.getString(2), 0, 0));
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_producto, nombre, precio, stock FROM producto WHERE stock > 0 ORDER BY nombre")) {
                while (rs.next())
                    cbProducto.addItem(new Item(rs.getInt(1),
                            rs.getString(2) + " ($" + rs.getBigDecimal(3) + ", stock " + rs.getInt(4) + ")",
                            rs.getDouble(3), rs.getInt(4)));
            }
        }
    }

    private void agregar() {
        Item p = (Item) cbProducto.getSelectedItem();
        if (p == null) { error("No hay productos disponibles"); return; }
        int cant = (Integer) spCantidad.getValue();
        // cantidad ya agregada del mismo producto
        int yaAgregada = 0;
        for (int i = 0; i < modelo.getRowCount(); i++)
            if ((Integer) modelo.getValueAt(i, 0) == p.id) yaAgregada += (Integer) modelo.getValueAt(i, 2);
        if (cant + yaAgregada > p.stock) { error("Stock insuficiente (disponible: " + p.stock + ")"); return; }
        double sub = cant * p.precio;
        String nombre = p.texto.substring(0, p.texto.lastIndexOf(" ($"));
        modelo.addRow(new Object[]{p.id, nombre, cant, p.precio, sub});
        recalcular();
    }

    private double recalcular() {
        double total = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) total += (Double) modelo.getValueAt(i, 4);
        lblTotal.setText(String.format("Total: $%.2f", total));
        return total;
    }

    private void guardar() {
        if (modelo.getRowCount() == 0) { error("Agregue al menos un producto"); return; }
        Item cliente = (Item) cbCliente.getSelectedItem();
        Item empleado = (Item) cbEmpleado.getSelectedItem();
        if (cliente == null || empleado == null) { error("Seleccione cliente y empleado"); return; }

        Connection c = bd.getConexion();
        try {
            c.setAutoCommit(false);
            int idFactura;
            try (PreparedStatement pf = c.prepareStatement(
                    "INSERT INTO factura (id_cliente, id_empleado, total) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pf.setInt(1, cliente.id);
                pf.setInt(2, empleado.id);
                pf.setDouble(3, recalcular());
                pf.executeUpdate();
                try (ResultSet k = pf.getGeneratedKeys()) { k.next(); idFactura = k.getInt(1); }
            }
            try (PreparedStatement pd = c.prepareStatement(
                    "INSERT INTO detalle_factura (id_factura, id_producto, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)");
                 PreparedStatement ps = c.prepareStatement(
                    "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?")) {
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    int idProd = (Integer) modelo.getValueAt(i, 0);
                    int cant = (Integer) modelo.getValueAt(i, 2);
                    ps.setInt(1, cant); ps.setInt(2, idProd); ps.setInt(3, cant);
                    if (ps.executeUpdate() == 0)
                        throw new SQLException("Stock insuficiente para: " + modelo.getValueAt(i, 1));
                    pd.setInt(1, idFactura);
                    pd.setInt(2, idProd);
                    pd.setInt(3, cant);
                    pd.setDouble(4, (Double) modelo.getValueAt(i, 3));
                    pd.setDouble(5, (Double) modelo.getValueAt(i, 4));
                    pd.executeUpdate();
                }
            }
            c.commit();
            JOptionPane.showMessageDialog(this, "Factura N° " + idFactura + " guardada");
            modelo.setRowCount(0);
            recalcular();
            cbProducto.removeAllItems();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id_producto, nombre, precio, stock FROM producto WHERE stock > 0 ORDER BY nombre")) {
                while (rs.next())
                    cbProducto.addItem(new Item(rs.getInt(1),
                            rs.getString(2) + " ($" + rs.getBigDecimal(3) + ", stock " + rs.getInt(4) + ")",
                            rs.getDouble(3), rs.getInt(4)));
            }
        } catch (SQLException e) {
            try { c.rollback(); } catch (SQLException ignorada) { }
            error(e.getMessage());
        } finally {
            try { c.setAutoCommit(true); } catch (SQLException ignorada) { }
        }
    }

    private void error(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
