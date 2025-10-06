package co.univalle.ygo.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONArray;

import co.univalle.ygo.modelo.Carta;

public class ClienteYgoApi {
    private static final String URL_INFO = "https://db.ygoprodeck.com/api/v7/cardinfo.php";
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public Carta obtenerCartaMonstruoAleatoria() throws IOException, InterruptedException {
        // Único flujo: cardinfo.php filtrando tipos Monster (rápido y estable)
        String[] tipos = new String[] {
            "Normal Monster","Effect Monster","Ritual Monster","Fusion Monster",
            "Synchro Monster","XYZ Monster","Pendulum Effect Monster","Link Monster"
        };
        Random r = new Random();
        for (int intento = 0; intento < 15; intento++) {
            String tipo = tipos[r.nextInt(tipos.length)];
            int offset = r.nextInt(900);
            String url = URL_INFO + "?type=" + java.net.URLEncoder.encode(tipo, java.nio.charset.StandardCharsets.UTF_8)
                    + "&num=1&offset=" + offset;
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "YGO-Duel-Lite/0.1 (Java)")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) { Thread.sleep(60); continue; }
            try {
                JSONObject root = new JSONObject(resp.body());
                if (!root.has("data")) { Thread.sleep(80); continue; }
                JSONArray data = root.getJSONArray("data");
                if (data.length() == 0) { Thread.sleep(50); continue; }
                JSONObject obj = data.getJSONObject(0);
                if (!obj.has("atk") || !obj.has("def")) { Thread.sleep(40); continue; }
                String nombre = obj.optString("name", "?");
                int ataque = obj.optInt("atk", 0);
                int defensa = obj.optInt("def", 0);
                String urlImagen = null;
                if (obj.has("card_images") && obj.getJSONArray("card_images").length() > 0) {
                    urlImagen = obj.getJSONArray("card_images").getJSONObject(0).optString("image_url", null);
                }
                if (urlImagen == null) { Thread.sleep(40); continue; }
                return new Carta(nombre, ataque, defensa, urlImagen);
            } catch (Exception ex) {
                Thread.sleep(50);
            }
        }
        throw new IOException("No se pudo obtener una carta Monster tras varios intentos");
    }
}
