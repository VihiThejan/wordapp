# Word Game - Quick Reference Guide

## 🔑 API Keys Setup

### 1. API Ninjas Configuration

**local.properties:**
```properties
API_NINJAS_KEY=your_api_key_here
```

**build.gradle.kts (app level):**
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "API_NINJAS_KEY", "\"${project.findProperty("API_NINJAS_KEY")}\"")
    }
    buildFeatures {
        buildConfig = true
    }
}
```

### 2. Dreamlo Configuration

**local.properties:**
```properties
DREAMLO_PUBLIC_KEY=your_public_key
DREAMLO_PRIVATE_KEY=your_private_key
```

### 3. Network Security (AndroidManifest.xml)

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

**res/xml/network_security_config.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">dreamlo.com</domain>
    </domain-config>
</network-security-config>
```

---

## 📦 Required Dependencies

**build.gradle.kts (app level):**
```kotlin
dependencies {
    // Existing dependencies...
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-compiler:2.47")
    
    // Coroutines & Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Additional UI
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}
```

---

## 🏗️ Core Code Templates

### 1. Game State Model

```kotlin
data class GameState(
    val secretWord: String = "",
    val currentScore: Int = 100,
    val attemptsRemaining: Int = 10,
    val guessHistory: List<String> = emptyList(),
    val level: Int = 1,
    val hintsUsed: Int = 0,
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val timeStarted: Long = 0L
)

enum class GameStatus {
    IN_PROGRESS, WON, LOST
}
```

### 2. Preferences Manager

```kotlin
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
    
    var userName: String?
        get() = prefs.getString("USER_NAME", null)
        set(value) = prefs.edit().putString("USER_NAME", value).apply()
    
    val isFirstLaunch: Boolean
        get() = userName == null
        
    var highScore: Int
        get() = prefs.getInt("HIGH_SCORE", 0)
        set(value) = prefs.edit().putInt("HIGH_SCORE", value).apply()
}
```

### 3. API Service Interfaces

```kotlin
interface WordApiService {
    @GET("v1/randomword")
    @Headers("X-Api-Key: ${BuildConfig.API_NINJAS_KEY}")
    suspend fun getRandomWord(): RandomWordResponse
    
    @GET("v1/rhyme")  
    @Headers("X-Api-Key: ${BuildConfig.API_NINJAS_KEY}")
    suspend fun getRhymes(@Query("word") word: String): RhymeResponse
    
    @GET("v1/thesaurus")
    @Headers("X-Api-Key: ${BuildConfig.API_NINJAS_KEY}")
    suspend fun getSynonyms(@Query("word") word: String): ThesaurusResponse
}

interface LeaderboardApiService {
    @GET("dreamlo/{publicKey}/json")
    suspend fun getLeaderboard(
        @Path("publicKey") publicKey: String
    ): LeaderboardResponse
    
    @GET("dreamlo/{privateKey}/add/{name}/{score}/{time}")
    suspend fun submitScore(
        @Path("privateKey") privateKey: String,
        @Path("name") name: String,
        @Path("score") score: Int,
        @Path("time") time: Long
    ): Response<Unit>
}
```

### 4. Game Timer Implementation

```kotlin
class GameTimer {
    private var startTime: Long = 0
    private var isRunning = false
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private var timerJob: Job? = null
    
    fun start(scope: CoroutineScope) {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            isRunning = true
            startTimerUpdates(scope)
        }
    }
    
    fun stop(): Long {
        isRunning = false
        timerJob?.cancel()
        return if (startTime != 0L) {
            (System.currentTimeMillis() - startTime) / 1000
        } else 0L
    }
    
    private fun startTimerUpdates(scope: CoroutineScope) {
        timerJob = scope.launch {
            while (isRunning) {
                delay(1000)
                _elapsedTime.value = (System.currentTimeMillis() - startTime) / 1000
            }
        }
    }
    
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
```

### 5. Game Logic Use Cases

```kotlin
class GuessWordUseCase @Inject constructor() {
    
    fun processGuess(
        currentState: GameState, 
        guess: String
    ): GameState {
        val normalizedGuess = guess.trim().lowercase()
        val normalizedSecret = currentState.secretWord.lowercase()
        
        return if (normalizedGuess == normalizedSecret) {
            // Correct guess - level up
            currentState.copy(
                gameStatus = GameStatus.WON,
                guessHistory = currentState.guessHistory + guess
            )
        } else {
            // Wrong guess - deduct points and attempts
            val newScore = maxOf(0, currentState.currentScore - 10)
            val newAttempts = maxOf(0, currentState.attemptsRemaining - 1)
            val newHistory = currentState.guessHistory + guess
            
            val gameStatus = when {
                newScore == 0 || newAttempts == 0 -> GameStatus.LOST
                else -> GameStatus.IN_PROGRESS
            }
            
            currentState.copy(
                currentScore = newScore,
                attemptsRemaining = newAttempts,
                guessHistory = newHistory,
                gameStatus = gameStatus
            )
        }
    }
    
    fun checkLetterOccurrence(word: String, letter: Char): Int {
        return word.count { it.equals(letter, ignoreCase = true) }
    }
}
```

### 6. Hilt Modules

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWordApiService(retrofit: Retrofit): WordApiService {
        return retrofit.create(WordApiService::class.java)
    }
}
```

---

## 🎯 Implementation Checklist

### Phase 1: Setup (Day 1-2)
- [ ] Add all dependencies to build.gradle.kts
- [ ] Set up API keys in local.properties
- [ ] Create network security config
- [ ] Set up Hilt application class

### Phase 2: Core Structure (Day 3-5)
- [ ] Create all data models
- [ ] Implement PreferencesManager
- [ ] Set up API service interfaces
- [ ] Create repository classes
- [ ] Implement basic ViewModels

### Phase 3: UI Implementation (Day 6-8)
- [ ] Create OnboardingActivity
- [ ] Design main GameActivity layout
- [ ] Implement game controls UI
- [ ] Add LeaderboardFragment
- [ ] Create dialogs and loading states

### Phase 4: Business Logic (Day 9-11)
- [ ] Implement complete game logic
- [ ] Add timer functionality
- [ ] Create hint system
- [ ] Implement scoring system
- [ ] Add level progression

### Phase 5: Polish & Testing (Day 12-14)
- [ ] Add error handling
- [ ] Implement loading states
- [ ] Test all features thoroughly
- [ ] Polish UI/UX
- [ ] Final testing and submission

---

## 🚨 Important Notes

### API Usage Tips:
1. **API Ninjas**: Use the free tier wisely (limited requests)
2. **Dreamlo**: Handle HTTP endpoints properly
3. **Error Handling**: Always have fallback mechanisms

### Testing Strategy:
1. Test with airplane mode (offline functionality)
2. Test with slow network connections
3. Test on different screen sizes
4. Test app lifecycle (background/foreground)

### Common Pitfalls to Avoid:
1. Not handling API failures gracefully
2. Forgetting to add network security config for Dreamlo
3. Not implementing proper loading states
4. Missing input validation
5. Not handling app lifecycle properly

**Good Luck with Your Implementation!** 🚀