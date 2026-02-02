# TravelScribe

A modern Android application for converting mixed-language speech (Hindi/English) into structured, editable English travel logs.

## 📱 Features

- **Voice Recording**: Record travel experiences in mixed Hindi/English speech
- **AI-Powered Transcription**: Backend LLM converts speech to structured English text
- **Expense Tracking**: Automatic extraction of expenses with editable amounts and currencies
- **Trip Organization**: Notes organized by Trip → Day → Log hierarchy
- **Offline Support**: Full local storage with Room Database

## 🏗️ Architecture

This project follows **Clean Architecture** with **MVVM** pattern:

```
app/
├── data/           # Data layer (repositories impl, data sources, DTOs)
├── domain/         # Domain layer (use cases, entities, repository interfaces)
├── presentation/   # Presentation layer (UI, ViewModels, Compose screens)
├── di/             # Dependency Injection modules
└── core/           # Core utilities, extensions, constants
```

### Layers

1. **Domain Layer**: Contains business logic, entities, and repository interfaces. No Android dependencies.
2. **Data Layer**: Implements repositories, handles API calls and local database operations.
3. **Presentation Layer**: Contains UI components (Jetpack Compose), ViewModels, and UI state.

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Async | Coroutines & Flow |
| Network | Retrofit + OkHttp |
| Local Storage | Room Database |
| Audio | Android MediaRecorder API |

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Minimum SDK: 26 (Android 8.0)

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd TravelScribe
```

### 2. API Keys Configuration

Create a `local.properties` file in the root directory (if not exists) and add:

```properties
# LLM API Configuration
LLM_API_BASE_URL=https://your-api-endpoint.com/
LLM_API_KEY=your_api_key_here
```

**⚠️ Security Note**: Never commit `local.properties` to version control. It's already in `.gitignore`.

### 3. Build Configuration

The API keys are read from `local.properties` and injected as BuildConfig fields:

```kotlin
// Access in code via:
BuildConfig.LLM_API_BASE_URL
BuildConfig.LLM_API_KEY
```

### 4. Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## 📁 Project Structure

```
app/src/main/java/com/travelscribe/
├── TravelScribeApp.kt              # Application class
├── MainActivity.kt                  # Single Activity entry point
│
├── core/
│   ├── audio/
│   │   └── VoiceRecorderManager.kt # Audio recording interface
│   ├── common/
│   │   ├── Result.kt               # Result wrapper
│   │   └── Constants.kt            # App constants
│   ├── extensions/                  # Kotlin extensions
│   └── utils/                       # Utility classes
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── TravelScribeDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── TripDao.kt
│   │   │   │   ├── TravelDayDao.kt
│   │   │   │   └── TravelLogDao.kt
│   │   │   └── entity/
│   │   │       ├── TripEntity.kt
│   │   │       ├── TravelDayEntity.kt
│   │   │       └── TravelLogEntity.kt
│   │   └── datastore/              # DataStore preferences
│   ├── remote/
│   │   ├── api/
│   │   │   └── LlmApiService.kt
│   │   ├── dto/
│   │   │   ├── TranscriptionRequestDto.kt
│   │   │   └── TranscriptionResponseDto.kt
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt
│   ├── repository/
│   │   ├── TripRepositoryImpl.kt
│   │   ├── TravelDayRepositoryImpl.kt
│   │   ├── TravelLogRepositoryImpl.kt
│   │   └── TranscriptionRepositoryImpl.kt
│   └── mapper/
│       └── EntityMappers.kt
│
├── domain/
│   ├── model/
│   │   ├── Trip.kt
│   │   ├── TravelDay.kt
│   │   ├── TravelLog.kt
│   │   └── Expense.kt
│   ├── repository/
│   │   ├── TripRepository.kt
│   │   ├── TravelDayRepository.kt
│   │   ├── TravelLogRepository.kt
│   │   └── TranscriptionRepository.kt
│   └── usecase/
│       ├── trip/
│       │   ├── CreateTripUseCase.kt
│       │   ├── GetTripsUseCase.kt
│       │   └── DeleteTripUseCase.kt
│       ├── day/
│       │   ├── CreateTravelDayUseCase.kt
│       │   └── GetTravelDaysUseCase.kt
│       ├── log/
│       │   ├── CreateTravelLogUseCase.kt
│       │   ├── UpdateTravelLogUseCase.kt
│       │   └── GetTravelLogsUseCase.kt
│       └── transcription/
│           └── TranscribeAudioUseCase.kt
│
├── presentation/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   ├── components/
│   │   ├── RecordButton.kt
│   │   ├── ExpenseCard.kt
│   │   ├── TripCard.kt
│   │   └── LoadingIndicator.kt
│   └── screens/
│       ├── trips/
│       │   ├── TripsScreen.kt
│       │   ├── TripsViewModel.kt
│       │   └── TripsUiState.kt
│       ├── tripdetail/
│       │   ├── TripDetailScreen.kt
│       │   ├── TripDetailViewModel.kt
│       │   └── TripDetailUiState.kt
│       ├── daydetail/
│       │   ├── DayDetailScreen.kt
│       │   ├── DayDetailViewModel.kt
│       │   └── DayDetailUiState.kt
│       └── recording/
│           ├── RecordingScreen.kt
│           ├── RecordingViewModel.kt
│           └── RecordingUiState.kt
│
└── di/
    ├── AppModule.kt
    ├── DatabaseModule.kt
    ├── NetworkModule.kt
    ├── RepositoryModule.kt
    └── UseCaseModule.kt
```

## 🗃️ Data Models

### Trip
```kotlin
data class Trip(
    val id: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### TravelDay
```kotlin
data class TravelDay(
    val id: Long,
    val tripId: Long,
    val date: LocalDate,
    val notes: String?,
    val createdAt: Instant
)
```

### TravelLog
```kotlin
data class TravelLog(
    val id: Long,
    val dayId: Long,
    val rawAudioPath: String?,
    val transcribedText: String,
    val expenses: List<Expense>,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Expense
```kotlin
data class Expense(
    val id: String,
    val item: String,
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory
)
```

## 🎤 Audio Recording

The app uses `VoiceRecorderManager` for handling microphone input:

```kotlin
interface VoiceRecorderManager {
    val recordingState: StateFlow<RecordingState>
    val amplitude: StateFlow<Int>
    
    suspend fun startRecording(): Result<String>
    suspend fun stopRecording(): Result<String>
    fun cancelRecording()
    fun release()
}
```

**Supported Formats**: `.m4a` (AAC) - optimal for speech recognition APIs.

## 🔄 LLM API Integration

### Request Format
```json
{
  "audio_url": "base64_encoded_audio_or_url",
  "source_languages": ["hi", "en"],
  "target_language": "en"
}
```

### Response Format
```json
{
  "narrative": "Today we visited the Taj Mahal...",
  "expenses": [
    {
      "item": "Entry ticket",
      "amount": 1100.0,
      "currency": "INR",
      "category": "ATTRACTION"
    }
  ]
}
```

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Code coverage
./gradlew jacocoTestReport
```

## 📄 License

[Your License Here]

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
