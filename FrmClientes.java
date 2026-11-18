package sistemafacturacion;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Formulario CRUD de clientes. */
public class FrmClientes extends JFrame {
    private final JTextField txtId = new JTextField(), txtCedula = new JTextField(),
            txtNombres = new JTextField(), txtApellidos = new JTextField(),
            txtDireccion = new JTextField(), txtTelefono = new JTextField();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Cédula/RUC", "Nombres", "Apellidos", "Dirección", "Teléfono"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private ClienteBeans cli;

    public FrmClientes() {
        setTitle("Clientes");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        txtId.setEditable(false);
        JPanel form = new JPanel(new GridLayout(6, 2, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Datos del cliente"));
        form.add(new JLabel("ID:"));            form.add(txtId);
        form.add(new JLabel("Cédula/RUC:"));    form.add(txtCedula);
        form.add(new JLabel("Nombres:"));       form.add(txtNombres);
        form.add(new JLabel("Apellidos:"));     form.add(txtApellidos);
        form.add(new JLabel("Dirección:"));     form.add(txtDireccion);
        form.add(new JLabel("Teléfono:"));      form.add(txtTelefono);

        JButton bNuevo = new JButton("Nuevo"), bGuardar = new JButton("Guardar"),
                bActualizar = new JButton("Actualizar"), bEliminar = new JButton("Eliminar");
        JPanel botones = new JPanel();
        botones.add(bNuevo); botones.add(bGuardar); botones.add(bActualizar); botones.add(bEliminar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        try {
            cli = new ClienteBeans();
            cargarTabla();
        } catch (Exception e) {
            error(e);
        }

        tabla.getSelectionModel().addListSelectionListener(ev -> {
            int f = tabla.getSelectedRow();
            if (!ev.getValueIsAdjusting() && f >= 0) {
                txtId.setText(String.valueOf(modelo.getValueAt(f, 0)));
                txtCedula.setText(String.valueOf(modelo.getValueAt(f, 1)));
                txtNombres.setText(String.valueOf(modelo.getValueAt(f, 2)));
                txtApellidos.setText(String.valueOf(modelo.getValueAt(f, 3)));
                txtDireccion.setText(String.valueOf(modelo.getValueAt(f, 4)));
                txtTelefono.setText(String.valueOf(modelo.getValueAt(f, 5)));
            }
        });

        bNuevo.addActionListener(e -> limpiar());
        bGuardar.addActionListener(e -> ejecutar(() -> { leer(); cli.insertarCliente(); }, "Cliente guardado"));
        bActualizar.addActionListener(e -> {
            if (txtId.getText().isEmpty()) { aviso("Seleccione un cliente de la tabla"); return; }
            ejecutar(() -> { leer(); cli.setIdCliente(Integer.parseInt(txtId.getText())); cli.actualizarCliente(); },
                    "Cliente actualizado");
        });
        bEliminar.addActionListener(e -> {
            if (txtId.getText().isEmpty()) { aviso("Seleccione un cliente de la tabla"); return; }
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar este cliente?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ejecutar(() -> { cli.setIdCliente(Integer.parseInt(txtId.getText())); cli.eliminarCliente(); },
                        "Cliente eliminado");
            }
        });
    }

    private interface Accion { void run() throws Exception; }

    private void ejecutar(Accion a, String ok) {
        try {
            a.run();
            cargarTabla();
            limpiar();
            aviso(ok);
        } catch (Exception e) {
            error(e);
        }
    }

    private void leer() {
        if (txtCedula.getText().trim().isEmpty() || txtNombres.getText().trim().isEmpty()
                || txtApellidos.getText().trim().isEmpty())
            throw new IllegalArgumentException("Cédula, nombres y apellidos son obligatorios");
        cli.setCedulaRuc(txtCedula.getText().trim());
        cli.setNombres(txtNombres.getText().trim());
        cli.setApellidos(txtApellidos.getText().trim());
        cli.setDireccion(txtDireccion.getText().trim());
        cli.setTelefono(txtTelefono.getText().trim());
    }

    private void cargarTabla() throws Exception {
        modelo.setRowCount(0);
        List<ClienteBeans> lista = cli.listarClientes();
        for (ClienteBeans c : lista)
            modelo.addRow(new Object[]{c.getIdCliente(), c.getCedulaRuc(), c.getNombres(),
                    c.getApellidos(), c.getDireccion(), c.getTelefono()});
    }

    private void limpiar() {
        txtId.setText(""); txtCedula.setText(""); txtNombres.setText("");
        txtApellidos.setText(""); txtDireccion.setText(""); txtTelefono.setText("");
        tabla.clearSelection();
    }

    private void aviso(String m) { JOptionPane.showMessageDialog(this, m); }
    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
