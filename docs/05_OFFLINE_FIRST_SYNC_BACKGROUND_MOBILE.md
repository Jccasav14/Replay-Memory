# REPLAY: ARQUITECTURA OFFLINE-FIRST, SINCRONIZACIÓN Y PROCESAMIENTO MÓVIL

## 1. Estrategia Offline-First con Expo SQLite
En **REPLAY**, la falta de conectividad no es una excepción sino un estado operacional de primer nivel.

### 1.1. Ciclo de Vida del Dato en el Dispositivo
1. **Creación Local Inmediata**: Al tomar una foto o registrar un recuerdo sin conexión, la app genera un UUID v4 local (`id: "loc-xxxx"`), almacena la imagen en el directorio local de archivos del dispositivo (`FileSystem.documentDirectory`) y registra la fila en `local_memories` con `sync_status = 'PENDING_INSERT'`.
2. **Registro en Cola de Sincronización**: Se inserta una entrada en la tabla `sync_queue` con la operación (`INSERT`), el timestamp local y el payload serializado.
3. **Optimistic UI**: La interfaz de usuario refleja de inmediato el recuerdo en el Timeline móvil con un indicador visual sutil (icono de nube con reloj).

---

## 2. Motor de Sincronización y Resolución de Conflictos

### 2.1. Protocolo de Sincronización Bidireccional
```text
[Cliente Móvil]                                   [Spring Boot Backend]
       |                                                    |
       |  1. POST /api/v1/sync/batch                        |
       |     - deviceId, lastSyncTimestamp                  |
       |     - operations: [INSERT, UPDATE, DELETE]         |
       |--------------------------------------------------->|
       |                                                    |
       |                                                    |-- Valida Tokens y Lock en Redis
       |                                                    |-- Ejecuta transacciones en MongoDB
       |                                                    |-- Resuelve conflictos (LWW / Versioning)
       |                                                    |-- Consulta cambios del servidor ocurridos
       |                                                    |   después de lastSyncTimestamp
       |                                                    |
       |  2. 200 OK Response                                |
       |     - status: SUCCESS                              |
       |     - mappings: [{ localId, remoteId, status }]    |
       |     - serverChanges: [nuevos o editados remotos]   |
       |<---------------------------------------------------|
       |                                                    |
       |-- Actualiza local_memories con remote_id           |
       |-- Cambia sync_status a 'SYNCED'                    |
       |-- Aplica serverChanges en SQLite                   |
       |-- Limpia sync_queue procesada                      |
```

### 2.2. Política de Resolución de Conflictos: Last-Write-Wins (LWW) con Versionado
* Cada entidad posee un campo entero monotónico `syncVersion` y una marca de tiempo UTC `updatedAt`.
* Si un cliente intenta actualizar un recuerdo cuya versión remota es mayor a la versión que el cliente posee, el backend aplica la regla:
  * **Si los campos editados son disyuntos**: Se realiza un *three-way merge* automático.
  * **Si hay colisión en el mismo campo**: Prevalece la modificación con el timestamp más reciente (`Last-Write-Wins`) y se guarda una traza en el log de auditoría.

---

## 3. Realidades y Limitaciones del Funcionamiento en Segundo Plano (Background)

> **ADVERTENCIA DE INGENIERÍA**:
> Ni Android ni iOS permiten que una aplicación móvil ejecute código JavaScript de forma arbitraria y continua 24/7 en segundo plano. Los sistemas operativos modernos aplican restricciones agresivas para preservar la batería y proteger la privacidad.

### 3.1. Limitaciones Reales de iOS
* **Background Execution Window**: iOS concede entre 15 y 30 segundos como máximo cuando la app entra en suspensión para finalizar tareas (`Background Tasks API`).
* **Background App Refresh**: El sistema operativo decide cuándo despertar a la app en función de patrones de uso del usuario y nivel de batería. No es predecible en intervalos exactos.
* **Significant Location Change Service**: Permite despertar la app únicamente cuando el dispositivo cambia significativamente de antena celular o punto de acceso Wi-Fi (~500 metros), ahorrando batería.

### 3.2. Limitaciones Reales de Android
* **Doze Mode & App Standby**: Desactiva el acceso a red y suspende tareas programadas cuando el teléfono está quieto y con la pantalla apagada.
* **Foreground Service**: Obligatorio si se requiere ejecución continua, mostrando una notificación persistente al usuario.

### 3.3. Diseño de Tareas en REPLAY (Expo TaskManager & Location)
Para REPLAY se diseña un **registro discreto por eventos significativos (Geofencing y Significant Motion)**:
1. Se configuran geocercas en lugares clave registrados en el Life Graph (Casa, Trabajo, Universidad).
2. Se utiliza `Location.startGeofencingAsync(TASK_GEOFENCE, regions)`.
3. Al entrar o salir de una región, el sistema operativo despierta la app de forma ultra-ligera (menos de 5 segundos) para registrar un `LOCATION_EVENT` en SQLite local.
4. Las fotos tomadas se sincronizan preferentemente cuando el usuario abre la app o cuando el dispositivo se conecta a una red Wi-Fi y está conectado a la corriente (`TaskScheduler` / `WorkManager`).

---

## 4. Evolución de la Aplicación: Expo Go a EAS Build

```text
+---------------------+
| 1. Expo Go          | -> Prototipado rápido de UI, pantallas y componentes básicos.
+---------------------+
           |
           v
+---------------------+
| 2. Expo Dev Client  | -> Compilación de desarrollo con módulos nativos customizados
|    (Dev Build)      |    (Expo SQLite, TaskManager, Location Background, Native Keys).
+---------------------+
           |
           v
+---------------------+
| 3. EAS Build        | -> Generación en la nube o local de binarios firmados
|    (Release)        |    (Android: .aab / .apk; iOS: .ipa para TestFlight).
+---------------------+
           |
           v
+---------------------+
| 4. Stores Oficiales | -> Publicación en Google Play Store y Apple App Store
|                     |    con declaración formal de permisos de ubicación y cámara.
+---------------------+
```
