# Silve Seguridad

Aplicación Android enfocada en seguridad personal y digital.

## Versión 1.0

- Panel principal de seguridad.
- Análisis inicial del dispositivo.
- Comprobador de enlaces (módulo en desarrollo).
- Asistente de seguridad (módulo en desarrollo).
- Sistema de emergencia (módulo en desarrollo).
- Permisos mínimos y privacidad como principios de diseño.

## Compilación

El proyecto usa Android Gradle Plugin, Kotlin y Java 17. GitHub Actions compila automáticamente un APK de depuración en cada push a `main` y lo publica como artefacto de workflow.

## Principios

Silve Seguridad no debe espiar dispositivos, extraer credenciales ni acceder a sistemas sin autorización. Las funciones de seguridad se implementarán usando APIs públicas de Android y acciones autorizadas por el usuario.
