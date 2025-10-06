package co.univalle.ygo.interfaz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

import co.univalle.ygo.api.ClienteYgoApi;
import co.univalle.ygo.interfaz.componentes.CasilleroCarta;
import co.univalle.ygo.logica.Duelo;
import co.univalle.ygo.logica.BattleListener;
import co.univalle.ygo.modelo.Carta;
import co.univalle.ygo.utilidades.CargadorImagenes;

public class VentanaPrincipal extends JFrame implements BattleListener {
    private final JTextArea areaLog = new JTextArea();
    private final JLabel etiquetaMarcador = new JLabel("Marcador: 0 - 0");
    private final JLabel etiquetaEstado = new JLabel("Estado: esperando carga");

    private final CasilleroCarta[] casillerosJugador = new CasilleroCarta[3];
    private final CasilleroCarta[] casillerosIA = new CasilleroCarta[3];
    private final JButton[] botonesElegir = new JButton[3];
    private final JButton botonCargar = new JButton("Cargar cartas");
    private final JButton botonIniciar = new JButton("Iniciar duelo");
    private final JButton botonReiniciar = new JButton("Reiniciar");

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final ClienteYgoApi api = new ClienteYgoApi();
    private final Random azar = new Random();
    private boolean finalLogged = false;

    // Estado de juego
    private final List<Carta> cartasJugador = new ArrayList<>();
    private final List<Carta> cartasIA = new ArrayList<>();
    private final boolean[] usadaJugador = new boolean[3];
    private final boolean[] usadaIA = new boolean[3];
    private boolean dueloIniciado = false;
    private final Duelo duelo = new Duelo();

    public VentanaPrincipal() {
        super("Yu-Gi-Oh! Duel Lite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        // Abrir maximizada
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setLayout(new BorderLayout(10,10));

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        superior.add(botonCargar);
        superior.add(botonIniciar);
        superior.add(botonReiniciar);
        // Modo único (ATK vs ATK): se ocultan controles de ataque/defensa
        superior.add(new JLabel(" | "));
        superior.add(etiquetaMarcador);
        superior.add(new JLabel(" | "));
        superior.add(etiquetaEstado);
        getContentPane().add(superior, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(2,1,10,10));
        centro.add(construirPanel("Jugador", casillerosJugador, botonesElegir));
        centro.add(construirPanel("Máquina", casillerosIA, null));
        getContentPane().add(centro, BorderLayout.CENTER);

        areaLog.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setPreferredSize(new Dimension(200, 200));
        getContentPane().add(scroll, BorderLayout.SOUTH);

        botonCargar.addActionListener(e -> cargarCartas());
        botonIniciar.addActionListener(e -> iniciarDuelo());
        botonIniciar.setEnabled(false);
        botonReiniciar.addActionListener(e -> reiniciar());

        for (int i = 0; i < botonesElegir.length; i++) {
            final int idx = i;
            if (botonesElegir[i] != null) {
                botonesElegir[i].addActionListener(e -> onElegir(idx));
            }
        }

        duelo.agregarEscucha(this);
    }

    private JPanel construirPanel(String titulo, CasilleroCarta[] casilleros, JButton[] botones) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        JPanel cartas = new JPanel(new GridLayout(1,3,10,10));
        for (int i=0;i<3;i++) {
            CasilleroCarta slot = new CasilleroCarta("Carta "+(i+1));
            casilleros[i] = slot;
            cartas.add(slot);
        }
        panel.add(cartas, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (botones != null) {
            for (int i=0;i<3;i++) {
                JButton elegir = new JButton("Elegir "+(i+1));
                elegir.setEnabled(false);
                botones[i] = elegir;
                acciones.add(elegir);
            }
        }
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarCartas() {
        botonCargar.setEnabled(false);
        botonIniciar.setEnabled(false);
        etiquetaEstado.setText("Estado: cargando cartas...");
        agregarLog("Cargando 3 cartas para cada jugador...");
        cartasJugador.clear();
        cartasIA.clear();
        for (int i=0;i<3;i++) { usadaJugador[i]=false; usadaIA[i]=false; }
        for (int i=0;i<3;i++) {
            casillerosJugador[i].mostrandoCarga();
            casillerosIA[i].mostrandoCarga();
            if (botonesElegir[i] != null) botonesElegir[i].setEnabled(false);
        }
        AtomicInteger restantes = new AtomicInteger(6);
        for (int i=0;i<3;i++) {
            final int idx = i;
            pool.submit(() -> traerAlCasillero(casillerosJugador[idx], true, idx, restantes));
            pool.submit(() -> traerAlCasillero(casillerosIA[idx], false, idx, restantes));
        }
    }

    private void traerAlCasillero(CasilleroCarta casillero, boolean esJugador, int idx, AtomicInteger restantes) {
        try {
            Carta carta = api.obtenerCartaMonstruoAleatoria();
            ImageIcon icono = null;
            try {
                // Escala más alta para cartas 9:16 aprox.
                icono = CargadorImagenes.cargar(carta.getUrlImagen(), 300, 430);
            } catch (Exception ex) {
                // imagen opcional
            }
            ImageIcon finalIcono = icono;
            SwingUtilities.invokeLater(() -> {
                casillero.setCarta(carta, finalIcono);
                if (esJugador) extendLista(cartasJugador, idx, carta); else extendLista(cartasIA, idx, carta);
                if (esJugador && botonesElegir[idx] != null) botonesElegir[idx].setEnabled(true);
                agregarLog((esJugador?"Jugador":"Máquina")+" recibió: "+carta);
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                casillero.setBorder(BorderFactory.createTitledBorder("Error"));
                agregarLog("Error cargando carta: "+ex.getMessage());
            });
        } finally {
            int faltan = restantes.decrementAndGet();
            if (faltan == 0) {
                SwingUtilities.invokeLater(() -> {
                    agregarLog("Cartas listas. Puedes iniciar el duelo.");
                    etiquetaEstado.setText("Estado: listo para iniciar");
                    botonCargar.setEnabled(true);
                    botonIniciar.setEnabled(cartasJugador.size()==3 && cartasIA.size()==3);
                });
            }
        }
    }

    private static <T> void extendLista(List<T> lista, int index, T valor) {
        while (lista.size() <= index) lista.add(null);
        lista.set(index, valor);
    }

    private void onElegir(int idx) {
        if (!dueloIniciado) { agregarLog("Primero inicia el duelo."); return; }
        if (usadaJugador[idx]) { agregarLog("Esa carta ya fue usada."); return; }
        Carta elegida = casillerosJugador[idx].getCarta();
        if (elegida == null) return;
        agregarLog("Jugador elige: "+elegida);
        usadaJugador[idx] = true;
        if (botonesElegir[idx] != null) botonesElegir[idx].setEnabled(false);

        // IA elige una de sus cartas no usadas al azar
        int iaIdx = elegirIndiceNoUsado(usadaIA);
        if (iaIdx == -1) { agregarLog("IA sin cartas disponibles"); return; }
        Carta cartaIA = casillerosIA[iaIdx].getCarta();
        usadaIA[iaIdx] = true;
        // resaltar carta IA elegida
        casillerosIA[iaIdx].setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE,2),
                "Carta "+(iaIdx+1)+" (IA)"));

        // Resolver turno (ambos en ataque para simplificar la regla)
        // Modo único: comparar ATK vs ATK
        duelo.resolverTurno(elegida, cartaIA, true, true);
    }

    private int elegirIndiceNoUsado(boolean[] usadas) {
        int[] indices = new int[3]; int c=0;
        for (int i=0;i<3;i++) if (!usadas[i]) indices[c++]=i;
        if (c==0) return -1;
        return indices[azar.nextInt(c)];
    }

    private void iniciarDuelo() {
        if (cartasJugador.size()<3 || cartasIA.size()<3) { agregarLog("Faltan cartas."); return; }
        dueloIniciado = true;
        finalLogged = false;
        etiquetaEstado.setText("Estado: duelo en curso");
        agregarLog("Duelo iniciado. Primer turno: " + (duelo.iniciaJugador()?"Jugador":"Máquina"));
        // Bloquear carga de cartas mientras el duelo está en curso
        botonCargar.setEnabled(false);
    }

    private void reiniciar() {
        dueloIniciado = false;
        finalLogged = false;
        etiquetaMarcador.setText("Marcador: 0 - 0");
        etiquetaEstado.setText("Estado: esperando carga");
        areaLog.setText("");
        // Reiniciar puntajes internos del duelo
        duelo.reset();
        // Permitir cargar cartas nuevamente
        botonCargar.setEnabled(true);
        for (int i=0;i<3;i++) { usadaJugador[i]=false; usadaIA[i]=false; }
        for (int i=0;i<3;i++) {
            casillerosJugador[i].setBorder(BorderFactory.createTitledBorder("Carta "+(i+1)));
            casillerosIA[i].setBorder(BorderFactory.createTitledBorder("Carta "+(i+1)));
            casillerosJugador[i].mostrandoCarga();
            casillerosIA[i].mostrandoCarga();
            if (botonesElegir[i] != null) botonesElegir[i].setEnabled(false);
        }
    }

    private void agregarLog(String texto) {
        areaLog.append(texto+"\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    // BattleListener
    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        agregarLog("Turno: Jugador lanzó '"+playerCard+"' vs IA '"+aiCard+"' -> Gana: "+winner);
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        etiquetaMarcador.setText("Marcador: "+playerScore+" - "+aiScore);
        // Robustez: si alguien llega a 2, cerramos el duelo y registramos ganador
        if (!finalLogged && dueloIniciado && (playerScore == 2 || aiScore == 2)) {
            String winner = (playerScore == 2) ? "Jugador" : "IA";
            etiquetaEstado.setText("Estado: duelo finalizado");
            agregarLog("Ganador final: "+winner);
            for (JButton b : botonesElegir) if (b!=null) b.setEnabled(false);
            dueloIniciado = false;
            finalLogged = true;
            // Rehabilitar carga de cartas tras terminar el duelo
            botonCargar.setEnabled(true);
        }
    }

    @Override
    public void onDuelEnded(String winner) {
        if (!finalLogged) {
            etiquetaEstado.setText("Estado: duelo finalizado");
            agregarLog("Ganador final: "+winner);
            for (JButton b : botonesElegir) if (b!=null) b.setEnabled(false);
            dueloIniciado = false;
            finalLogged = true;
            // Rehabilitar carga de cartas tras terminar el duelo
            botonCargar.setEnabled(true);
        }
    }
}
