# Facultad

Repositorio con proyectos de la facultad. Por ahora contiene:

- [`TiendaUpa/`](TiendaUpa/) — tienda de videojuegos por consola (Fundamentos
  de Informática 1). Para instrucciones de descarga y uso ver el
  [README de TiendaUpa](TiendaUpa/README.md).

Este documento explica **cómo está armado por dentro** el programa de
`TiendaUpa/main.py`, sección por sección.

## Idea general

Todo el programa vive en un único archivo, `main.py`, sin ningún
`import`. Está organizado en bloques marcados con comentarios
(`# ===...`), y cada bloque agrupa funciones relacionadas. `main()`, al
final del archivo, es el punto de entrada: arma el menú principal y llama
al resto de las funciones según lo que elige el usuario.

Las "entidades" del programa (un producto, un usuario, una venta) son
simplemente **diccionarios** de Python, y las listas de productos,
usuarios y ventas son **listas de diccionarios**. No hay clases: es
deliberadamente simple porque la consigna prohíbe usar librerías.

```python
# Ejemplo de cómo se ve un producto en memoria
{
    "id": 5,
    "nombre": "Minecraft",
    "categoria": "Sandbox",
    "plataforma": "PC",
    "precio": 26.99,
    "stock": 30,
}
```

## Constantes iniciales

```python
RUTA_PRODUCTOS = "data/productos.txt"
RUTA_USUARIOS = "data/usuarios.txt"
RUTA_VENTAS = "data/ventas.txt"
```

Son las rutas a los tres archivos donde se guarda toda la información.
Se usan en las funciones de carga/guardado para no repetir el nombre del
archivo en todos lados.

## Sección UTILIDADES

Funciones chicas de apoyo, usadas por el resto del programa:

| Función | Qué hace |
|---|---|
| `limpiar_pantalla()` | Imprime muchos saltos de línea para simular que se "limpia" la pantalla (no se usa `os.system` porque no se permiten imports). |
| `pausar()` | Espera a que el usuario presione ENTER antes de continuar, para que pueda leer un mensaje antes de que cambie la pantalla. |
| `imprimir_titulo(texto)` | Imprime un encabezado centrado entre líneas de `=`, usado en cada pantalla/menú. |
| `hash_clave(clave)` | Convierte una contraseña en un número (string) mediante una fórmula simple hecha a mano, para no guardar la contraseña tal cual en el archivo. No es un cifrado real de seguridad, es una ofuscación básica válida para este trabajo práctico. |
| `pedir_entero(mensaje, minimo)` | Pide un número entero por teclado y repite la pregunta hasta que la respuesta sea válida (y, si se pasa `minimo`, hasta que sea mayor o igual a ese valor). |
| `pedir_flotante(mensaje, minimo)` | Igual que la anterior pero para números decimales (acepta coma o punto). |
| `pedir_texto(mensaje)` | Pide un texto y repite la pregunta si el usuario deja el campo vacío. |
| `formatear_precio(precio)` | Da vuelta un número como `26.99` en un texto con formato `$26.99`. |

## Sección PERSISTENCIA EN ARCHIVOS DE TEXTO

Como el programa no puede usar `json` ni ninguna librería para guardar
datos, esta sección lee y escribe los archivos `data/*.txt` a mano, línea
por línea, usando `|` como separador de campos.

Por cada entidad (productos, usuarios, ventas) hay un par de funciones
simétricas:

- `cargar_productos()` / `guardar_productos(productos)`
- `cargar_usuarios()` / `guardar_usuarios(usuarios)`
- `cargar_ventas()` / `guardar_ventas(ventas)`

Las funciones `cargar_*()` abren el archivo, leen línea por línea, cortan
cada línea por `|` con `split("|")` y arman un diccionario por línea. Si
el archivo todavía no existe (por ejemplo la primera vez que se ejecuta
el programa en una computadora nueva), devuelven una lista vacía en vez
de romper el programa.

Las funciones `guardar_*()` hacen lo inverso: recorren la lista de
diccionarios y escriben una línea por elemento, uniendo los campos con
`|`.

También está acá:

- `generar_id(lista)`: calcula qué número de `id` le corresponde al
  próximo elemento nuevo (el mayor `id` existente + 1, o `1` si la lista
  está vacía). Se usa al crear productos, usuarios y ventas nuevas.

### Formato de cada archivo

```
data/productos.txt   → id|nombre|categoria|plataforma|precio|stock
data/usuarios.txt    → id|nombre_usuario|clave_hash|rol
data/ventas.txt      → id|nombre_usuario|detalle|total
```

El campo `detalle` de una venta guarda los productos comprados en un
formato propio: `id:cantidad:precio` separados por `;` cuando hay más de
un producto. Por ejemplo `5:2:26.99;3:1:39.99` significa "2 unidades del
producto 5 y 1 unidad del producto 3".

## Sección PRODUCTOS / CATALOGO

Funciones para mostrar y administrar el catálogo de juegos:

- `mostrar_productos(productos)`: imprime una tabla con todos los
  productos de la lista que recibe.
- `buscar_productos(productos, texto)`: filtra la lista de productos
  cuyo nombre, categoría o plataforma contenga el texto buscado
  (sin importar mayúsculas/minúsculas).
- `buscar_producto_por_id(productos, id_producto)`: recorre la lista y
  devuelve el producto con ese `id`, o `None` si no existe. La usan casi
  todas las demás funciones para ubicar un producto puntual.
- `agregar_producto(productos)`: pide los datos de un juego nuevo por
  teclado, le asigna un `id`, lo agrega a la lista y guarda el archivo.
- `editar_producto(productos)`: muestra el catálogo, pide el `id` a
  editar y permite cambiar cada campo (dejar vacío = no modificarlo).
- `eliminar_producto(productos)`: pide el `id` a borrar, confirma con el
  usuario (`s`/`n`) y lo saca de la lista.

Estas tres últimas solo están disponibles desde el menú de administrador.

## Sección USUARIOS / SESION

- `usuario_existe(usuarios, nombre_usuario)`: revisa si ya hay alguien
  registrado con ese nombre de usuario (se usa al registrarse, para no
  permitir nombres repetidos).
- `registrar_usuario(usuarios)`: crea un usuario nuevo con rol
  `"cliente"` (los administradores no se crean por acá, ya vienen dados
  de alta en `data/usuarios.txt`).
- `iniciar_sesion(usuarios)`: pide usuario y contraseña, calcula el hash
  de la contraseña ingresada y lo compara contra el guardado. Si
  coincide, devuelve el diccionario del usuario logueado; si no,
  devuelve `None`.
- `ver_usuarios(usuarios)`: lista todos los usuarios registrados (solo
  para el administrador).

El campo `"rol"` de cada usuario (`"admin"` o `"cliente"`) es lo que
decide, en `main()`, a qué menú entra cada quien después de loguearse.

## Sección CARRITO DE COMPRAS

El carrito **no se guarda en ningún archivo**: es un diccionario que vive
solo mientras el cliente tiene la sesión abierta (se crea vacío al entrar
a `menu_cliente` y se pierde al cerrar sesión). Tiene la forma
`{id_producto: cantidad}`.

- `agregar_al_carrito(carrito, productos)`: pide un `id` de producto y
  una cantidad, valida que haya stock suficiente (restando lo que ya
  hay en el carrito) y lo suma al diccionario del carrito.
- `ver_carrito(carrito, productos)`: imprime el contenido del carrito con
  el subtotal de cada línea y el total general.
- `quitar_del_carrito(carrito)`: saca un producto del carrito por `id`.
- `finalizar_compra(carrito, productos, ventas, usuario)`: revisa que
  todavía haya stock suficiente de cada producto del carrito, descuenta
  el stock, arma un registro de venta nuevo (con `generar_id`), lo
  guarda en `data/ventas.txt` y vacía el carrito.
- `ver_historial_compras(ventas, usuario, productos)`: filtra las ventas
  que pertenecen al usuario logueado y muestra el detalle de cada una,
  traduciendo el campo `detalle` (que solo tiene ids) a nombres de
  producto legibles.
- `reporte_ventas(ventas)`: lista **todas** las ventas de todos los
  clientes con su total, y suma un total general (solo para el
  administrador).

## Sección MENUS

Acá se arma la interacción con el usuario: cada función es un bucle
`while True` que muestra opciones numeradas, lee lo que el usuario
escribe y llama a la función correspondiente.

- `menu_cliente(usuario, productos, ventas)`: menú de compras (ver
  catálogo, buscar, carrito, comprar, historial). Acá se crea el
  `carrito` de esta sesión.
- `menu_admin(usuario, productos, usuarios, ventas)`: menú de gestión
  (catálogo, alta/edición/baja de productos, usuarios, reporte de
  ventas).

Ambos bucles terminan (`break`) cuando el usuario elige "Cerrar sesión",
lo que devuelve el control a `main()`.

## `main()`: punto de entrada

Es la función que se ejecuta al correr `python main.py`. Hace lo
siguiente:

1. Carga productos, usuarios y ventas desde los archivos de texto
   (una sola vez, al arrancar).
2. Muestra el menú principal (Iniciar sesión / Registrarse / Salir) en
   un bucle infinito.
3. Si el login es exitoso, decide a qué menú mandar al usuario según su
   `"rol"` (`menu_admin` o `menu_cliente`).
4. Al elegir "Salir", corta el bucle con `break` y el programa termina.

Al final del archivo, `main()` se llama directamente para que el
programa arranque apenas se lo ejecuta.

## Por qué estas decisiones de diseño

- **Sin clases**: se usan diccionarios en vez de clases porque no se
  permiten conceptos de librerías externas ni patrones más avanzados
  vistos en cursos posteriores; con funciones y diccionarios alcanza
  para resolver todo el trabajo práctico de forma clara.
- **Sin `import`**: ni siquiera se usan módulos incluidos en Python
  (`json`, `os`, `hashlib`), porque la consigna del trabajo lo prohíbe
  explícitamente. Por eso hay funciones "hechas a mano" para cosas que
  normalmente resolvería una librería, como `hash_clave` o el guardado
  en archivos de texto en vez de JSON.
- **Persistencia en `.txt` con `|`**: es la forma más simple de guardar
  datos estructurados en disco sin usar el módulo `json`.
