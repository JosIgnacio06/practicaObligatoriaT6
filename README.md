# FernanPop v4 – Manual de Usuario

<img width="1024" height="1536" alt="Fernanpop" src="https://github.com/user-attachments/assets/c7cdd749-63de-4d48-845f-eed7fc1439d7" />

FernanPop es un programa de software de compra y venta pensado para que los usuarios puedan intercambiar productos de forma rápida, sencilla y segura. Esta versión incluye notificaciones automáticas por correo electrónico y Telegram para los administradores del sistema.

## ÍNDICE
1. Descripción
2. Requisitos
3. Instalación y ejecución del software
4. Funcionamiento del software
5. Creadores

---

# 1. Descripción
FernanPop permite a los usuarios registrarse, iniciar sesión, gestionar productos en venta, comprar productos de otros usuarios y mantener un historial de ventas y compras. Además, el sistema envía correos electrónicos de verificación y notificaciones al administrador vía Telegram para mantener un control seguro y actualizado de la actividad de los usuarios.

---

# 2. Requisitos
Para ejecutar FernanPop se necesitan los siguientes requisitos mínimos:

* **Java Development Kit (JDK) actualizado:** El JDK incluye el intérprete Java, clases Java y herramientas de desarrollo.
* **Página oficial de descarga:** [JDK Oracle](https://www.oracle.com/java/technologies/downloads/)

<img width="1814" height="512" alt="386256659-74b6704e-60e6-4178-ae80-d17462e029b3" src="https://github.com/user-attachments/assets/14c0c81f-da7e-4c6a-ba05-b8a2eff01573" />

Descargar e instalar el paquete correspondiente a tu sistema operativo y seguir las instrucciones del instalador.

<img width="569" height="23" alt="idk-25_linux-aarch64_bin tar gz" src="https://github.com/user-attachments/assets/53dde56b-e9a6-460b-b358-12ecaeec367a" />

---

# 3. Instalación y ejecución del software
1. Descarga la carpeta comprimida **.zip** del proyecto.
2. Descomprime la carpeta.
3. Ejecuta el archivo **.bat** (Windows) o el ejecutable correspondiente para iniciar el programa.

⚠️ **Advertencia:** Al abrir el .bat, Windows Defender puede bloquear la ejecución. Selecciona “Más información” → “Ejecutar de todas formas”.

<img width="658" height="610" alt="Windows protegió su PC" src="https://github.com/user-attachments/assets/022b87ee-f0ed-46ae-8cfa-0c2650832e2d" />
<img width="657" height="604" alt="Windows protegió su PC" src="https://github.com/user-attachments/assets/196fa33d-6a44-474b-b7cb-2af1e281403f" />

El programa se abrirá en la consola CMD y mostrará la pantalla de inicio de sesión.

---

# 4. Funcionamiento del software
El sistema se divide en dos entornos diferenciados: el acceso público para consultas rápidas y el panel de gestión privada para usuarios registrados.

## ■ Menú Principal (Modo Invitado)

Al iniciar la aplicación, el usuario accede a un menú de navegación global sin necesidad de estar autenticado:

<img width="366" height="194" alt="image" src="https://github.com/user-attachments/assets/8b5924e2-cc28-4950-a13b-cd3c4b70e729" />

**1. Buscar Productos:** Permite consultar todo el catálogo de artículos disponibles en la plataforma, o también por el ID o el nombre del artículo.

<img width="366" height="194" alt="image" src="https://github.com/user-attachments/assets/e0753efc-6c1a-4be1-b16b-82292dd29cf2" />

**2. Iniciar sesión:** Acceso mediante credenciales (Email y Contraseña) para usuarios ya registrados.

<img width="465" height="194" alt="image" src="https://github.com/user-attachments/assets/b1480a28-3c2f-41be-9c11-5751537ff435" />

**3. Registrarse:** Formulario de creación de cuenta para nuevos miembros.

<img width="465" height="340" alt="image" src="https://github.com/user-attachments/assets/2263c88a-1e43-4a65-8131-e5a299b4429a" />

**4. Salir:** Finaliza la ejecución del programa.

---

## ■ Menú de Usuario (Sesión Iniciada)

Una vez logueado, el sistema muestra un indicador de notificaciones con los tratos pendientes de valorar.

<img width="333" height="294" alt="image" src="https://github.com/user-attachments/assets/2273ae67-eb71-4c5b-9832-859fff4cadb7" />

### Opción 1: Mostrar mi perfil de usuario
Visualiza de forma clara los datos vinculados a la cuenta activa (Nombre y Email).

### Opción 2: Cambiar mis datos personales
Módulo de edición para actualizar el nombre de perfil, el correo electrónico o la contraseña.

### Opción 3: Ver mis productos en venta
Muestra los artículos que has subido. Permite borrar un producto o venderlo (solicitando el email del comprador y enviando correos de verificación).

**1.- Mostrar los productos:**

<img width="434" height="224" alt="image" src="https://github.com/user-attachments/assets/4c176d35-a97f-4e22-8dfc-37ec7e41010b" />

**2.- Borrar un producto:**

<img width="434" height="224" alt="image" src="https://github.com/user-attachments/assets/8fb1ba16-fc23-4e9c-878e-6cb70d23816f" />

**3.- Vender un producto:**

<img width="434" height="239" alt="image" src="https://github.com/user-attachments/assets/e28b185f-45f6-4fd8-873b-0c0b42513fd3" />

### Opción 4: Introducir un producto para vender
Asistente para añadir artículos indicando Título, Descripción, Precio y Estado.

<img width="333" height="190" alt="image" src="https://github.com/user-attachments/assets/3d052476-a930-4e11-a0df-c378b89bf912" />

### Opción 5: Buscar Productos (Submenú de búsqueda)
Despliega un motor de búsqueda con tres filtros:

**Mostrar todos:** Listado general de productos.

<img width="434" height="350" alt="image" src="https://github.com/user-attachments/assets/6b57414e-5a3e-4087-ab2f-7f74e1118395" />

**Buscar por ID:** Localización por código numérico.

<img width="434" height="199" alt="image" src="https://github.com/user-attachments/assets/dbc30ee7-3f14-4b79-bb1d-e7892183aae3" />

**Buscar por texto:** Filtrado por palabras clave en el título.

<img width="434" height="189" alt="image" src="https://github.com/user-attachments/assets/68e4d895-9c29-428b-a068-bef994c7c02f" />

### Opción 6: Ver valoraciones pendientes
Lista compras no puntuadas. Permite asignar nota (1-5) y comentario, actualizando la reputación del vendedor.

**1.- Ver todas las valoraciones pendientes:**

<img width="434" height="416" alt="image" src="https://github.com/user-attachments/assets/1c92baf6-0199-4459-bdcf-edffbb97f38d" />

**2.- Valorar una venta por su ID:**

<img width="434" height="254" alt="image" src="https://github.com/user-attachments/assets/782ca5b8-f59a-4843-9151-7fcd332250e1" />

### Opción 7: Ver mi historial de tratos
Registro histórico dividido en:

**Compras:** Artículos adquiridos y sus valoraciones enviadas.

<img width="434" height="400" alt="image" src="https://github.com/user-attachments/assets/d7ffe146-3286-4e48-874c-6465528303da" />

**Ventas:** Artículos vendidos con puntuaciones recibidas.

<img width="434" height="439" alt="image" src="https://github.com/user-attachments/assets/a8d675ca-2f0f-4bc8-afe6-fe510a26338f" />

### Opción 8: Borrar mi perfil de usuario
Elimina de forma irreversible la cuenta y toda su información asociada.

### Opción 9: Cerrar sesión
Finaliza la sesión de forma segura y vuelve al Menú Principal.

### Opción 10: Salir
Cierra completamente la aplicación.

---

# 5. Creadores
Este software ha sido diseñado y desarrollado íntegramente por:

**Jose Ignacio Escuchas**
