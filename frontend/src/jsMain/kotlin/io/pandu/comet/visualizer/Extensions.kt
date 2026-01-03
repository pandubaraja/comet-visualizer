package io.pandu.comet.visualizer

fun Double.format(decimals: Int): String {
    return this.asDynamic().toFixed(decimals) as String
}