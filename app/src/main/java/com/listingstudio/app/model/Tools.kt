package com.listingstudio.app.model

/** Background styles the cutout can be placed on. All rendered locally, for free. */
enum class Background(val label: String) {
    WHITE("White Background"),
    STUDIO("Studio Background")
}

/** Marketplace export presets. Square output at the given pixel size on a white canvas. */
enum class Marketplace(val label: String, val size: Int) {
    EBAY("eBay (1600px)", 1600),
    AMAZON("Amazon (2000px)", 2000),
    ETSY("Etsy (2000px)", 2000),
    RESALE("Poshmark / Depop / Mercari (1200px)", 1200)
}
