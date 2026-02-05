# Dziennik Aktywności Fizycznej - Dokumentacja Techniczna

## Opis Projektu
Aplikacja mobilna do śledzenia aktywności fizycznej użytkownika. Umożliwia rejestrowanie treningów, liczenie kroków, śledzenie tras GPS oraz dokumentowanie aktywności zdjęciami.

---

## Wymagania Techniczne

### 1. Jetpack Compose
**Status:** ✔ Zaimplementowane

Cały interfejs użytkownika został zbudowany przy użyciu Jetpack Compose - nowoczesnego toolkit'u do tworzenia natywnych UI w Androidzie.

**Zastosowanie w projekcie:**
- Deklaratywne budowanie ekranów (HomeScreen, ActivityScreen, HistoryScreen, etc.)
- Komponenty Material Design 3
- Dynamiczne aktualizacje UI w czasie rzeczywistym (np. statystyki podczas treningu)
- Wykresy aktywności przy użyciu biblioteki Vico

**Screenshot:**

[Miejsce na screenshot - ekran główny aplikacji]

---

### 2. Navigation Compose
**Status:** ✔ Zaimplementowane

Nawigacja między ekranami wykorzystuje Navigation Compose z type-safe routes.

**Zastosowanie w projekcie:**
- Bezpieczne typowo trasy zdefiniowane jako sealed class/data class
- Przekazywanie argumentów między ekranami (np. activityId do ekranu szczegółów)
- Obsługa stosu nawigacji (back stack)
- Kotlinx.serialization do serializacji argumentów

**Zdefiniowane trasy:**
| Route | Opis |
|-------|------|
| Home | Ekran główny z podsumowaniem dnia |
| Activity | Ekran aktywnego treningu |
| History | Lista historii treningów |
| ActivityDetail(id) | Szczegóły wybranego treningu |
| PhotoGallery | Galeria zdjęć z treningów |
| Settings | Ustawienia aplikacji |

**Screenshot:**

[Miejsce na screenshot - nawigacja między ekranami]

---

### 3. Kotlin
**Status:** ✔ Zaimplementowane

Aplikacja napisana w całości w języku Kotlin.

**Zastosowanie w projekcie:**
- Kotlin Coroutines do operacji asynchronicznych
- Flow do reaktywnego strumienia danych
- Extension functions dla czytelności kodu
- Data classes dla modeli danych
- Null-safety dla bezpieczeństwa typów

---

### 4. Architektura MVVM
**Status:** ✔ Zaimplementowane

Aplikacja wykorzystuje wzorzec MVVM (Model-View-ViewModel) z UiState.

**Zastosowanie w projekcie:**

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    View     │────▶│  ViewModel  │────▶│  Repository │
│  (Compose)  │◀────│  + UiState  │◀────│  (Room DB)  │
└─────────────┘     └─────────────┘     └─────────────┘
```

- **ViewModel** - zarządza stanem UI i logiką biznesową
- **UiState** - reprezentuje stan ekranu (loading, success, error)
- **Repository** - abstrakcja dostępu do danych
- **Room Database** - lokalna baza danych

**Komponenty:**
| Warstwa | Przykłady |
|---------|-----------|
| View | HomeScreen, ActivityScreen |
| ViewModel | HomeViewModel, ActivityViewModel |
| Model | ActivityEntity, DailyStepsEntity |
| Repository | FitnessDatabase, DAOs |

**Screenshot:**

[Miejsce na screenshot - diagram architektury lub struktura projektu]

---

### 5. Runtime Permissions
**Status:** ✔ Zaimplementowane

Aplikacja poprawnie obsługuje uprawnienia runtime zgodnie z wytycznymi Androida.

**Wymagane uprawnienia:**

| Uprawnienie | Cel | Typ |
|-------------|-----|-----|
| `ACTIVITY_RECOGNITION` | Licznik kroków | Runtime (Android 10+) |
| `ACCESS_FINE_LOCATION` | Śledzenie trasy GPS | Runtime |
| `ACCESS_COARSE_LOCATION` | Lokalizacja przybliżona | Runtime |
| `CAMERA` | Robienie zdjęć podczas treningu | Runtime |
| `POST_NOTIFICATIONS` | Powiadomienia o treningu | Runtime (Android 13+) |

**Obsługa uprawnień:**
- Wyświetlanie uzasadnienia przed prośbą o uprawnienie
- Graceful degradation - aplikacja działa bez niektórych uprawnień
- Przekierowanie do ustawień systemu przy odmowie

**Screenshot:**

[Miejsce na screenshot - dialog z prośbą o uprawnienia]

---

## Dodatkowe Technologie

| Technologia | Wersja | Zastosowanie |
|-------------|--------|--------------|
| Room | 2.6.1 | Lokalna baza danych |
| DataStore | 1.0.0 | Przechowywanie preferencji |
| Vico | 1.13.1 | Wykresy aktywności |
| Coil | 2.5.0 | Ładowanie zdjęć |
| Play Services Location | 21.1.0 | GPS tracking |

---

## Konfiguracja Build

```kotlin
android {
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
}
```

---

## Screenshoty Aplikacji

### Ekran Główny
[Miejsce na screenshot]

### Ekran Treningu
[Miejsce na screenshot]

### Historia Aktywności
[Miejsce na screenshot]

### Szczegóły Treningu
[Miejsce na screenshot]

### Galeria Zdjęć
[Miejsce na screenshot]

### Ustawienia
[Miejsce na screenshot]

---

*Dokumentacja wygenerowana dla projektu "Dziennik Aktywności Fizycznej"*
