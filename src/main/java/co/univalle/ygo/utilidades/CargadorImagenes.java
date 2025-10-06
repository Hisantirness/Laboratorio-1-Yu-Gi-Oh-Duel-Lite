package co.univalle.ygo.utilidades;

import java.awt.Image;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.io.ByteArrayInputStream;

public class CargadorImagenes {
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();

    public static ImageIcon cargar(String url, int maxAncho, int maxAlto) throws IOException, InterruptedException {
        if (url == null || url.isBlank()) return null;
        if (cache.containsKey(url)) return cache.get(url);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) throw new IOException("HTTP " + resp.statusCode());
        try (ByteArrayInputStream in = new ByteArrayInputStream(resp.body())) {
            Image img = ImageIO.read(in);
            if (img == null) throw new IOException("Imagen inválida");
            Image escalada = img.getScaledInstance(maxAncho, maxAlto, Image.SCALE_SMOOTH);
            ImageIcon icono = new ImageIcon(escalada);
            cache.put(url, icono);
            return icono;
        }
    }
}
