# Yu-Gi-Oh! Duel Lite

Mini app de escritorio en Java Swing que simula un duelo sencillo usando la API de YGOProDeck.

## Instrucciones de ejecución
- Requisitos: Java 17+ y Maven 3.8+ (o IDE con soporte Maven).
- Ejecutar desde IDE: correr la clase `co.univalle.ygo.App`.
- Ejecutar desde consola (proyecto Maven):
  ```bash
  mvn -q exec:java
  ```

## Breve explicación de diseño
La aplicación está organizada por capas para separar responsabilidades. El cliente de API (`co.univalle.ygo.api.ClienteYgoApi`) usa `java.net.http.HttpClient` para consultar YGOProDeck y obtener cartas tipo Monster desde el endpoint `cardinfo.php`, aplicando un tipo de monstruo y un desplazamiento aleatorio. De cada carta se toman nombre, ATK, DEF e imagen oficial.

La lógica del duelo se concentra en `co.univalle.ygo.logica.Duelo`, que resuelve los turnos y actualiza el puntaje. La UI (`co.univalle.ygo.interfaz.VentanaPrincipal`) se desacopla mediante la interfaz `co.univalle.ygo.logica.BattleListener`, que notifica `onTurn`, `onScoreChanged` y `onDuelEnded`. El modo de batalla es único y simple: se compara ATK vs ATK; el primero en llegar a 2 puntos gana.

## Capturas de pantalla
Añade aquí 2–3 capturas del funcionamiento (carga de cartas, duelo en curso, ganador final).
## Notas de implementación
- `ClienteYgoApi.obtenerCartaMonstruoAleatoria()`:
  - Intenta hasta 20 veces `randomcard.php` y valida Monster + ATK/DEF + imagen.
  - Si no logra obtener una carta válida, recurre a `cardinfo.php` con filtros de tipos de monstruo y paginación aleatoria.
- `BattleListener` expone métodos en inglés acorde al enunciado:
  - `onTurn(String playerCard, String aiCard, String winner)`
  - `onScoreChanged(int playerScore, int aiScore)`
  - `onDuelEnded(String winner)`
- La UI usa hilos de fondo (`ExecutorService`) y actualiza componentes en el EDT (`SwingUtilities.invokeLater`).

## Problemas comunes
- Si `mvn` no es reconocido en consola, ejecuta desde el IDE o instala/añade Maven al PATH.
- Si fallan descargas de dependencias, pulsa “Reload Maven Project” en el IDE.
- Si la API limita tráfico (rate limit), espera unos segundos y vuelve a intentar cargar cartas.
