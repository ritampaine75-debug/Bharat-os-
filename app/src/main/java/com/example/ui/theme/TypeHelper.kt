package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import com.example.data.model.FontChoice

object TypeHelper {

    val PoppinsFontFamily = FontFamily(
        Font(R.font.poppins)
    )

    val CinzelFontFamily = FontFamily(
        Font(R.font.cinzel)
    )

    val RobotoMonoFontFamily = FontFamily(
        Font(R.font.roboto_mono)
    )

    fun getFontFamily(choice: FontChoice): FontFamily {
        return when (choice) {
            FontChoice.MODERN_POPPINS -> PoppinsFontFamily
            FontChoice.ELEGANT_CINZEL -> CinzelFontFamily
            FontChoice.DIGITAL_MONO -> RobotoMonoFontFamily
            FontChoice.SYSTEM_SANS -> FontFamily.SansSerif
        }
    }
}
