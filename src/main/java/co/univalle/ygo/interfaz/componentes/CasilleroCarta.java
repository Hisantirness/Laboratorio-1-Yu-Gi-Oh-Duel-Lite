package co.univalle.ygo.interfaz.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.border.EmptyBorder;
import javax.swing.*;
import co.univalle.ygo.modelo.Carta;

public class CasilleroCarta extends JPanel {
    private final JLabel etiquetaImagen = new JLabel("Imagen", SwingConstants.CENTER);
    private final JLabel etiquetaMeta = new JLabel("Nombre/ATK/DEF", SwingConstants.CENTER);
    private Carta carta;

    public CasilleroCarta(String titulo) {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(titulo));
        // Tamaños recomendados para carta 9:16 aprox.
        setPreferredSize(new Dimension(360, 580));

        etiquetaImagen.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaImagen.setVerticalAlignment(SwingConstants.CENTER);
        etiquetaImagen.setPreferredSize(new Dimension(340, 500));
        etiquetaImagen.setBorder(new EmptyBorder(8,8,8,8));

        etiquetaMeta.setBorder(new EmptyBorder(4,8,8,8));

        add(etiquetaImagen, BorderLayout.CENTER);
        add(etiquetaMeta, BorderLayout.SOUTH);
    }

    public void setCarta(Carta c, ImageIcon icono) {
        this.carta = c;
        etiquetaImagen.setText(null);
        etiquetaImagen.setIcon(icono);
        if (c != null) etiquetaMeta.setText("<html><center>"+c.getNombre()+"<br/>ATK "+c.getAtaque()+" DEF "+c.getDefensa()+"</center></html>");
        else etiquetaMeta.setText("Nombre/ATK/DEF");
    }

    public void mostrandoCarga() {
        etiquetaImagen.setIcon(null);
        etiquetaImagen.setText("Cargando...");
        etiquetaMeta.setText("...");
    }

    public Carta getCarta() { return carta; }
}
