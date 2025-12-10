
# 🍔 Burger App EV3 - Android Application

Aplicación Android para gestión de pedidos de hamburguesas personalizadas.

## 📱 Características Principales

✅ **Sistema de Autenticación Completo**
- Login y registro de usuarios
- Dos roles: Cliente y Administrador
- Validación de credenciales

✅ **Constructor de Hamburguesas Personalizadas**
- Selección de ingredientes
- Múltiples tipos de pan, carnes, quesos y salsas
- Visualización en tiempo real

✅ **Gestión de Pedidos**
- Historial completo de pedidos
- Estado de pedidos (pendiente, en proceso, completado)
- Adaptador personalizado para RecyclerView

✅ **Base de Datos Local**
- SQLite con `DatabaseHelper`
- Modelos: User, Burger, Ingredient, Order
- Operaciones CRUD completas

## 🏗️ Estructura del Proyecto

\`\`\`
burger-app-ev3/
├── app/
│   ├── src/main/java/com/B/carrasco/burgerapp/
│   │   ├── activities/          # Todas las actividades
│   │   │   ├── LoginActivity.java
│   │   │   ├── RegisterActivity.java
│   │   │   ├── ClientMainActivity.java
│   │   │   ├── AdminMainActivity.java
│   │   │   ├── BuildBurgerActivity.java
│   │   │   ├── OrderHistoryActivity.java
│   │   │   └── DepositUploadActivity.java
│   │   ├── adapters/           # Adaptadores para vistas
│   │   │   └── OrderAdapter.java
│   │   ├── database/           # Base de datos
│   │   │   └── DatabaseHelper.java
│   │   └── models/             # Modelos de datos
│   │       ├── User.java
│   │       ├── Burger.java
│   │       ├── Ingredient.java
│   │       └── Order.java
│   └── src/main/res/           # Recursos
│       ├── layout/             # Todos los layouts XML
│       ├── drawable/           # Imágenes y vectores
│       └── values/             # Strings, colors, themes
├── build.gradle                # Configuración principal
├── app/build.gradle           # Configuración del módulo app
└── README.md                  # Este archivo
\`\`\`

## 🚀 Instalación y Ejecución

1. **Clonar el repositorio**
   \`\`\`bash
   git clone https://github.com/ElArpan/burger-app-ev3.git
   cd burger-app-ev3
   \`\`\`

2. **Abrir en Android Studio**
    - Abre Android Studio
    - Selecciona \"Open an Existing Project\"
    - Navega a la carpeta del proyecto

3. **Sincronizar Gradle**
    - Android Studio sincronizará automáticamente
    - O manualmente: File > Sync Project with Gradle Files

4. **Ejecutar la aplicación**
    - Conecta un dispositivo Android o inicia un emulador
    - Click en Run > Run 'app'

## 🛠️ Configuración del Entorno

- **Android Studio**: 2022.3.1 o superior
- **Android SDK**: API 34 (Android 14)
- **Java**: JDK 17
- **Gradle**: 8.0+

## 📋 Flujo de la Aplicación

1. **Pantalla de Login** → Registro de nuevos usuarios
2. **Pantalla Principal (Cliente)** →
    - Ver hamburguesas disponibles
    - Crear hamburguesa personalizada
    - Ver historial de pedidos
3. **Pantalla Principal (Admin)** →
    - Gestionar todos los pedidos
    - Ver estadísticas
4. **Constructor de Hamburguesas** →
    - Personalizar cada ingrediente
    - Ver precio total
    - Confirmar pedido

## 🔧 Funcionalidades Técnicas

- **Navegación entre Activities** con Intents
- **Base de Datos SQLite** con operaciones seguras
- **RecyclerView** con adaptador personalizado
- **Material Design** components
- **Manejo de eventos** y validaciones
- **Persistencia de datos** local

## 👤 Roles de Usuario

### 🧑‍💼 Cliente
- Crear y personalizar hamburguesas
- Ver historial de pedidos
- Realizar nuevos pedidos

### 👨‍💼 Administrador
- Ver todos los pedidos del sistema
- Cambiar estados de pedidos
- Gestionar ingredientes disponibles

## 📊 Modelos de Datos

\`\`\`java
// Ejemplo de modelo User
public class User {
private int id;
private String username;
private String password;
private String role; // \"client\" o \"admin\"
private String email;
}
\`\`\`

## 🐛 Solución de Problemas Comunes

### Error: \"Gradle sync failed\"
- Verifica la conexión a Internet
- Asegúrate de tener el JDK correcto instalado
- Prueba: File > Invalidate Caches and Restart

### Error: \"App not installed\"
- Desinstala versiones anteriores
- Verifica que el dispositivo tenga espacio suficiente
- Asegúrate de que la firma del APK sea correcta

## 🤝 Contribución

1. Haz fork del repositorio
2. Crea una rama para tu feature (\`git checkout -b feature/AmazingFeature\`)
3. Commit tus cambios (\`git commit -m 'Add some AmazingFeature'\`)
4. Push a la rama (\`git push origin feature/AmazingFeature\`)
5. Abre un Pull Request

## 📄 Licencia

Distribuido bajo la Licencia MIT. Ver \`LICENSE\` para más información.

## ✍️ Autor

**Benjamín Carrasco**
- GitHub: [@ElArpan](https://github.com/ElArpan)
- Proyecto desarrollado para el curso de Android

## 🙏 Agradecimientos

- Android Studio team
- Google Developers
- Stack Overflow community
- Tutores y compañeros de curso

---
⭐ **Si te gusta este proyecto, dale una estrella en GitHub!**