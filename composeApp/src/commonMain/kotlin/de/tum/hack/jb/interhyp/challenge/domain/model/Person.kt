package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * Person entity representing a family member in the DreamBuilder app.
 */
@Serializable
data class Person(
    val id: String,
    val age: Int,
    val gender: Gender,
    val image: String? = null
)

/**
 * Gender enum for Person
 */
@Serializable
enum class Gender {
    MALE,
    FEMALE,
    OTHER
}
