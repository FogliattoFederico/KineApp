# KineApp 🩺

**KineApp** es una solución integral diseñada para profesionales de la kinesiología y fisioterapia, enfocada en simplificar la gestión administrativa de la práctica clínica diaria. La aplicación permite organizar la agenda, realizar un seguimiento detallado de los pacientes y automatizar el control de sesiones por obras sociales.

## 🚀 Características Principales

*   **📅 Agenda Inteligente:** Gestión de turnos diarios con visualización de estado (pendiente/atendido) y soporte optimizado para **Modo Oscuro**.
*   **👥 Gestión de Pacientes:** Base de datos centralizada con información de contacto, diagnósticos y antecedentes.
*   **📋 Control de Coberturas:** Sistema especializado para diferenciar entre tipos de atención:
    *   **Particular:** Registro de pagos directos.
    *   **CUD (Certificado Único de Discapacidad):** Seguimiento de atención prioritaria.
    *   **Orden Médica:** Contador automático de sesiones restantes y consumidas (ej. 1/10).
*   **🏠 Modalidad de Atención:** Soporte para consultas en **Consultorio** y visitas a **Domicilio**.
*   **💰 Facturación y Rendimiento:** Módulo para visualizar honorarios generados y valor de las sesiones.
*   **☁️ Sincronización en Tiempo Real:** Integración completa con **Firebase** (Firestore y Authentication).

## 🔒 Seguridad y Privacidad

Para garantizar la confidencialidad de los datos de salud de los pacientes, la app implementa:
*   **🛡️ Autenticación Biométrica:** Bloqueo de acceso mediante huella dactilar o reconocimiento facial.
*   **🔑 Cifrado de Credenciales:** Almacenamiento seguro de datos locales mediante `EncryptedSharedPreferences` (AES-256).
*   **🕵️ Ofuscación de Código:** Protección mediante R8/ProGuard para evitar ingeniería inversa.
*   **📜 Reglas de Firebase:** Acceso restringido a nivel de base de datos; cada profesional solo puede acceder a su propia información.

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Java (Android SDK)
*   **Arquitectura:** Patrón Repository para el manejo de datos.
*   **Base de Datos:** Firebase Firestore.
*   **Autenticación:** Firebase Auth.
*   **UI/UX:** Material Design 3.

## ⚙️ Configuración del Proyecto

Para proteger la seguridad de la infraestructura, el archivo de configuración de Firebase (`google-services.json`) no está incluido en este repositorio. Para ejecutar el proyecto localmente:

1.  Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2.  Registra la aplicación con el ID de paquete `frgp.utn.edu.kineapp`.
3.  Descarga el archivo `google-services.json` y colócalo en la carpeta `app/`.
4.  Habilita **Firestore Database** y **Firebase Authentication** en tu consola de Firebase.

---
*Desarrollado como parte del trayecto formativo en UTN - FRGP.*
