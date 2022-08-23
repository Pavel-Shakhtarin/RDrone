package ru.pavel.shakhtarin.rdrone.server.graphic.level


import com.badlogic.gdx.graphics.Color
import java.util.*


enum class Colors(val color: Color){
    COLOR_ONE(Color.RED),
    COLOR_TWO(Color.FOREST),
    COLOR_THREE(Color.MAGENTA),
    COLOR_FOUR(Color.ORANGE),
    COLOR_FIVE(Color.LIME),
    COLOR_SIX(Color.NAVY),
    COLOR_SEVEN(Color.OLIVE),
    COLOR_EIGHT(Color.TEAL),
    COLOR_NINE(Color.SALMON),
    COLOR_TEN(Color.PURPLE),
    COLOR_ELEVEN(Color.PINK),
    COLOR_TWELVE(Color.VIOLET),
    COLOR_THIRTEEN(Color.GREEN),
    COLOR_FOURTEEN(Color.ROYAL),
    COLOR_FIFTEEN(Color.CHARTREUSE),
    COLOR_SIXTEEN(Color.MAROON)
}

fun colorQ(): LinkedList<Colors> {
    return LinkedList<Colors>().apply {
        add(Colors.COLOR_ONE)
        add(Colors.COLOR_TWO)
        add(Colors.COLOR_THREE)
        add(Colors.COLOR_FOUR)
        add(Colors.COLOR_FIVE)
        add(Colors.COLOR_SIX)
        add(Colors.COLOR_SEVEN)
        add(Colors.COLOR_EIGHT)
        add(Colors.COLOR_NINE)
        add(Colors.COLOR_TEN)
        add(Colors.COLOR_ELEVEN)
        add(Colors.COLOR_TWELVE)
        add(Colors.COLOR_THIRTEEN)
        add(Colors.COLOR_FOURTEEN)
        add(Colors.COLOR_FIFTEEN)
        add(Colors.COLOR_SIXTEEN)
    }
}