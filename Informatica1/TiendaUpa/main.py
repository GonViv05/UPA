# =========================================================
# TIENDA UPA - Sistema de venta de videojuegos (consola)
# =========================================================
#
# QUE HACE ESTE PROGRAMA
# -----------------------
# Es una "tienda" que se maneja desde la terminal (sin ventanas ni
# botones, todo con texto). Un usuario puede registrarse, iniciar
# sesion, ver el catalogo de juegos, agregarlos a un carrito y
# comprarlos. Un administrador puede agregar, editar y eliminar
# productos, ver los usuarios y ver un reporte de ventas.
#
# DONDE SE GUARDA LA INFORMACION
# -------------------------------
# El programa NO usa una base de datos. En su lugar, guarda todo en
# 3 archivos de texto plano (extension .txt) dentro de la carpeta
# "data/":
#   - productos.txt  -> el catalogo de juegos
#   - usuarios.txt   -> las cuentas registradas
#   - ventas.txt     -> el historial de compras
#
# COMO SE VE UN ARCHIVO .TXT Y COMO SE CONVIERTE EN DATOS UTILES
# ----------------------------------------------------------------
# Un archivo .txt es simplemente texto: no entiende de "numeros" ni
# de "listas", solo son lineas de caracteres. Para poder guardar
# varios datos en una sola linea (por ejemplo: id, nombre, precio),
# este programa separa cada dato con el simbolo "|" (barra vertical).
#
# Ejemplo de una linea real de productos.txt:
#   1|The Legend of Zelda Tears of the Kingdom|Aventura|Nintendo Switch|59.99|12
#
# Esa linea, leida por una persona, no es comoda de usar. Por eso el
# programa la transforma en una estructura de datos que Python
# entiende mejor: un DICCIONARIO (una especie de ficha con etiquetas).
# La misma linea de arriba se convierte en:
#   {
#       "id": 1,
#       "nombre": "The Legend of Zelda Tears of the Kingdom",
#       "categoria": "Aventura",
#       "plataforma": "Nintendo Switch",
#       "precio": 59.99,
#       "stock": 12,
#   }
#
# Y todos esos diccionarios juntos forman una LISTA, por ejemplo:
#   productos = [ {producto 1}, {producto 2}, {producto 3}, ... ]
#
# Este "ida y vuelta" entre texto plano y datos utiles pasa por dos
# tipos de funciones que vas a ver mas abajo:
#   - Funciones "cargar_..." -> leen el .txt linea por linea, cortan
#     cada linea por el "|" con el metodo split("|"), y arman los
#     diccionarios/listas que el resto del programa usa.
#   - Funciones "guardar_..." -> hacen el camino inverso: toman la
#     lista de diccionarios que hay en memoria y la convierten de
#     nuevo en lineas de texto separadas por "|", para escribirlas
#     en el archivo .txt y que la informacion no se pierda al cerrar
#     el programa.
#
# En resumen: el .txt es la "memoria permanente" (persistencia) y los
# diccionarios/listas en Python son la forma comoda de trabajar con
# esos datos mientras el programa esta corriendo.


# Rutas (ubicacion) de cada archivo de texto que usa el programa.
# Se guardan en variables para no tener que escribir el nombre del
# archivo cada vez que se necesita, y para poder cambiarlo facil en
# un solo lugar si hiciera falta.
RUTA_PRODUCTOS = "data/productos.txt"
RUTA_USUARIOS = "data/usuarios.txt"
RUTA_VENTAS = "data/ventas.txt"


# =========================================================
# UTILIDADES
# =========================================================
# Esta seccion tiene funciones "de apoyo" que se usan en muchas
# partes del programa: mostrar textos con formato, pedir datos al
# usuario y validar que lo que escribio tenga sentido.

def limpiar_pantalla():
    # "Limpia" la pantalla de la consola imprimiendo 60 saltos de
    # linea seguidos. Es un truco simple: como la consola solo
    # muestra las ultimas lineas, esto empuja todo el texto viejo
    # hacia arriba y fuera de la vista.
    #Con esto reemplacé la libreria time
    print("\n" * 60)


def pausar():
    # Detiene el programa hasta que el usuario presiona ENTER.
    # Sirve para darle tiempo a la persona de leer un mensaje antes
    # de que la pantalla se limpie o cambie de menu.
    #Esto reemplaza la libreria time y el metodo sleep
    input("\nPresiona ENTER para continuar...")


def imprimir_titulo(texto):
    # Muestra un titulo centrado y rodeado de lineas "=" para que
    # se note mas en la consola, por ejemplo el nombre de un menu.

    print("\n" + "=" * 60)
    print(texto.center(60))
    print("=" * 60)


def hash_clave(clave):
    # Convierte una contrasena (texto) en un codigo numerico dificil
    # de adivinar, para NO guardar la contrasena real en el archivo
    # usuarios.txt. Esto es una version muy simplificada de lo que
    # en programacion se llama "hash": una formula matematica que a
    # partir de un texto genera siempre el mismo numero, pero que es
    # muy dificil de invertir (es decir, adivinar la contrasena
    # original solo mirando el numero).
    #
    # Como funciona en este caso: recorre cada letra de la
    # contrasena, la convierte a su codigo numerico con ord() (por
    # ejemplo la "a" es el numero 97), y lo combina con la posicion
    # de la letra (i) y un numero fijo (31) para "mezclar" el
    # resultado. Al final devuelve todo como texto (str).
    total = 0
    for i in range(len(clave)):
        total = total + (ord(clave[i]) * (i + 1) * 31)
    return str(total)


def pedir_entero(mensaje, minimo=None):
    # Pide un numero entero (sin decimales) por teclado y no deja
    # avanzar hasta que el usuario escriba algo valido.
    # "mensaje" es el texto que se muestra al pedir el dato.
    # "minimo" es opcional: si se indica, el numero no puede ser
    # menor a ese valor (por ejemplo, un stock no puede ser negativo).
    while True:
        valor = input(mensaje).strip()  # strip() saca espacios de mas
        if not valor.isdigit():
            print("Ingresa un numero entero valido.")
            continue
        valor = int(valor)  # convierte el texto "12" al numero 12
        if minimo is not None and valor < minimo:
            print(f"El valor debe ser mayor o igual a {minimo}.")
            continue
        return valor


def pedir_flotante(mensaje, minimo=None):
    # Igual que pedir_entero, pero permite numeros con decimales
    # (por ejemplo precios como 59.99). Tambien acepta que el
    # usuario escriba la coma "," en vez del punto "." como
    # separador decimal, y la reemplaza automaticamente.
    while True:
        valor = input(mensaje).strip().replace(",", ".")
        try:
            valor = float(valor)  # convierte el texto a numero decimal
        except ValueError:
            # Si el texto no se puede convertir a numero (por ejemplo
            # si el usuario escribio letras), Python lanza un error
            # ValueError. Lo "atrapamos" aca para pedir el dato de nuevo
            # en vez de que el programa se rompa.
            print("Ingresa un numero valido.")
            continue
        if minimo is not None and valor < minimo:
            print(f"El valor debe ser mayor o igual a {minimo}.")
            continue
        return valor


def pedir_texto(mensaje):
    # Pide un texto por teclado y no permite que quede vacio
    # (por ejemplo, el nombre de un producto no puede ser "").
    while True:
        valor = input(mensaje).strip()
        if valor != "":
            return valor
        print("Este campo no puede estar vacio.")


def formatear_precio(precio):
    # Recibe un numero (por ejemplo 1999.9) y lo devuelve como texto
    # con formato de precio, con el simbolo "$", separador de miles
    # y siempre 2 decimales. Ejemplo: 1999.9 -> "$1,999.90".
    return f"${precio:,.2f}"


# =========================================================
# PERSISTENCIA EN ARCHIVOS DE TEXTO
# =========================================================
# "Persistencia" quiere decir que los datos sobreviven aunque el
# programa se cierre. Aca estan las funciones que leen y escriben
# los archivos .txt explicados al principio del archivo.
#
# Todas las funciones "cargar_..." siguen el mismo patron:
#   1. Abren el archivo de texto.
#   2. Recorren linea por linea.
#   3. Separan cada linea usando split("|") (parte el texto en
#      pedazos, cortando justo donde aparece el simbolo "|").
#   4. Convierten cada pedazo al tipo de dato correcto (int, float
#      o texto) y arman un diccionario.
#   5. Agregan ese diccionario a una lista y la devuelven al final.
#
# Y las funciones "guardar_..." hacen el camino inverso: recorren la
# lista de diccionarios y arman lineas de texto separadas por "|"
# para escribirlas en el archivo, pisando el contenido anterior.
# ASI EVITAMOS TENER BASE DE DATOS PERO TENEMOS DATOS PERSISTENTES CONSULTABLES JAAAAAAAAA

def cargar_productos():
    # Lee data/productos.txt y devuelve una LISTA de diccionarios,
    # uno por cada producto. Formato de cada linea del archivo:
    #   id|nombre|categoria|plataforma|precio|stock
    productos = []
    try:
        archivo = open(RUTA_PRODUCTOS, "r", encoding="utf-8")
    except FileNotFoundError:
        # Si el archivo todavia no existe (por ejemplo, primera vez
        # que se usa el programa), se devuelve una lista vacia en
        # vez de que el programa se rompa con un error.
        return productos
    for linea in archivo:
        linea = linea.strip()  # saca el salto de linea "\n" del final
        if linea == "":
            continue  # ignora lineas vacias
        partes = linea.split("|")  # corta la linea en la lista de textos
        productos.append({
            "id": int(partes[0]),
            "nombre": partes[1],
            "categoria": partes[2],
            "plataforma": partes[3],
            "precio": float(partes[4]),
            "stock": int(partes[5]),
        })
    archivo.close()  # siempre hay que cerrar el archivo despues de usarlo
    return productos


def guardar_productos(productos):
    # Hace lo contrario a cargar_productos: recibe la lista de
    # diccionarios que esta en memoria y la vuelve a escribir en
    # data/productos.txt, una linea por producto, separando cada
    # dato con "|". El modo "w" (write) borra el contenido anterior
    # del archivo y lo reemplaza por completo.
    archivo = open(RUTA_PRODUCTOS, "w", encoding="utf-8")
    for p in productos:
        linea = str(p["id"]) + "|" + p["nombre"] + "|" + p["categoria"] + "|" + p["plataforma"] + "|" + str(p["precio"]) + "|" + str(p["stock"]) + "\n"
        archivo.write(linea)
    archivo.close()


def cargar_usuarios():
    # Igual que cargar_productos, pero para data/usuarios.txt.
    # Formato de cada linea: id|nombre_usuario|clave_hash|rol
    # ("rol" puede ser "admin" o "cliente").
    usuarios = []
    try:
        archivo = open(RUTA_USUARIOS, "r", encoding="utf-8")
    except FileNotFoundError:
        return usuarios
    for linea in archivo:
        linea = linea.strip()
        if linea == "":
            continue
        partes = linea.split("|")
        usuarios.append({
            "id": int(partes[0]),
            "nombre_usuario": partes[1],
            "clave_hash": partes[2],
            "rol": partes[3],
        })
    archivo.close()
    return usuarios


def guardar_usuarios(usuarios):
    # Vuelve a escribir data/usuarios.txt completo a partir de la
    # lista de usuarios que esta en memoria.
    archivo = open(RUTA_USUARIOS, "w", encoding="utf-8")
    for u in usuarios:
        linea = str(u["id"]) + "|" + u["nombre_usuario"] + "|" + u["clave_hash"] + "|" + u["rol"] + "\n"
        archivo.write(linea)
    archivo.close()


def cargar_ventas():
    # Igual que las anteriores, pero para data/ventas.txt.
    # Formato de cada linea: id|nombre_usuario|detalle|total
    # El campo "detalle" guarda dentro de si mismo varios productos
    # comprados en esa venta, separados por ";" (ver mas abajo en
    # finalizar_compra y ver_historial_compras como se arma y se lee).
    ventas = []
    try:
        archivo = open(RUTA_VENTAS, "r", encoding="utf-8")
    except FileNotFoundError:
        return ventas
    for linea in archivo:
        linea = linea.strip()
        if linea == "":
            continue
        partes = linea.split("|")
        ventas.append({
            "id": int(partes[0]),
            "usuario": partes[1],
            "detalle": partes[2],
            "total": float(partes[3]),
        })
    archivo.close()
    return ventas


def guardar_ventas(ventas):
    # Vuelve a escribir data/ventas.txt completo a partir de la
    # lista de ventas que esta en memoria.
    archivo = open(RUTA_VENTAS, "w", encoding="utf-8")
    for v in ventas:
        linea = str(v["id"]) + "|" + v["usuario"] + "|" + v["detalle"] + "|" + str(v["total"]) + "\n"
        archivo.write(linea)
    archivo.close()


def generar_id(lista):
    # Calcula que numero de "id" le corresponde al proximo elemento
    # nuevo (producto, usuario o venta), buscando el id mas alto que
    # ya existe y sumandole 1. Si la lista esta vacia, empieza en 1.
    if len(lista) == 0:
        return 1
    ids = []
    for elemento in lista:
        ids.append(elemento["id"])
    return max(ids) + 1


# =========================================================
# PRODUCTOS / CATALOGO
# =========================================================
# Funciones relacionadas con el catalogo de juegos: mostrarlo,
# buscar dentro de el, y agregar/editar/eliminar productos (esto
# ultimo solo lo puede hacer el administrador, ver menu_admin).

def mostrar_productos(productos):
    # Imprime en pantalla una tabla con todos los productos de la
    # lista recibida: id, nombre, categoria, plataforma, precio y
    # stock (cantidad disponible).
    if len(productos) == 0:
        print("No hay productos disponibles.")
        return
    print(f"{'ID':<4}{'Nombre':<44}{'Categoria':<14}{'Plataforma':<16}{'Precio':<12}{'Stock':<6}")
    print("-" * 96)
    for p in productos:
        print(f"{p['id']:<4}{p['nombre']:<44}{p['categoria']:<14}{p['plataforma']:<16}{formatear_precio(p['precio']):<12}{p['stock']:<6}")


def buscar_productos(productos, texto):
    # Devuelve una lista nueva solo con los productos cuyo nombre,
    # categoria o plataforma contengan el texto buscado (sin
    # importar mayusculas o minusculas, porque todo se compara en
    # minuscula con .lower()).
    texto = texto.lower()
    resultado = []
    for p in productos:
        if texto in p["nombre"].lower() or texto in p["categoria"].lower() or texto in p["plataforma"].lower():
            resultado.append(p)
    return resultado


def buscar_producto_por_id(productos, id_producto):
    # Recorre la lista de productos y devuelve el que tenga el id
    # buscado. Si no lo encuentra, devuelve None (es decir, "nada").
    for p in productos:
        if p["id"] == id_producto:
            return p
    return None


def agregar_producto(productos):
    # Le pide al administrador los datos de un producto nuevo, le
    # asigna un id automatico (con generar_id), lo agrega a la
    # lista en memoria y guarda esa lista en el archivo de texto
    # para que el cambio no se pierda.
    imprimir_titulo("AGREGAR PRODUCTO")
    nombre = pedir_texto("Nombre del juego: ")
    categoria = pedir_texto("Categoria: ")
    plataforma = pedir_texto("Plataforma: ")
    precio = pedir_flotante("Precio: ", minimo=0)
    stock = pedir_entero("Stock: ", minimo=0)
    nuevo_id = generar_id(productos)
    productos.append({
        "id": nuevo_id,
        "nombre": nombre,
        "categoria": categoria,
        "plataforma": plataforma,
        "precio": precio,
        "stock": stock,
    })
    guardar_productos(productos)
    print(f"Producto '{nombre}' agregado con id {nuevo_id}.")


def editar_producto(productos):
    # Muestra el catalogo, pide el id del producto a editar y luego
    # pide cada dato de nuevo. Si el usuario deja un campo vacio
    # (solo presiona ENTER), ese dato NO se modifica y queda el
    # valor que ya tenia. Al final guarda los cambios en el archivo.
    imprimir_titulo("EDITAR PRODUCTO")
    mostrar_productos(productos)
    id_producto = pedir_entero("\nID del producto a editar: ")
    producto = buscar_producto_por_id(productos, id_producto)
    if producto is None:
        print("No existe un producto con ese id.")
        return
    print("Deja el campo vacio para mantener el valor actual.")
    nombre = input(f"Nombre [{producto['nombre']}]: ").strip()
    if nombre != "":
        producto["nombre"] = nombre
    categoria = input(f"Categoria [{producto['categoria']}]: ").strip()
    if categoria != "":
        producto["categoria"] = categoria
    plataforma = input(f"Plataforma [{producto['plataforma']}]: ").strip()
    if plataforma != "":
        producto["plataforma"] = plataforma
    precio = input(f"Precio [{producto['precio']}]: ").strip()
    if precio != "":
        producto["precio"] = float(precio.replace(",", "."))
    stock = input(f"Stock [{producto['stock']}]: ").strip()
    if stock != "":
        producto["stock"] = int(stock)
    guardar_productos(productos)
    print("Producto actualizado correctamente.")


def eliminar_producto(productos):
    # Muestra el catalogo, pide el id del producto a eliminar, pide
    # una confirmacion ("s" para si) y si se confirma, lo saca de la
    # lista y guarda el cambio en el archivo de texto.
    imprimir_titulo("ELIMINAR PRODUCTO")
    mostrar_productos(productos)
    id_producto = pedir_entero("\nID del producto a eliminar: ")
    producto = buscar_producto_por_id(productos, id_producto)
    if producto is None:
        print("No existe un producto con ese id.")
        return
    confirmacion = input(f"Seguro que deseas eliminar '{producto['nombre']}'? (s/n): ").strip().lower()
    if confirmacion == "s":
        productos.remove(producto)
        guardar_productos(productos)
        print("Producto eliminado.")
    else:
        print("Operacion cancelada.")


# =========================================================
# USUARIOS / SESION
# =========================================================
# Funciones relacionadas con las cuentas de usuario: registrarse,
# iniciar sesion y (para el administrador) ver la lista de usuarios.

def usuario_existe(usuarios, nombre_usuario):
    # Revisa si ya existe un usuario con ese nombre (sin importar
    # mayusculas/minusculas), para no permitir nombres repetidos.
    for u in usuarios:
        if u["nombre_usuario"].lower() == nombre_usuario.lower():
            return True
    return False


def registrar_usuario(usuarios):
    # Crea una cuenta nueva de tipo "cliente" (el rol "admin" no se
    # puede crear desde aca, solo existe si ya esta en el archivo).
    # Pide nombre de usuario y contrasena, verifica que el nombre no
    # este repetido, y guarda la contrasena "hasheada" (ver
    # hash_clave) en vez de guardarla tal cual, por seguridad.
    imprimir_titulo("REGISTRO DE USUARIO")
    nombre_usuario = pedir_texto("Nombre de usuario: ")
    if usuario_existe(usuarios, nombre_usuario):
        print("Ese nombre de usuario ya esta en uso.")
        return
    clave = pedir_texto("Contrasena: ")
    nuevo_id = generar_id(usuarios)
    usuario = {
        "id": nuevo_id,
        "nombre_usuario": nombre_usuario,
        "clave_hash": hash_clave(clave),
        "rol": "cliente",
    }
    usuarios.append(usuario)
    guardar_usuarios(usuarios)
    print(f"Usuario '{nombre_usuario}' registrado con exito. Ya puedes iniciar sesion.")


def iniciar_sesion(usuarios):
    # Pide nombre de usuario y contrasena, calcula el hash de la
    # contrasena ingresada y lo compara con el hash guardado en el
    # archivo. Si coinciden nombre de usuario y hash, el inicio de
    # sesion es correcto y se devuelve el diccionario de ese
    # usuario; si no, se devuelve None (no se pudo iniciar sesion).
    imprimir_titulo("INICIAR SESION")
    nombre_usuario = pedir_texto("Nombre de usuario: ")
    clave = pedir_texto("Contrasena: ")
    clave_hash = hash_clave(clave)
    for u in usuarios:
        if u["nombre_usuario"].lower() == nombre_usuario.lower() and u["clave_hash"] == clave_hash:
            print(f"Bienvenido/a, {u['nombre_usuario']}!")
            return u
    print("Usuario o contrasena incorrectos.")
    return None


def ver_usuarios(usuarios):
    # Muestra una tabla con todos los usuarios registrados y su rol
    # (admin o cliente). Es una funcion que solo usa el administrador.
    imprimir_titulo("USUARIOS REGISTRADOS")
    print(f"{'ID':<5}{'Usuario':<25}{'Rol':<10}")
    print("-" * 40)
    for u in usuarios:
        print(f"{u['id']:<5}{u['nombre_usuario']:<25}{u['rol']:<10}")


# =========================================================
# CARRITO DE COMPRAS
# =========================================================
# El carrito NO se guarda en ningun archivo: es un diccionario que
# vive solo mientras el cliente esta usando el menu (se crea vacio
# al entrar y desaparece al cerrar sesion). Su forma es:
#   carrito = { id_producto: cantidad, id_producto: cantidad, ... }
# Por ejemplo: { 1: 2, 5: 1 } significa "2 unidades del producto 1
# y 1 unidad del producto 5".

def agregar_al_carrito(carrito, productos):
    # Muestra el catalogo, pide el id del producto y la cantidad
    # deseada, y la suma al carrito. Antes de aceptar, revisa que
    # haya stock suficiente (restando lo que ya se habia puesto en
    # el carrito, para no "vender" mas de lo que hay disponible).
    mostrar_productos(productos)
    id_producto = pedir_entero("\nID del producto a agregar: ")
    producto = buscar_producto_por_id(productos, id_producto)
    if producto is None:
        print("No existe un producto con ese id.")
        return
    cantidad_en_carrito = carrito.get(id_producto, 0)
    disponible = producto["stock"] - cantidad_en_carrito
    if disponible <= 0:
        print("No queda stock disponible para agregar.")
        return
    cantidad = pedir_entero(f"Cantidad (disponible {disponible}): ", minimo=1)
    if cantidad > disponible:
        print("No hay suficiente stock disponible.")
        return
    carrito[id_producto] = cantidad_en_carrito + cantidad
    print(f"Se agregaron {cantidad} unidad(es) de '{producto['nombre']}' al carrito.")


def ver_carrito(carrito, productos):
    # Muestra el contenido actual del carrito: cada producto, la
    # cantidad elegida y el subtotal (precio x cantidad), y al final
    # el total a pagar sumando todos los subtotales.
    imprimir_titulo("TU CARRITO")
    if len(carrito) == 0:
        print("El carrito esta vacio.")
        return
    total = 0.0
    print(f"{'ID':<4}{'Nombre':<44}{'Cantidad':<10}{'Subtotal':<12}")
    print("-" * 70)
    for id_producto in carrito:
        cantidad = carrito[id_producto]
        producto = buscar_producto_por_id(productos, id_producto)
        if producto is None:
            continue
        subtotal = producto["precio"] * cantidad
        total += subtotal
        print(f"{producto['id']:<4}{producto['nombre']:<44}{cantidad:<10}{formatear_precio(subtotal):<12}")
    print("-" * 64)
    print(f"TOTAL: {formatear_precio(total)}")


def quitar_del_carrito(carrito):
    # Saca del carrito el producto cuyo id indique el usuario.
    if len(carrito) == 0:
        print("El carrito esta vacio.")
        return
    id_producto = pedir_entero("ID del producto a quitar del carrito: ")
    if id_producto in carrito:
        del carrito[id_producto]
        print("Producto quitado del carrito.")
    else:
        print("Ese producto no esta en el carrito.")


def finalizar_compra(carrito, productos, ventas, usuario):
    # Convierte el carrito en una venta real:
    #   1. Revisa que siga habiendo stock suficiente de cada producto
    #      (por si cambio algo desde que se agrego al carrito).
    #   2. Arma el "detalle" de la venta como texto: junta todos los
    #      productos comprados con el formato id:cantidad:precio y
    #      los separa entre si con ";" (por ejemplo "1:2:59.99;5:1:26.99"),
    #      para poder guardarlos en una sola linea de ventas.txt.
    #   3. Resta el stock vendido de cada producto y guarda el
    #      catalogo actualizado.
    #   4. Crea el registro de la venta (con id nuevo, usuario,
    #      detalle y total) y lo guarda en ventas.txt.
    #   5. Vacia el carrito porque la compra ya se concreto.
    if len(carrito) == 0:
        print("El carrito esta vacio, no hay nada que comprar.")
        return
    total = 0.0
    detalle_items = []
    for id_producto in carrito:
        cantidad = carrito[id_producto]
        producto = buscar_producto_por_id(productos, id_producto)
        if producto is None or producto["stock"] < cantidad:
            print("Stock insuficiente para completar la compra. Compra cancelada.")
            return
        detalle_items.append(str(producto["id"]) + ":" + str(cantidad) + ":" + str(producto["precio"]))
        total += producto["precio"] * cantidad
    for id_producto in carrito:
        cantidad = carrito[id_producto]
        producto = buscar_producto_por_id(productos, id_producto)
        producto["stock"] = producto["stock"] - cantidad
    guardar_productos(productos)
    nuevo_id = generar_id(ventas)
    venta = {
        "id": nuevo_id,
        "usuario": usuario["nombre_usuario"],
        "detalle": ";".join(detalle_items),
        "total": total,
    }
    ventas.append(venta)
    guardar_ventas(ventas)
    carrito.clear()
    print(f"Compra realizada con exito. Total pagado: {formatear_precio(total)}")


def ver_historial_compras(ventas, usuario, productos):
    # Muestra todas las compras que hizo el usuario que inicio
    # sesion. Por cada venta encontrada, "desarma" el texto guardado
    # en "detalle" (que tiene el formato id:cantidad:precio separado
    # por ";", armado en finalizar_compra) para volver a mostrar
    # cada producto comprado con su nombre, cantidad y precio.
    imprimir_titulo("HISTORIAL DE COMPRAS")
    encontradas = []
    for v in ventas:
        if v["usuario"] == usuario["nombre_usuario"]:
            encontradas.append(v)
    if len(encontradas) == 0:
        print("No has realizado compras todavia.")
        return
    for v in encontradas:
        print(f"\nCompra #{v['id']} - Total: {formatear_precio(v['total'])}")
        items = v["detalle"].split(";")
        for item in items:
            partes = item.split(":")
            id_producto = int(partes[0])
            cantidad = int(partes[1])
            precio = float(partes[2])
            producto = buscar_producto_por_id(productos, id_producto)
            nombre = producto["nombre"] if producto is not None else f"Producto #{id_producto}"
            print(f"   - {nombre} x{cantidad} ({formatear_precio(precio)} c/u)")


def reporte_ventas(ventas):
    # Funcion solo para el administrador: muestra todas las ventas
    # realizadas (de todos los usuarios) y el total general vendido,
    # sumando el total de cada venta.
    imprimir_titulo("REPORTE DE VENTAS")
    if len(ventas) == 0:
        print("Aun no se han registrado ventas.")
        return
    total_general = 0.0
    for v in ventas:
        print(f"Venta #{v['id']} - Usuario: {v['usuario']} - Total: {formatear_precio(v['total'])}")
        total_general += v["total"]
    print("-" * 50)
    print(f"TOTAL GENERAL: {formatear_precio(total_general)}")


# =========================================================
# MENUS
# =========================================================
# Los "menus" son bucles (while True) que se repiten mostrando
# opciones numeradas hasta que el usuario elige salir/cerrar sesion.
# Segun el numero que escriba, se llama a la funcion correspondiente
# de las secciones anteriores.

def menu_cliente(usuario, productos, ventas):
    # Menu que ve un usuario con rol "cliente" despues de iniciar
    # sesion. Ademas de mostrar el menu, crea el carrito de compras
    # (vacio al principio) que se va a usar durante toda la sesion.
    carrito = {}
    while True:
        imprimir_titulo(f"MENU CLIENTE - {usuario['nombre_usuario']}")
        print("1. Ver catalogo")
        print("2. Buscar producto")
        print("3. Agregar producto al carrito")
        print("4. Ver carrito")
        print("5. Quitar producto del carrito")
        print("6. Finalizar compra")
        print("7. Ver historial de compras")
        print("8. Cerrar sesion")
        opcion = input("\nElige una opcion: ").strip()
        if opcion == "1":
            mostrar_productos(productos)
        elif opcion == "2":
            texto = pedir_texto("Buscar (nombre, categoria o plataforma): ")
            mostrar_productos(buscar_productos(productos, texto))
        elif opcion == "3":
            agregar_al_carrito(carrito, productos)
        elif opcion == "4":
            ver_carrito(carrito, productos)
        elif opcion == "5":
            quitar_del_carrito(carrito)
        elif opcion == "6":
            finalizar_compra(carrito, productos, ventas, usuario)
        elif opcion == "7":
            ver_historial_compras(ventas, usuario, productos)
        elif opcion == "8":
            print("Sesion cerrada.")
            break  # sale del bucle "while True" y termina la sesion
        else:
            print("Opcion invalida.")
        pausar()
        limpiar_pantalla()


def menu_admin(usuario, productos, usuarios, ventas):
    # Menu que ve un usuario con rol "admin" despues de iniciar
    # sesion. Da acceso a las tareas de administracion de la tienda:
    # manejar el catalogo, ver usuarios y ver el reporte de ventas.
    while True:
        imprimir_titulo(f"MENU ADMINISTRADOR - {usuario['nombre_usuario']}")
        print("1. Ver catalogo")
        print("2. Agregar producto")
        print("3. Editar producto")
        print("4. Eliminar producto")
        print("5. Ver usuarios registrados")
        print("6. Ver reporte de ventas")
        print("7. Cerrar sesion")
        opcion = input("\nElige una opcion: ").strip()
        if opcion == "1":
            mostrar_productos(productos)
        elif opcion == "2":
            agregar_producto(productos)
        elif opcion == "3":
            editar_producto(productos)
        elif opcion == "4":
            eliminar_producto(productos)
        elif opcion == "5":
            ver_usuarios(usuarios)
        elif opcion == "6":
            reporte_ventas(ventas)
        elif opcion == "7":
            print("Sesion cerrada.")
            break
        else:
            print("Opcion invalida.")
        pausar()
        limpiar_pantalla()


def main():
    # Funcion principal: es el punto de partida de todo el programa.
    # 1. Carga en memoria los productos, usuarios y ventas leyendo
    #    los 3 archivos .txt (con las funciones cargar_... de mas
    #    arriba). A partir de aca, todo el programa trabaja sobre
    #    estas listas que estan en memoria (son mas rapidas y
    #    comodas de usar que leer el archivo cada vez).
    # 2. Muestra el menu principal en un bucle infinito: iniciar
    #    sesion, registrarse o salir.
    # 3. Segun el rol del usuario que inicia sesion (admin o
    #    cliente), lo manda al menu correspondiente.

    productos = cargar_productos()
    usuarios = cargar_usuarios()
    ventas = cargar_ventas()
    while True:
        limpiar_pantalla()
        imprimir_titulo("TIENDA UPA - VIDEOJUEGOS")
        print("1. Iniciar sesion")
        print("2. Registrarse")
        print("3. Salir")
        opcion = input("\nElige una opcion: ").strip()
        if opcion == "1":
            usuario = iniciar_sesion(usuarios)
            if usuario is not None:
                pausar()
                limpiar_pantalla()
                if usuario["rol"] == "admin":
                    menu_admin(usuario, productos, usuarios, ventas)
                else:
                    menu_cliente(usuario, productos, ventas)
            else:
                pausar()
        elif opcion == "2":
            registrar_usuario(usuarios)
            pausar()
        elif opcion == "3":
            print("Gracias por visitar Tienda UPA. Hasta pronto!")
            break
        else:
            print("Opcion invalida.")
            pausar()


# Esta linea es la que realmente "enciende" el programa: llama a la
# funcion main() definida arriba y arranca la ejecucion.
main()
