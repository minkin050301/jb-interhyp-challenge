package de.tum.hack.jb.interhyp.challenge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform