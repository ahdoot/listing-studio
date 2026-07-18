package com.listingstudio.app.model

/**
 * The AI tools the app exposes. Each carries the natural-language instruction
 * sent to Gemini's image model to perform the edit.
 */
enum class AiTool(val label: String, val prompt: String) {
    REMOVE_BG(
        "White Background",
        "Remove the background completely and replace it with a solid, pure white (#FFFFFF) " +
            "background. Keep the product perfectly intact with clean, sharp, natural edges " +
            "(including fine details like hair, straps, or transparent parts). Center the product " +
            "and output a clean, professional e-commerce product photo. Do not add any text, logos, " +
            "or watermarks."
    ),
    STUDIO_BG(
        "Studio Background",
        "Place this product on a clean, professional studio background suitable for an e-commerce " +
            "listing, with soft, realistic lighting and a subtle gradient. Keep the product itself " +
            "unchanged with sharp edges. No text, logos, or watermarks."
    ),
    SHADOW(
        "Add Shadow",
        "Keeping the product exactly as-is on a pure white background, add a soft, realistic drop " +
            "shadow beneath it to give natural depth, as in professional product photography. " +
            "Do not alter the product itself."
    ),
    WRINKLE(
        "Remove Wrinkles",
        "Remove all wrinkles, creases, and folds from the clothing or fabric in this image so it " +
            "looks freshly ironed and smooth. Preserve the exact colors, patterns, texture, shape, " +
            "and proportions of the garment. Do not change anything else."
    ),
    GHOST_MANNEQUIN(
        "Ghost Mannequin",
        "Transform this clothing photo into a professional ghost-mannequin (invisible mannequin) " +
            "product image: show the garment as if worn by an invisible person, with a natural hollow " +
            "neckline and shape, on a pure white (#FFFFFF) background. Remove any visible mannequin, " +
            "hanger, model, or background. Keep colors and fabric detail accurate."
    ),
    UPSCALE(
        "Enhance / Upscale",
        "Enhance this product photo: increase sharpness and clarity, clean up noise, and improve " +
            "lighting to look like a crisp, high-resolution professional listing image. Keep the " +
            "product, colors, and composition unchanged."
    )
}

/** Marketplace export presets. Square output at the given pixel size on a white canvas. */
enum class Marketplace(val label: String, val size: Int) {
    EBAY("eBay (1600px)", 1600),
    AMAZON("Amazon (2000px)", 2000),
    ETSY("Etsy (2000px)", 2000),
    RESALE("Poshmark / Depop / Mercari (1200px)", 1200)
}
