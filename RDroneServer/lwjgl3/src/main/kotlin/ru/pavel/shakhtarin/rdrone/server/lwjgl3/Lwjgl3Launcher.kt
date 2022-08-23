@file:JvmName("Lwjgl3Launcher")

package ru.pavel.shakhtarin.rdrone.server.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ru.pavel.shakhtarin.rdrone.server.graphic.level.World
import ru.pavel.shakhtarin.rdrone.server.RDroneServerKTXMain

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(RDroneServerKTXMain(World()), Lwjgl3ApplicationConfiguration().apply {
        setTitle("RDroneServer")
        setWindowedMode(1920, 1080)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
