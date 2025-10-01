# Word Guessing Game - Complete Implementation Plan

## ITE 2152 Mobile Application Development Assignment 2

### 📋 Project Overview

- **Course**: ITE 2152 Mobile Application Development  
- **Assignment**: Assignment 2 - Word Guessing Game
- **Platform**: Android (Native Kotlin)
- **Architecture**: MVVM + Clean Architecture
- **Total Points**: 85 marks

---

## 🎯 Feature Requirements & Implementation Plan

### Feature 1: Onboarding System (5 marks) ✅ COMPLETED

**Requirements:**
- ✅ First-time launch detection
- ✅ Name input with validation 
- ✅ Store name in SharedPreferences ("USER_NAME" key)
- ✅ Skip onboarding on subsequent launches
- ✅ Welcome message using stored name

**Implementation Tasks:**
- [x] Create OnboardingActivity/Fragment
- [x] Implement PreferencesManager class
- [x] Add name input validation
- [x] Design welcome UI screen
- [x] Handle navigation flow

**What was implemented:**
- SplashActivity with navigation logic
- OnboardingActivity with Material Design UI
- PreferencesManager with Hilt DI
- Input validation (2-20 characters, letters only)
- Personalized welcome messages in MainActivity and HomeFragment

**Code Structure:**
```kotlin
class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
    
    var userName: String?
        get() = prefs.getString("USER_NAME", null)
        set(value) = prefs.edit().putString("USER_NAME", value).apply()
    
    val isFirstLaunch: Boolean
        get() = userName == null
}
```

### Feature 2: Core Word Guessing Mechanism (25 marks) ✅ COMPLETED

**Requirements:**
- ✅ Fetch random words from API Ninjas (with fallback system)
- ✅ Case-insensitive word comparison
- ✅ Scoring system: Start with 100 points, -10 per wrong guess
- ✅ Attempts system: 10 attempts, -1 per guess
- ✅ Progressive difficulty based on level
- ✅ Game state management

**Difficulty Levels:**
- ✅ Level 1: 4-5 letter words
- ✅ Level 2: 6-7 letter words  
- ✅ Level 3: 8-9 letter words
- ✅ Level 4+: 10+ letter words

**Implementation Tasks:**
- [x] Create GameState data class
- [x] Implement WordRepository  
- [x] Set up Retrofit for API Ninjas
- [x] Create GameViewModel with scoring logic
- [x] Implement difficulty progression
- [x] Add game over conditions
- [x] Handle word validation

**What was implemented:**
- Complete game engine with state management
- WordRepository with fallback word system
- GameViewModel with reactive UI updates
- Real-time timer implementation
- Comprehensive game UI with Material Design
- Guess history tracking with visual feedback
- Level progression system
- Win/lose condition handling
- Letter checking and word length features integrated
- Hint system foundation (ready for API integration)

**Game State Model:**
```kotlin
data class GameState(
    val secretWord: String,
    val currentScore: Int = 100,
    val attemptsRemaining: Int = 10,
    val guessHistory: List<String> = emptyList(),
    val level: Int = 1,
    val hintsUsed: Int = 0,
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS
)

enum class GameStatus {
    IN_PROGRESS, WON, LOST
}
```

### Feature 3: Letter Occurrence Check (5 marks)

**Requirements:**
- Cost: 5 points per check
- Input: Single letter (A-Z, case-insensitive)
- Output: Count of letter occurrences in secret word
- Validate sufficient points before deduction

**Implementation Tasks:**
- [ ] Create letter input UI (A-Z buttons or input field)
- [ ] Implement letter counting logic
- [ ] Add point validation and deduction
- [ ] Display occurrence count result
- [ ] Handle insufficient points case

### Feature 4: Word Length Query (5 marks)

**Requirements:**
- Cost: 5 points
- Returns: Integer count of letters in secret word
- Button-based activation

**Implementation Tasks:**
- [ ] Add "Get Word Length" button
- [ ] Implement point deduction logic
- [ ] Display word length result
- [ ] Handle insufficient points scenario

### Feature 5: Hint/Tip System (5 marks)

**Requirements:**
- Available only after 5 failed attempts
- One hint per game session
- Provide rhyming word (Rhyme API) or synonym (Thesaurus API)
- Fallback between APIs

**Implementation Tasks:**
- [ ] Set up Rhyme and Thesaurus API endpoints
- [ ] Implement hint availability logic (after 5 attempts)
- [ ] Create hint retrieval with API fallback
- [ ] Add hint display UI
- [ ] Prevent multiple hint usage per game

**API Integration:**
```kotlin
interface HintApiService {
    @GET("v1/rhyme")
    @Headers("X-Api-Key: YOUR_API_KEY")
    suspend fun getRhymes(@Query("word") word: String): RhymeResponse
    
    @GET("v1/thesaurus") 
    @Headers("X-Api-Key: YOUR_API_KEY")
    suspend fun getSynonyms(@Query("word") word: String): ThesaurusResponse
}
```

### Feature 6: Timer Implementation (5 marks)

**Requirements:**
- Start timer when word is loaded
- Stop timer on correct guess
- Display in MM:SS format
- Store completion time for leaderboard

**Implementation Tasks:**
- [ ] Create GameTimer class
- [ ] Implement timer UI display
- [ ] Add start/stop timer logic
- [ ] Format time display (MM:SS)
- [ ] Store completion time on win

**Timer Implementation:**
```kotlin
class GameTimer {
    private var startTime: Long = 0
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    fun start() {
        startTime = System.currentTimeMillis()
        startTimerUpdates()
    }
    
    fun stop(): Long {
        return (System.currentTimeMillis() - startTime) / 1000
    }
}
```

### Feature 7: Global Leaderboard (10 marks)

**Requirements:**
- Use Dreamlo.com API backend
- Submit score and time on successful guess
- Display top 10-20 players
- Handle SSL/HTTP certificate issues

**Implementation Tasks:**
- [ ] Set up Dreamlo API integration
- [ ] Configure network security for HTTP
- [ ] Create LeaderboardRepository
- [ ] Implement score submission
- [ ] Design leaderboard UI (RecyclerView)
- [ ] Add pull-to-refresh functionality
- [ ] Handle network errors

**Network Security Config:**
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">dreamlo.com</domain>
    </domain-config>
</network-security-config>
```

---

## 🏗️ Technical Implementation Plan

### Phase 1: Project Setup & Dependencies

**Tasks:**
- [ ] Update build.gradle.kts with required dependencies
- [ ] Set up Hilt for dependency injection
- [ ] Configure Retrofit and OkHttp
- [ ] Add API keys to local.properties
- [ ] Set up network security configuration

**Required Dependencies:**
```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Dependency Injection
implementation("com.google.dagger:hilt-android:2.47")
kapt("com.google.dagger:hilt-compiler:2.47")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Additional UI
implementation("androidx.recyclerview:recyclerview:1.3.1")
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
```

### Phase 2: Data Layer Implementation

**Tasks:**
- [ ] Create data models (GameState, LeaderboardEntry, etc.)
- [ ] Implement API service interfaces
- [ ] Create repository classes
- [ ] Set up SharedPreferences manager
- [ ] Implement error handling classes

**Project Structure:**
```
com.example.wordapp/
├── data/
│   ├── model/          # GameState, LeaderboardEntry, ApiResponses
│   ├── repository/     # WordRepository, LeaderboardRepository  
│   ├── remote/         # API services and implementations
│   └── local/          # PreferencesManager
├── domain/
│   ├── usecase/        # GuessWordUseCase, GetHintUseCase
│   └── model/          # Domain models
├── presentation/
│   ├── onboarding/     # OnboardingActivity + ViewModel
│   ├── game/           # GameActivity + ViewModel
│   ├── leaderboard/    # LeaderboardFragment + ViewModel
│   └── common/         # Shared UI components
└── di/                 # Hilt modules
```

### Phase 3: UI Layer Development

**Tasks:**
- [ ] Create OnboardingActivity with name input
- [ ] Design main GameActivity layout
- [ ] Implement game controls (guess input, action buttons)
- [ ] Create LeaderboardFragment
- [ ] Add timer display component
- [ ] Implement dialogs for results/errors
- [ ] Add loading states and animations

**Main Game Screen Layout:**
- **Top Section**: Player name, level, score, attempts, timer
- **Middle Section**: Word input field, submit button, guess history
- **Bottom Section**: Letter check, word length, hint buttons

### Phase 4: Business Logic Implementation

**Tasks:**
- [ ] Implement GameViewModel with complete game logic
- [ ] Create scoring and attempt management system
- [ ] Add word difficulty progression
- [ ] Implement hint system with API integration
- [ ] Create timer functionality
- [ ] Add game state persistence
- [ ] Handle win/lose conditions

### Phase 5: API Integration

**Tasks:**
- [ ] Set up API Ninjas integration (Random Word, Rhyme, Thesaurus)
- [ ] Implement Dreamlo leaderboard integration
- [ ] Add proper error handling and retry logic
- [ ] Implement offline fallback mechanisms
- [ ] Add API key management
- [ ] Test all API endpoints

### Phase 6: Error Handling & Edge Cases

**Tasks:**
- [ ] Network error handling with user feedback
- [ ] Input validation for all user inputs
- [ ] App lifecycle state management
- [ ] Concurrent operation handling
- [ ] API timeout and retry mechanisms
- [ ] Graceful degradation for failed services

### Phase 7: UI/UX Polish & Testing

**Tasks:**
- [ ] Apply Material Design principles
- [ ] Implement responsive layouts
- [ ] Add animations and transitions
- [ ] Create loading and error states
- [ ] Test on multiple screen sizes
- [ ] Add accessibility features
- [ ] Implement dark/light theme support

### Phase 8: Testing & Quality Assurance

**Tasks:**
- [ ] Write unit tests for ViewModels and UseCases
- [ ] Create integration tests for repositories
- [ ] Add UI tests for critical user flows
- [ ] Test offline/online scenarios
- [ ] Validate on multiple devices and Android versions
- [ ] Performance testing and optimization

---

## 🔑 API Configuration Setup

### API Ninjas Setup

1. Sign up at [API Ninjas](https://api.ninjas.com/)
2. Get API key from dashboard
3. Add to `local.properties`:
   ```
   API_NINJAS_KEY="your_api_key_here"
   ```
4. Configure in `build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_NINJAS_KEY", "\"${project.findProperty("API_NINJAS_KEY")}\"")
   ```

### Dreamlo Setup

1. Create free leaderboard at [Dreamlo](https://dreamlo.com/)
2. Get public and private keys
3. Add to `local.properties`:
   ```
   DREAMLO_PUBLIC_KEY="your_public_key"
   DREAMLO_PRIVATE_KEY="your_private_key"  
   ```

---

## 📊 Development Timeline

### Week 1: Foundation
- [ ] Project setup and dependencies
- [ ] Basic UI structure  
- [ ] API configuration

### Week 2: Core Features
- [ ] Onboarding system
- [ ] Basic game mechanics
- [ ] API integrations

### Week 3: Advanced Features  
- [ ] Hint system
- [ ] Timer implementation
- [ ] Leaderboard functionality

### Week 4: Polish & Testing
- [ ] UI/UX improvements
- [ ] Error handling
- [ ] Testing and debugging
- [ ] Final submission preparation

---

## 🎯 Success Criteria

### Functionality (65 marks)
- [ ] All 7 core features working correctly
- [ ] Proper API integration
- [ ] Correct scoring and game logic
- [ ] Functional leaderboard system

### Design (20 marks)
- [ ] Intuitive user interface
- [ ] Material Design compliance
- [ ] Responsive layouts
- [ ] Smooth user experience

### Code Quality (5 marks)
- [ ] Clean, well-commented code
- [ ] Proper error handling
- [ ] Following Android best practices
- [ ] MVVM architecture implementation

---

## 📞 Next Steps

1. **Immediate Actions:**
   - [ ] Set up API accounts and get keys
   - [ ] Update project dependencies
   - [ ] Create basic project structure
   - [ ] Start with onboarding feature

2. **Development Order:**
   - Start with onboarding (simplest feature)
   - Implement core game mechanics
   - Add API integrations
   - Implement advanced features
   - Polish UI and add error handling

**Last Updated**: October 2, 2025  
**Status**: Ready for Implementation