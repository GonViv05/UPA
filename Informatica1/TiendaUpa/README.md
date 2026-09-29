# Tienda UPA

Proyecto de la facultad (Fundamentos de Informática 1): una tienda de
videojuegos que funciona por consola (terminal), escrita en Python puro,
sin usar ninguna librería externa ni módulos importados.

Esta guía está pensada para alguien que **nunca programó**, así que explica
todo paso a paso.

## 1. Cómo descargar el proyecto

Este proyecto vive dentro de un repositorio de GitHub
([GonViv05/UPA](https://github.com/GonViv05/UPA)), en la carpeta
`Informatica1/TiendaUpa`. La forma más simple de descargarlo, sin instalar nada extra,
es la siguiente:

1. Entrá a <https://github.com/GonViv05/UPA/tree/main/Informatica1/TiendaUpa>
2. Arriba a la derecha, hacé clic en el botón verde **`< > Code`** (en la
   página principal del repositorio, no dentro de la carpeta).
3. Elegí **"Download ZIP"**.
4. Se descarga un archivo comprimido (`UPA-main.zip`). Hacé doble
   clic para abrirlo y "Extraer todo" (en Windows, botón derecho sobre el
   archivo → **Extraer todo...**) en la carpeta donde quieras guardarlo.
5. Dentro de la carpeta extraída vas a encontrar `TiendaUpa`, con
   `main.py` y la carpeta `data/` adentro. Con eso ya tenés todo lo
   necesario para ejecutarlo (ver el paso 3 de esta guía).

> Nota: al descargar el ZIP se trae **todo** el repositorio `UPA`,
> no solo `TiendaUpa`. Es normal, el resto de las carpetas corresponden a
> otras materias/proyectos.

### Alternativa para quien sepa usar git

```bash
git clone https://github.com/GonViv05/UPA.git
cd UPA/Informatica1/TiendaUpa
```

## 2. Qué necesitás para poder ejecutarlo

Tener **Python instalado** en la computadora (versión 3.8 o superior).

Para saber si ya lo tenés, abrí una terminal (en Windows: `cmd` o
PowerShell) y escribí:

```bash
python --version
```

Si te muestra un número de versión (por ejemplo `Python 3.12.0`), ya está
instalado. Si da error, hay que descargarlo desde
[python.org/downloads](https://www.python.org/downloads/) e instalarlo
(marcando la casilla "Add Python to PATH" durante la instalación).

## 3. Cómo ejecutar el programa

1. Abrí una terminal.
2. Ubicate dentro de la carpeta `TiendaUpa` (donde está el archivo
   `main.py`). Por ejemplo:
   ```bash
   cd ruta/hasta/TiendaUpa
   ```
3. Ejecutá:
   ```bash
   python main.py
   ```
4. Va a aparecer un menú de texto. Se interactúa escribiendo el número de
   la opción deseada y presionando Enter.

## 4. Cómo usarlo

Al iniciar, el programa muestra tres opciones:

```
1. Iniciar sesion
2. Registrarse
3. Salir
```

### Si es tu primera vez (todavía no tenés usuario)

Elegí la opción **2 (Registrarse)**, poné un nombre de usuario y una
contraseña. Después volvé al menú principal y elegí **1 (Iniciar sesion)**
con esos mismos datos.

### Si querés entrar como administrador

Ya existe un usuario administrador creado de antemano:

- Usuario: `admin`
- Contraseña: `admin123`

Con esta cuenta se accede a un menú distinto, pensado para gestionar la
tienda (agregar/editar/eliminar productos, ver usuarios, ver reporte de
ventas), en vez del menú de compras normal.

### Menú de cliente (usuario común)

- **Ver catálogo**: muestra todos los videojuegos disponibles.
- **Buscar producto**: busca por nombre, categoría o plataforma.
- **Agregar al carrito**: elegís un producto por su ID y una cantidad.
- **Ver carrito**: muestra qué agregaste y el total a pagar.
- **Quitar del carrito**: saca un producto que agregaste por error.
- **Finalizar compra**: confirma la compra y descuenta el stock.
- **Ver historial de compras**: muestra tus compras anteriores.
- **Cerrar sesión**: vuelve al menú principal.

### Menú de administrador

- Ver catálogo, agregar/editar/eliminar productos.
- Ver la lista de usuarios registrados.
- Ver un reporte con todas las ventas realizadas por todos los clientes.

## 5. Cómo guarda la información el programa

El programa **no usa una base de datos**, guarda todo en archivos de texto
simples dentro de la carpeta `data/`:

| Archivo             | Qué contiene                                   |
|----------------------|-------------------------------------------------|
| `data/productos.txt` | El catálogo de videojuegos (nombre, precio, stock, etc.) |
| `data/usuarios.txt`  | Los usuarios registrados y sus contraseñas (cifradas) |
| `data/ventas.txt`    | El historial de todas las compras realizadas   |

Cada línea de estos archivos representa un registro, y los datos dentro de
cada línea están separados por el símbolo `|`. No hace falta editarlos a
mano: el programa los lee y los actualiza solo.

**Importante**: si borrás o modificás mal estos archivos, el programa
puede dejar de funcionar correctamente. Si eso pasa, se puede volver a
escribir el archivo desde cero siguiendo el mismo formato.

## 6. Por qué no hay librerías

La consigna del trabajo pedía no usar ninguna librería (ni siquiera las
que vienen incluidas con Python, como `json` u `os`). Por eso, en
`main.py` no hay ninguna línea `import`: todo está resuelto con las
herramientas básicas del lenguaje (funciones, listas, diccionarios, y la
función `open()` para leer/escribir archivos de texto).

## 7. Estructura de archivos del proyecto

```
TiendaUpa/
├── main.py              # Todo el programa
├── README.md            # Este archivo
└── data/
    ├── productos.txt    # Catálogo de productos
    ├── usuarios.txt     # Usuarios registrados
    └── ventas.txt       # Historial de compras
```
