package sistemafacturacion;

import java.awt.GridLayout;
import javax.swing.*;

/** Menú principal del sistema. */
public class Principal extends JFrame {

    public Principal() {
        setTitle("Sistema de facturación");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(320, 180);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1, 10, 10));
        JButton bClientes = new JButton("Clientes");
        JButton bFactura = new JButton("Nueva factura");
        JButton bSalir = new JButton("Salir");
        add(bClientes); add(bFactura); add(bSalir);
        bClientes.addActionListener(e -> new FrmClientes().setVisible(true));
        bFactura.addActionListener(e -> new FrmFactura().setVisible(true));
        bSalir.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Principal().setVisible(true));
    }
}
