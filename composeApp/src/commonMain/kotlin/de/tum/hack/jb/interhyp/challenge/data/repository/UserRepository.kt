package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.domain.model.User
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository interface for user-related operations.
 */
interface UserRepository {
    /**
     * Save user profile data
     */
    suspend fun saveUser(user: User)
    
    /**
     * Get current user profile
     */
    fun getUser(): Flow<User?>
    
    /**
     * Update user wealth/savings
     */
    suspend fun updateWealth(userId: String, newWealth: Double)
    
    /**
     * Clear user data
     */
    suspend fun clearUser()
    
    /**
     * Check if user exists
     */
    suspend fun hasUser(): Boolean
    
    /**
     * Save partial user profile (for intermediate onboarding saves)
     */
    suspend fun savePartialProfile(profile: UserProfile)
    
    /**
     * Get partial user profile (for resuming onboarding)
     */
    fun getPartialProfile(): Flow<UserProfile?>
}

/**
 * Simple storage interface for platform-specific persistence
 */
interface SimpleStorage {
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}

/**
 * In-memory implementation of SimpleStorage (fallback for platforms without persistent storage)
 */
class InMemoryStorage : SimpleStorage {
    private val storage = mutableMapOf<String, String>()
    
    override suspend fun saveString(key: String, value: String) {
        storage[key] = value
    }
    
    override suspend fun getString(key: String): String? {
        return storage[key]
    }
    
    override suspend fun remove(key: String) {
        storage.remove(key)
    }
}

/**
 * Implementation of UserRepository with persistent storage using SimpleStorage
 */
class UserRepositoryImpl(
    private val storage: SimpleStorage = InMemoryStorage()
) : UserRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    private val _userFlow = MutableStateFlow<User?>(null)
    private val _partialProfileFlow = MutableStateFlow<UserProfile?>(null)
    
    private var isLoaded = false
    
    companion object {
        private const val USER_KEY = "user_profile"
        private const val PARTIAL_PROFILE_KEY = "partial_user_profile"
    }
    
    private suspend fun ensureLoaded() {
        if (!isLoaded) {
            isLoaded = true
            loadUserFromStorage()
            loadPartialProfileFromStorage()
        }
    }
    
    private suspend fun loadUserFromStorage() {
        try {
            val userJson = storage.getString(USER_KEY)
            if (userJson != null) {
                val user = json.decodeFromString<User>(userJson)
                _userFlow.value = user
            }
        } catch (e: Exception) {
            println("Failed to load user from storage: ${e.message}")
        }
    }
    
    private suspend fun loadPartialProfileFromStorage() {
        try {
            val profileJson = storage.getString(PARTIAL_PROFILE_KEY)
            if (profileJson != null) {
                val profile = json.decodeFromString<UserProfile>(profileJson)
                _partialProfileFlow.value = profile
            }
        } catch (e: Exception) {
            println("Failed to load partial profile from storage: ${e.message}")
        }
    }
    
    override suspend fun saveUser(user: User) {
        try {
            val userJson = json.encodeToString(user)
            storage.saveString(USER_KEY, userJson)
            _userFlow.value = user
        } catch (e: Exception) {
            println("Failed to save user: ${e.message}")
        }
    }
    
    override fun getUser(): Flow<User?> {
        return kotlinx.coroutines.flow.flow {
            ensureLoaded()
            _userFlow.collect { emit(it) }
        }
    }
    
    override suspend fun updateWealth(userId: String, newWealth: Double) {
        ensureLoaded()
        val currentUser = _userFlow.value
        if (currentUser != null && currentUser.id == userId) {
            val updatedUser = currentUser.copy(wealth = newWealth)
            saveUser(updatedUser)
        }
    }
    
    override suspend fun clearUser() {
        try {
            storage.remove(USER_KEY)
            storage.remove(PARTIAL_PROFILE_KEY)
            _userFlow.value = null
            _partialProfileFlow.value = null
        } catch (e: Exception) {
            println("Failed to clear user data: ${e.message}")
        }
    }
    
    override suspend fun hasUser(): Boolean {
        ensureLoaded()
        return _userFlow.value != null
    }
    
    override suspend fun savePartialProfile(profile: UserProfile) {
        try {
            val profileJson = json.encodeToString(profile)
            storage.saveString(PARTIAL_PROFILE_KEY, profileJson)
            _partialProfileFlow.value = profile
        } catch (e: Exception) {
            println("Failed to save partial profile: ${e.message}")
        }
    }
    
    override fun getPartialProfile(): Flow<UserProfile?> {
        return kotlinx.coroutines.flow.flow {
            ensureLoaded()
            _partialProfileFlow.collect { emit(it) }
        }
    }
}
