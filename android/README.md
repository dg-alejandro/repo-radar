# Delta semana 7 — completar la app Android (18 mayo 2026)

Este ZIP es un **delta** que se aplica **encima** del proyecto Android que ya
tenias (`com.reporadar`, namespace fijado en `build.gradle.kts`). Mantiene
todo lo que ya funcionaba y solo anade lo que faltaba.

## Como aplicarlo

1. Descomprime este ZIP **dentro de la carpeta raiz de tu proyecto** (la que
   contiene `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`).
2. Al fusionar, **acepta sobrescribir** estos archivos (los he reescrito a
   conciencia, son evolucion de los tuyos, no codigo paralelo):
   - `app/src/main/java/com/reporadar/MainActivity.java`
   - `app/src/main/java/com/reporadar/RetrofitClient.java`
   - `app/src/main/res/layout/activity_main.xml`
   - `app/src/main/AndroidManifest.xml`
3. **No se tocan** estos archivos tuyos (los he leido y estan bien tal cual):
   - `ProjectAdapter.java`
   - `api/ApiService.java`
   - `model/Project.java`, `Category.java`, `Technology.java`,
     `AuthRequest.java`, `AuthResponse.java`
   - `item_project.xml`
   - `themes.xml`, `colors.xml`, `strings.xml`
   - `build.gradle.kts`, `libs.versions.toml`, etc.
4. En Android Studio, Build > Clean Project, Build > Rebuild Project,
   y ejecuta en el emulador.

## Que cambia respecto a tu version anterior

### `MainActivity.java`
- **Mantiene** la busqueda en tiempo real con TextWatcher (no la he tocado).
- **Anade** un `Toolbar` con menu (Mis favoritos / Iniciar sesion / Cerrar
  sesion). Los items se muestran u ocultan segun haya sesion activa.
- **Anade** un Spinner que carga las categorias desde `/api/categories` y
  permite filtrar.
- **Cambia** el comportamiento del click en una fila: antes hacia un Toast
  con el nombre del proyecto; ahora abre la `ProjectDetailActivity`.

### `RetrofitClient.java`
- **Mantiene** `getInstance()` y `getAuthenticatedInstance(String token)`
  tal cual.
- **Anade** un overload `getAuthenticatedInstance(Context)` que lee el token
  del SessionManager automaticamente.

### `activity_main.xml`
- **Mantiene** `etSearch`, `recyclerView`, `tvEmpty` con los mismos IDs.
- **Anade** `toolbar` arriba y `spinnerCategory` debajo del buscador.

### `AndroidManifest.xml`
- **Mantiene** todo (`MainActivity` como LAUNCHER, permiso INTERNET, tema...).
- **Anade** las 4 nuevas activities (Login, Register, ProjectDetail, Favorites).
- **Anade** el atributo `android:networkSecurityConfig` apuntando al nuevo XML.

## Archivos nuevos

- `com/reporadar/session/SessionManager.java` — JWT en SharedPreferences.
- `com/reporadar/ui/auth/LoginActivity.java` y `RegisterActivity.java`.
- `com/reporadar/ui/detail/ProjectDetailActivity.java`.
- `com/reporadar/ui/favorites/FavoritesActivity.java` — reutiliza tu
  `ProjectAdapter`.
- `res/layout/activity_login.xml`, `activity_register.xml`,
  `activity_project_detail.xml`, `activity_favorites.xml`.
- `res/menu/menu_main.xml`.
- `res/xml/network_security_config.xml`.

## Punto a validar manana — IMPORTANTE

**`AuthRequest` solo tiene email y password, no `name`.** La pantalla de
registro pide el nombre al usuario pero NO lo manda al backend (porque el
DTO no lo tiene). Si tu endpoint `/api/auth/register` espera tambien
`name`, el registro fallara con HTTP 400.

Si es asi, hay que:
1. Anadir un campo `name` (con su getter y un segundo constructor) a
   `model/AuthRequest.java`.
2. En `RegisterActivity.doRegister()`, cambiar la llamada a
   `new AuthRequest(name, email, password)`.

Lo he dejado asi para no tocar tu modelo sin tu permiso. Si me confirmas
manana que el backend espera `name`, te lo cambio en un minuto.

## Sobre `recyclerview` en `build.gradle.kts`

Tu `build.gradle.kts` no declara `androidx.recyclerview` explicitamente,
pero la `MainActivity` original ya lo usa. Funciona porque viene
transitivamente con `material`. Si quieres dejarlo explicito (mejor por
claridad), anade en `dependencies`:

```kotlin
implementation("androidx.recyclerview:recyclerview:1.3.2")
```

No es necesario para que compile.

## Flujo de navegacion

```
MainActivity (pantalla inicial, anonima)
   |--(EditText busqueda)----------> filtra en tiempo real
   |--(Spinner categoria)----------> filtra
   |--(toolbar > iniciar sesion)--> LoginActivity
   |                                   |--(link)------> RegisterActivity
   |--(toolbar > mis favoritos)----> FavoritesActivity (si logueado)
   |--(click en una fila)----------> ProjectDetailActivity
                                          |--(boton favorito)--> add/remove
                                          |--(abrir repositorio)--> navegador
```

## Checklist de la semana 7

- [x] Proyecto Android + Retrofit (ya estaba)
- [x] Pantalla de listado de proyectos publicados (ya estaba, ampliada)
- [x] Busqueda por texto (ya estaba, intacta)
- [x] Filtro por categoria (anadido, spinner)
- [x] Pantalla de detalle de proyecto (anadida)
- [x] Pantallas de registro y login (anadidas)
- [x] JWT en SharedPreferences + interceptor Retrofit (anadido, reusa
       tu RetrofitClient)
- [x] Boton de favorito en el detalle (anadido)
- [x] Pantalla de favoritos (anadida)
- [ ] Probar todos los flujos contra el backend (queda para ti)
