package co.univalle.ygo.logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.univalle.ygo.modelo.Carta;

public class Duelo {
    private final Random azar = new Random();
    private final List<BattleListener> listeners = new ArrayList<>();

    private int puntajeJugador = 0;
    private int puntajeIA = 0;

    // Métodos existentes en español conservados para compatibilidad
    public void agregarEscucha(BattleListener l) { listeners.add(l); }
    public void quitarEscucha(BattleListener l) { listeners.remove(l); }

    public void resolverTurno(Carta cartaJugador, Carta cartaIA, boolean jugadorAtaca, boolean iaAtaca) {
        String ganador;
        if (jugadorAtaca == iaAtaca) {
            ganador = (cartaJugador.getAtaque() >= cartaIA.getAtaque()) ? "Jugador" : "IA";
        } else {
            boolean atacaJugador = jugadorAtaca && !iaAtaca;
            int atk = atacaJugador ? cartaJugador.getAtaque() : cartaIA.getAtaque();
            int def = atacaJugador ? cartaIA.getDefensa() : cartaJugador.getDefensa();
            ganador = (atk >= def) ? (atacaJugador ? "Jugador" : "IA") : (atacaJugador ? "IA" : "Jugador");
        }

        if ("Jugador".equals(ganador)) puntajeJugador++; else puntajeIA++;
        for (BattleListener l : listeners) l.onTurn(cartaJugador.getNombre(), cartaIA.getNombre(), ganador);
        for (BattleListener l : listeners) l.onScoreChanged(puntajeJugador, puntajeIA);

        if (puntajeJugador == 2 || puntajeIA == 2) {
            String g = puntajeJugador == 2 ? "Jugador" : "IA";
            for (BattleListener l : listeners) l.onDuelEnded(g);
        }
    }

    public boolean iniciaJugador() { return azar.nextBoolean(); }

    // Reinicia el estado del duelo (puntajes) conservando los listeners
    public void reset() {
        this.puntajeJugador = 0;
        this.puntajeIA = 0;
    }
}
