/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemafacturacion;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Consulta de facturas: lista general con filtro y detalle de la factura seleccionada. */
public class FrmVerFacturas extends JFrame {
    private static final String CLAVE = "";

    private final JTextField txtBuscar = new JTextField(18);
    private final DefaultTableModel modeloFact = new DefaultTableModel(
            new String[]{"N° Factura", "Fecha", "Cliente", "Empleado", "Total"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel modeloDet = new DefaultTableModel(
            new String[]{"Producto", "Cantidad", "Precio unit.", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaFact = new JTable(modeloFact);
    private final JTable tablaDet = new JTable(modeloDet);
    private final JLabel lblResumen = new JLabel(" ");
    private accesobd bd;

    public FrmVerFacturas() {
        setTitle("Ver facturas");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(850, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JButton bBuscar = new JButton("Buscar"), bTodas = new JButton("Ver todas");
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Cliente (nombre o cédula):"));
        filtro.add(txtBuscar); filtro.add(bBuscar); filtro.add(bTodas);
        add(filtro, BorderLayout.NORTH);

        JScrollPane spFact = new JScrollPane(tablaFact);
        spFact.setBorder(BorderFactory.createTitledBorder("Facturas"));
        JScrollPane spDet = new JScrollPane(tablaDet);
        spDet.setBorder(BorderFactory.createTitledBorder("Detalle de la factura seleccionada"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spFact, spDet);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        lblResumen.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));
        lblResumen.setFont(lblResumen.getFont().deriveFont(Font.BOLD));
        add(lblResumen, BorderLayout.SOUTH);

        try {
            bd = new accesobd("localhost", "root", CLAVE, "tienda_facturacion");
            bd.conectaBD();
            cargarFacturas("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        tablaFact.getSelectionModel().addListSelectionListener(ev -> {
            int f = tablaFact.getSelectedRow();
            if (!ev.getValueIsAdjusting() && f >= 0)
                cargarDetalle((Integer) modeloFact.getValueAt(f, 0));
        });
        bBuscar.addActionListener(e -> cargarFacturas(txtBuscar.getText().trim()));
        txtBuscar.addActionListener(e -> cargarFacturas(txtBuscar.getText().trim()));
        bTodas.addActionListener(e -> { txtBuscar.setText(""); cargarFacturas(""); });
    }

    private void cargarFacturas(String filtro) {
        modeloFact.setRowCount(0);
        modeloDet.setRowCount(0);
        String sql = "SELECT f.id_factura, f.fecha, CONCAT(c.apellidos,' ',c.nombres), "
                + "CONCAT(e.apellidos,' ',e.nombres), f.total "
                + "FROM factura f JOIN cliente c ON c.id_cliente = f.id_cliente "
                + "JOIN empleado e ON e.id_empleado = f.id_empleado "
                + "WHERE c.cedula_ruc LIKE ? OR c.nombres LIKE ? OR c.apellidos LIKE ? "
                + "ORDER BY f.id_factura DESC";
        double suma = 0;
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            String like = "%" + filtro + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modeloFact.addRow(new Object[]{rs.getInt(1), rs.getTimestamp(2),
                            rs.getString(3), rs.getString(4), rs.getDouble(5)});
                    suma += rs.getDouble(5);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        lblResumen.setText(String.format("Facturas: %d   |   Suma de totales: $%.2f",
                modeloFact.getRowCount(), suma));
    }

    private void cargarDetalle(int idFactura) {
        modeloDet.setRowCount(0);
        String sql = "SELECT p.nombre, d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM detalle_factura d JOIN producto p ON p.id_producto = d.id_producto "
                + "WHERE d.id_factura = ?";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    modeloDet.addRow(new Object[]{rs.getString(1), rs.getInt(2),
                            rs.getDouble(3), rs.getDouble(4)});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
