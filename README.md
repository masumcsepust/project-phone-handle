# নূরানী কায়দা — Islamic Qaida & Arabic Letter Learning (Android)

A Bengali-first Android MVP for learning the 29 Arabic letters, their positional
forms (একক / শুরুতে / মাঝখানে / শেষে), pronunciation practice with an AI
assistant, and human verification by an Ustad (হুজুর যাচাই).

100% Kotlin · Jetpack Compose (Material 3) · single Activity · MVVM + Repository.

## Screens

| Tab | Screen | What it does |
|---|---|---|
| হোম | `HomeScreen` | Daily progress, letter hero card, Hujur status, AI voice card, quiz card |
| হরফ | `LettersScreen` | All 29 letters carousel, 4 positional forms, example words with **only the target letter in red**, TTS |
| এআই অ্যাসিস্ট | `AiAssistScreen` | 3-second simulated recording with animated waveform → AI score |
| হুজুর যাচাই | `HujurVerifyScreen` | Ustad score gauge, audio cards, improvement hints, pending reviews |
| প্রোফাইল | `ProfileScreen` | Level, achievements, stats, recent activity, menu |
| (from Home) | `QuizScreen` | 5-question letter-recognition quiz that adds quiz points |

## UX decisions made while merging the mockups

- **One consistent bottom bar.** The mockups used different labels and icons on each screen (for example "হরক" and a duplicated "এআই অ্যাসিস্ট"). The app always shows the same 5 tabs. The Hujur tab badge counts unheard feedback plus reviews still pending.
- **No fake results.** The AI card shows a hint until the learner actually records. "ওস্তাদকে পাঠান" only submits a real recording. With no recording yet, the app guides the learner to the AI screen instead.
- **A closed feedback loop.** A submission shows as *পর্যালোচনার অপেক্ষায়* and is "reviewed" about 8 s later by the mock backend. The badge and Home status pill then update.
- **Everything is tappable to hear.** Letters, positional forms and example words all speak through Arabic TTS. If no Arabic voice is installed, a Bengali toast tells the learner how to add one.
- **The Quiz card works.** It opens a real quiz.

## Project layout

```
app/src/main/java/com/nurani/qaida/
├── MainActivity.kt                 Scaffold + NavHost + bottom bar, TTS lifecycle
├── data/model/QaidaModels.kt       Letters, positional forms, AI/Hujur/profile models
├── data/repository/
│   ├── QaidaRepository.kt          The interface the UI depends on
│   └── MockQaidaRepositoryImpl.kt  29 letters + delay()-simulated network
├── ui/viewmodel/QaidaViewModel.kt  QaidaUiState (StateFlow) + one-shot UiEvents
├── ui/navigation/QaidaNavigation.kt Routes, tabs, bottom bar
├── ui/components/CommonComponents.kt Robot mascot, avatars, waveform, Islamic pattern, cards…
├── ui/screens/*.kt
├── ui/theme/{Color,Theme}.kt
└── util/                           ArabicTtsManager, Bengali digits
```

## Connecting a real backend

Every network call lives in `MockQaidaRepositoryImpl`, marked with
`// TODO: Replace with Retrofit/Ktor API`:
`getLetters`, `evaluatePronunciationWithAi`, `submitAudioToHujur`,
`getHujurFeedback`, `markUstadAudioHeard`, `getUserProfile`, `addQuizPoints`.

To connect a backend:
1. Write a `RemoteQaidaRepository : QaidaRepository`.
2. Swap it in `QaidaViewModel.Factory`.

No UI or ViewModel code changes. For real recording, replace the countdown in
`QaidaViewModel.startRecording()` with `MediaRecorder`, and request `RECORD_AUDIO`.

## Build & test

Requires JDK 17+ and the Android SDK (compileSdk 35). Runs on Android 7.0+ (minSdk 24).

```bash
./gradlew :app:assembleDebug            # APK → app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest        # logic + repository tests, screenshot smoke tests
./gradlew :app:testDebugUnitTest -Proborazzi.record=true   # write screen PNGs (Robolectric)
```
