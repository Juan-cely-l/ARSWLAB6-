# ARSWLAB6-
````markdown
# ARSWLAB6 – Spring WebSockets + React


##  Estructura del proyecto

```text
C:.
├── .idea/                     # archivos del IDE
├── .mvn/wrapper/              # wrapper de Maven
├── src
│   ├── main
│   │   ├── java
│   │   │   └── tutorial/sockets/ARSWLAB6
│   │   │       ├── components     # lógica de envío programado (broadcast cada 5s)
│   │   │       ├── configuration  # configuración de WebSocket y scheduling
│   │   │       ├── controller     # controlador/endpoint REST básico (p.ej. /status)
│   │   │       └── endpoints      # endpoint WebSocket real (p.ej. /timer)
│   │   └── resources
│   │       └── static
│   │           └── js             # componente React (WsComponent.jsx / WSClient)
│   └── test
│       └── java
│           └── tutorial/sockets/ARSWLAB6
└── target/                       # artefactos compilados
````


---

##  ¿Qué hace la app?

1. **Arranca un servidor Spring Boot** que expone:

    * Un endpoint REST de prueba (similar a `/status`) para comprobar que el servidor está levantado.
    * Un endpoint WebSocket en `/timer` (o ruta equivalente en tu paquete) que acepta conexiones WebSocket.
2. **Registra todas las sesiones WebSocket abiertas** en una cola concurrente.
3. **Cada 5 segundos** un bean `@Component` con `@Scheduled(fixedRate = 5000)` envía un mensaje a **todas** las sesiones conectadas:

   > `"The time is now HH:mm:ss"`
   > Esto es el “broadcast” del servidor.
4. En el **cliente web** (HTML + React en `/src/main/resources/static`) se crea un `WebSocket("ws://localhost:8080/timer")`, se escuchan los mensajes y se actualiza el DOM mostrando la hora recibida.

---

##  Componentes principales (lado servidor)

### 1. Endpoint WebSocket (`endpoints/…`)

Clase anotada con:

```java
@Component
@ServerEndpoint("/timer")
public class TimerEndpoint { ... }
```

* Mantiene una `Queue<Session>` con todas las conexiones abiertas.
* Tiene un método estático `send(String msg)` que recorre la cola y le hace `session.getBasicRemote().sendText(msg)` a cada cliente.
* Gestiona eventos `@OnOpen`, `@OnClose`, `@OnError`.


### 2. Emisor programado (`components/…`)

Clase similar a:

```java
@Component
@Scope("singleton")
public class TimedMessageBroker {

    @Scheduled(fixedRate = 5000)
    public void broadcast() {
        TimerEndpoint.send("The time is now " + ... );
    }
}
```

* Está marcada como `@Component` y `@Scope("singleton")`.
* Cada 5 s envía un mensaje a **todas** las sesiones usando el endpoint anterior.


### 3. Configuración (`configuration/…`)

Spring Boot por defecto **no registra automáticamente** los `@ServerEndpoint` de Java EE en el Tomcat embebido.
Por eso se crea una clase:

```java
@Configuration
@EnableScheduling
public class WSConfigurator {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

* `@EnableScheduling` activa las tareas programadas.
* `ServerEndpointExporter` hace el *bridge* entre Spring y el contenedor WebSocket para que las clases con `@ServerEndpoint` queden registradas.

### 4. Controlador REST (`controller/…`)

Control sencillo tipo:

```java
@RestController
public class WebController {
    @GetMapping("/status")
    public String status() {
        return "{\"status\":\"Greetings from Spring Boot ...\"}";
    }
}
```

Sirve para probar que el servidor está vivo en `http://localhost:8080/status`.

---

##  Cliente web (React)

En `src/main/resources/static/index.html` se incluye:

* React 16 desde CDN
* ReactDOM desde CDN
* Babel para permitir JSX en el browser
* El script local `js/WsComponent.jsx` (o similar) con el componente React


Comportamiento del componente (según el PDF):

1. En `componentDidMount()` abre `new WebSocket("ws://localhost:8080/timer")`.
2. En `onmessage` actualiza el estado con el texto recibido.
3. Renderiza:

    * “Loading…” mientras no llegan mensajes
    * El mensaje con la hora cuando llega algo
4. En caso de error, muestra el error.


---

##  Requisitos

* **Java** 8+ (o la versión configurada en tu `pom.xml`)
* **Maven** 3.x
* Navegador moderno con soporte WebSocket
* Puerto **8080** libre

---

##  Cómo ejecutar

1. **Compilar y correr** con Maven:

   ```bash
   mvn spring-boot:run
   ```

   (O desde tu IDE ejecuta la clase principal bajo `tutorial.sockets.ARSWLAB6`.)

2. **Probar que el backend está levantado**:

    * Abre: `http://localhost:8080/status`
      Deberías ver un JSON con la fecha/hora actual y el mensaje de que el servidor está corriendo.

3. **Probar el cliente web**:

    * Abre: `http://localhost:8080/index.html`
      Verás la página con React y al cabo de pocos segundos comenzarán a llegar los mensajes del servidor.
      (Recuerda que el broadcast está cada **5 segundos**.)

---

##  Endpoints expuestos

| Tipo          | Ruta / URI | Descripción                                      |
| ------------- | ---------- | ------------------------------------------------ |
| **REST**      | `/status`  | Respuesta simple para saber si el servidor vive. |
| **WebSocket** | `/timer`   | Canal WS para broadcast de hora cada 5 s.        |

*(La ruta exacta del WS debe coincidir con la usada en el JS del cliente: `ws://localhost:8080/timer`.)*

---

## ¿Cómo probar varios clientes?

1. Abre **dos o más** pestañas en `http://localhost:8080/index.html`.
2. Cada pestaña abrirá su propia conexión WebSocket.
3. Cuando el servidor haga broadcast verás **el mismo mensaje en todas las pestañas** casi al mismo tiempo → eso valida que se está usando la cola compartida de sesiones.

---

## Puntos clave del diseño

* **Spring como contenedor IoC**: los componentes (`@Component`, `@Configuration`, `@Service`, etc.) son descubiertos y orquestados por Spring.
* **Arquitectura por anotaciones**: prácticamente todo el wiring se hace por anotaciones.
* **Patrón MVC de Spring**: aunque aquí el énfasis es el canal WebSocket, se mantiene el estilo MVC tradicional con un controlador REST.
* **WebSocket**: canal full-duplex, no requiere sondeo desde el cliente.

---

##  Personalización

* **Cambiar intervalo**: edita el valor de `fixedRate` en el componente de broadcast.
* **Cambiar el mensaje**: en vez de `The time is now ...` puedes mandar JSON y que React lo pinte.
* **Cambiar la ruta WS**: cambia el valor de `@ServerEndpoint("/timer")` **y** el de `new WebSocket("ws://localhost:8080/timer")` en el cliente.

---

