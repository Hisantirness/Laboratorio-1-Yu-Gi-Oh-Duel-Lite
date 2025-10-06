package co.univalle.ygo.logica;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.univalle.ygo.modelo.Carta;

public class DueloTest {

    private Duelo duelo;

    @BeforeEach
    void setUp() {
        duelo = new Duelo();
    }

    @Test
    void ambosEnAtaque_ganaMayorATK() {
        Carta c1 = new Carta("A", 2000, 1500, null);
        Carta c2 = new Carta("B", 1800, 2000, null);
        final String[] ganador = new String[1];
        duelo.agregarEscucha(new BattleListener() {
            @Override public void onTurn(String pc, String ac, String g) { ganador[0] = g; }
            @Override public void onScoreChanged(int pj, int pi) {}
            @Override public void onDuelEnded(String g) {}
        });
        duelo.resolverTurno(c1, c2, true, true);
        assertEquals("Jugador", ganador[0]);
    }

    @Test
    void ataqueVsDefensa_comparaAtkVsDef_delDefensor() {
        Carta atacante = new Carta("Atk", 1500, 1000, null);
        Carta defensor = new Carta("Def", 1400, 1600, null);
        final String[] ganador = new String[1];
        duelo.agregarEscucha(new BattleListener() {
            @Override public void onTurn(String pc, String ac, String g) { ganador[0] = g; }
            @Override public void onScoreChanged(int pj, int pi) {}
            @Override public void onDuelEnded(String g) {}
        });
        // Jugador ataca, IA defiende: 1500 vs 1600 => gana IA
        duelo.resolverTurno(atacante, defensor, true, false);
        assertEquals("IA", ganador[0]);
    }

    @Test
    void terminaAlAlcanzarDosPuntos() {
        Carta fuerte = new Carta("Fuerte", 3000, 3000, null);
        Carta debil = new Carta("Debil", 1000, 1000, null);
        final String[] ganadorDuelo = new String[1];
        duelo.agregarEscucha(new BattleListener() {
            @Override public void onTurn(String pc, String ac, String g) {}
            @Override public void onScoreChanged(int pj, int pi) {}
            @Override public void onDuelEnded(String g) { ganadorDuelo[0] = g; }
        });
        duelo.resolverTurno(fuerte, debil, true, true);
        duelo.resolverTurno(fuerte, debil, true, true);
        assertEquals("Jugador", ganadorDuelo[0]);
    }
}
