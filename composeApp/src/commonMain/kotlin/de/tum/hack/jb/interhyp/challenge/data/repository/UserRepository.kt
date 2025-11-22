package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.domain.model.User
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

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
}

/**
 * Implementation of UserRepository using DataStore for persistence.
 * Note: Actual DataStore implementation will be added in the next step.
 */
class UserRepositoryImpl : UserRepository {
    
    // TODO: Implement with DataStore or SQLDelight
    private var cachedUser: User? = null
    
    override suspend fun saveUser(user: User) {
        cachedUser = user
    }
    
    override fun getUser(): Flow<User?> {
        return kotlinx.coroutines.flow.flow {
            emit(cachedUser)
        }
    }
    
    override suspend fun updateWealth(userId: String, newWealth: Double) {
        cachedUser = cachedUser?.copy(wealth = newWealth)
    }
    
    override suspend fun clearUser() {
        cachedUser = null
    }
    
    override suspend fun hasUser(): Boolean {
        return cachedUser != null
    }
}
