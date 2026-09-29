RUTA_PRODUCTOS = "data/productos.txt"
RUTA_USUARIOS = "data/usuarios.txt"
RUTA_VENTAS = "data/ventas.txt"


# =========================================================
# UTILIDADES
# =========================================================

def limpiar_pantalla():
    print("\n" * 60)


def pausar():
    input("\nPresiona ENTER para continuar...")


def imprimir_titulo(texto):
    print("\n" + "=" * 60)
    print(texto.center(60))
    print("=" * 60)


def hash_clave(clave):
    total = 0
    for i in range(len(clave)):
        total = total + (ord(clave[i]) * (i + 1) * 31)
    return str(total)


def pedir_entero(mensaje, minimo=None):
    while True:
        valor = input(mensaje).strip()
        if not valor.isdigit():
            print("Ingresa un numero entero valido.")
            continue
        valor = int(valor)
        if minimo is not None and valor < minimo:
            print(f"El valor debe ser mayor o igual a {minimo}.")
            continue
        return valor


def pedir_flotante(mensaje, minimo=None):
    while True:
        valor = input(mensaje).strip().replace(",", ".")
        try:
            valor = float(valor)
        except ValueError:
            print("Ingresa un numero valido.")
            continue
        if minimo is not None and valor < minimo:
            print(f"El valor debe ser mayor o igual a {minimo}.")
            continue
        return valor


def pedir_texto(mensaje):
    while True:
        valor = input(mensaje).strip()
        if valor != "":
            return valor
        print("Este campo no puede estar vacio.")


def formatear_precio(precio):
    return f"${precio:,.2f}"


# =========================================================
# PERSISTENCIA EN ARCHIVOS DE TEXTO
# =========================================================

def cargar_productos():
    productos = []
    try:
        archivo = open(RUTA_PRODUCTOS, "r", encoding="utf-8")
    except FileNotFoundError:
        return productos
    for linea in archivo:
        linea = linea.strip()
        if linea == "":
            continue
        partes = linea.split("|")
        productos.append({
            "id": int(partes[0]),
            "nombre": partes[1],
            "categoria": partes[2],
            "plataforma": partes[3],
            "precio": float(partes[4]),
            "stock": int(partes[5]),
        })
    archivo.close()
    return productos


def guardar_productos(productos):
    archivo = open(RUTA_PRODUCTOS, "w", encoding="utf-8")
    for p in productos:
        linea = str(p["id"]) + "|" + p["nombre"] + "|" + p["categoria"] + "|" + p["plataforma"] + "|" + str(p["precio"]) + "|" + str(p["stock"]) + "\n"
        archivo.write(linea)
    archivo.close()


def cargar_usuarios():
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
    archivo = open(RUTA_USUARIOS, "w", encoding="utf-8")
    for u in usuarios:
        linea = str(u["id"]) + "|" + u["nombre_usuario"] + "|" + u["clave_hash"] + "|" + u["rol"] + "\n"
        archivo.write(linea)
    archivo.close()


def cargar_ventas():
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
    archivo = open(RUTA_VENTAS, "w", encoding="utf-8")
    for v in ventas:
        linea = str(v["id"]) + "|" + v["usuario"] + "|" + v["detalle"] + "|" + str(v["total"]) + "\n"
        archivo.write(linea)
    archivo.close()


def generar_id(lista):
    if len(lista) == 0:
        return 1
    ids = []
    for elemento in lista:
        ids.append(elemento["id"])
    return max(ids) + 1


# =========================================================
# PRODUCTOS / CATALOGO
# =========================================================

def mostrar_productos(productos):
    if len(productos) == 0:
        print("No hay productos disponibles.")
        return
    print(f"{'ID':<4}{'Nombre':<44}{'Categoria':<14}{'Plataforma':<16}{'Precio':<12}{'Stock':<6}")
    print("-" * 96)
    for p in productos:
        print(f"{p['id']:<4}{p['nombre']:<44}{p['categoria']:<14}{p['plataforma']:<16}{formatear_precio(p['precio']):<12}{p['stock']:<6}")


def buscar_productos(productos, texto):
    texto = texto.lower()
    resultado = []
    for p in productos:
        if texto in p["nombre"].lower() or texto in p["categoria"].lower() or texto in p["plataforma"].lower():
            resultado.append(p)
    return resultado


def buscar_producto_por_id(productos, id_producto):
    for p in productos:
        if p["id"] == id_producto:
            return p
    return None


def agregar_producto(productos):
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

def usuario_existe(usuarios, nombre_usuario):
    for u in usuarios:
        if u["nombre_usuario"].lower() == nombre_usuario.lower():
            return True
    return False


def registrar_usuario(usuarios):
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
    imprimir_titulo("USUARIOS REGISTRADOS")
    print(f"{'ID':<5}{'Usuario':<25}{'Rol':<10}")
    print("-" * 40)
    for u in usuarios:
        print(f"{u['id']:<5}{u['nombre_usuario']:<25}{u['rol']:<10}")


# =========================================================
# CARRITO DE COMPRAS
# =========================================================

def agregar_al_carrito(carrito, productos):
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

def menu_cliente(usuario, productos, ventas):
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
            break
        else:
            print("Opcion invalida.")
        pausar()
        limpiar_pantalla()


def menu_admin(usuario, productos, usuarios, ventas):
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



main()
