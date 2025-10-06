package co.univalle.ygo.modelo;

public class Carta {
    private final String nombre;
    private final int ataque;
    private final int defensa;
    private final String urlImagen;

    public Carta(String nombre, int ataque, int defensa, String urlImagen) {
        this.nombre = nombre;
        this.ataque = ataque;
        this.defensa = defensa;
        this.urlImagen = urlImagen;
    }

    public String getNombre() { return nombre; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public String getUrlImagen() { return urlImagen; }

    @Override
    public String toString() {
        return nombre + " (ATK " + ataque + ", DEF " + defensa + ")";
    }
}
