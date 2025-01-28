package org.mantis.muse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform