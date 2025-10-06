# Laboratorio 1:Yu-Gi-Oh! Duel Lite - Desarrollo de Software III

Mini-app de escritorio en Java Swing que simula un duelo sencillo usando la API de YGOProDeck.

## Creado por:
- Santiago Villa Salazar
- Edgar Fabian Rueda Colonia

## Instrucciones de ejecución
- Requisitos: Java 17+ y Maven 3.8+ (o IDE con soporte Maven).
- Ejecutar desde IDE: correr la clase `co.univalle.ygo.App`.
- Ejecutar desde consola (proyecto Maven):
  ```bash
  mvn -q exec:java
  ```
## Capturas de ejecución

**Carga de API**  
<img src="https://i.imgur.com/nycFbM8.png" alt="Carga de API" width="700"/>

**API cargada**  
<img src="https://i.imgur.com/JQ1rsB6.png" alt="API cargada" width="700"/>

**Batalla con la IA**  
<img src="https://i.imgur.com/pHILsxv.png" alt="Batalla con la IA" width="700"/>

**Reinicio de la App**  
<img src="https://i.imgur.com/H2T35HX.png" alt="Reinicio de la App" width="700"/>

## Explicación de diseño
La aplicación está organizada por capas para separar responsabilidades. El cliente de API (`co.univalle.ygo.api.ClienteYgoApi`) usa `java.net.http.HttpClient` para consultar YGOProDeck y obtener cartas tipo Monster desde el endpoint `cardinfo.php`, aplicando un tipo de monstruo y un desplazamiento aleatorio. De cada carta se toman nombre, ATK, DEF e imagen oficial.

La lógica del duelo se concentra en `co.univalle.ygo.logica.Duelo`, que resuelve los turnos y actualiza el puntaje. La UI (`co.univalle.ygo.interfaz.VentanaPrincipal`) se desacopla mediante la interfaz `co.univalle.ygo.logica.BattleListener`, que notifica `onTurn`, `onScoreChanged` y `onDuelEnded`. El modo de batalla es único y simple: se compara ATK vs ATK; el primero en llegar a 2 puntos gana.


## Notas de implementación
- `ClienteYgoApi.obtenerCartaMonstruoAleatoria()`:
  - Intenta hasta 20 veces `randomcard.php` y valida Monster + ATK/DEF + imagen.
  - Si no logra obtener una carta válida, recurre a `cardinfo.php` con filtros de tipos de monstruo y paginación aleatoria.
- `BattleListener` expone métodos en inglés acorde al enunciado:
  - `onTurn(String playerCard, String aiCard, String winner)`
  - `onScoreChanged(int playerScore, int aiScore)`
  - `onDuelEnded(String winner)`
- La UI usa hilos de fondo (`ExecutorService`) y actualiza componentes en el EDT (`SwingUtilities.invokeLater`).
