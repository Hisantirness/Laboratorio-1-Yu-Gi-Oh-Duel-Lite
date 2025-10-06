package co.univalle.ygo;

import javax.swing.SwingUtilities;
import co.univalle.ygo.interfaz.VentanaPrincipal;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}
