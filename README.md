# Oasis 🌌

> **Un refugio de paz para respirar, sonreír y desconectar del trabajo.**

Oasis es una aplicación nativa de Android diseñada con **Jetpack Compose** que busca transformar tus momentos de estrés laboral en pequeños oasis de serenidad. Mediante una guía visual de respiración rítmica y reflexiones profundas sobre la vida y el descanso, Oasis te invita a pausar, respirar y reconectar con el presente.

Este proyecto ha sido diseñado bajo una atmósfera visual de alta gama ("Sophisticated Dark"), que proporciona una paz y tranquilidad inmediatas desde el momento de abrir la aplicación.

<img width="320" height="714" alt="image" src="https://github.com/user-attachments/assets/8e713f43-2d43-4fa8-9ebf-e3fa8bf8df06" />


---

## ✍️ Autoría y Desarrollo

Este maravilloso proyecto es una creación original de:

**Alberto Arce**  
📧 [alberto.arce.ti@gmail.com](mailto:alberto.arce.ti@gmail.com)  
*Desarrollador enfocado en construir experiencias con impacto humano y bienestar.*

---

## ✨ Características Principales

### 🧘‍♂️ Ritmo de Respiración Guiada (*Breathing Pulse Metronome*)
- **Diferentes Modos de Respiración:** Selección interactiva sobre técnicas de salud reconocidas mundialmente:
  - *Calma Simple (4-4):* Ritmo rápido para soltar tensiones.
  - *Caja Zen (4-4-4-4):* Caja mental para enfoque y concentración ejecutiva.
  - *Sueño Profundo (4-7-8):* Patrón respiratorio místico para apagar el insomnio laboral.
  - *Sincronía Zen (5-5):* Coherencia cardíaca para balancear el corazón y las células.
- **Temporizadores de Cuenta Regresiva Visual:** Canvas nativo que dibuja un dial de respiración circular que se encoge e infla al compás milimétrico de cada fase (*Inspirar, Sostener, Vaciar*).
- **Metrónomo Acústico Cues:** Pitidos discretos de baja sonoridad (`ToneGenerator` nativo) que avisan del inicio de fase, permitiendo practicar con los ojos totalmente cerrados.
- **Glow Sincronizado:** Colores que mutan dinámicamente según la fase de aire activo (Cian para Inspirar, Ámbar para Retener, Orquídea para Expulsar, Pizarra para Vacío).

### 🗄️ Persistencia de Datos Completa (Room Database SQLite)
- **Historial de Práctica:** Almacenamiento seguro de cada pausa completada.
- **Rachas de Paz Activa (*Life Streaks*):** Un algoritmo en tiempo real analiza tus registros de práctica en base de datos para computar tus rachas de días consecutivos de meditación libre de estrés.
- **Santuario Personal de Mantras:** Guarda e interactúa con tus reflexiones místicas predilectas o inserta tus propias afirmaciones escritas. Puedes eliminarlas, compartirlas o copiarlas al portapapeles instantáneamente.
- **Mis Oasis Forjados:** Permite guardar de manera definitiva tus visuales generados junto a los prompts de traducción Zen creados con Inteligencia Artificial.

### 👤 Personalización Intuitiva & SOS Grounding
- **Tu Compañero Diario:** Posibilidad de registrar cómo te llama tu Santuario en preferencias persistidas localmente (`SharedPreferences`).
- **Guía de Emergencia SOS contra Ansiedad (Metodo 5-4-3-2-1):** Un panel interactivo de contención psicológica cognitiva contra ataques de pánico repentinos causados por el overwork, diseñado bajo pautas clínicas de enraizamiento.
- **Borrado Purificador de Datos:** Opción rápida en Ajustes para borrar el historial de paz o resetear tus rachas.

---

## 🛠️ Arquitectura Técnica

Oasis está desarrollado bajo los estándares modernos de desarrollo en el ecosistema Android:

- **Lenguaje:** [Kotlin](https://kotlinlang.org/) (100% moderno y seguro).
- **Framework de UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con paradigma declarativo y control de estados avanzado (`animateFloatAsState`, `LaunchedEffect`).
- **Design System:** Material Design 3 (M3) adaptado con configuraciones personalizadas de paletas oscuras.
- **Gradle Kotlin DSL:** Configuración robusta y flexible para la compilación y dependencias.

---

## 🚀 Cómo Empezar

### Requisitos Previos

- **Android Studio** (versión Ladybug o superior recomendada).
- **JDK 17** o posterior.
- Dispositivo físico Android o emulador con **Android API 24** (Nougat) en adelante.

### Instalación

1. Clona el repositorio desde GitHub:
   ```bash
   git clone https://github.com/tu-usuario/oasis-android.git
   ```
2. Abre el proyecto en Android Studio.
3. Deja que Gradle sincronice todas las dependencias definidas en `build.gradle.kts`.
4. ¡Dale al botón **Run** de Android Studio para desplegar Oasis en tu dispositivo preferido!

---

---
*Hecho con amor y enfocado en la felicidad de las personas. Tómate un respiro, el mundo puede esperar.* 👋
