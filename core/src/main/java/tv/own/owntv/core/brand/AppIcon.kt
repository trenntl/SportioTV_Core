package tv.own.owntv.core.brand

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.own.owntv.core.R

/**
 * Sportio's launcher mark: the green pitch tile with the play triangle. There is exactly one
 * launcher activity in the host app (`MainActivity`), one TV banner and one launch theme — the
 * icon colour is part of the brand and no longer user-configurable.
 *
 * Older installs may still have an `app_icon` value stored from the multi-colour era
 * (PETROL/SUNFLOWER/…/PIXEL); [fromStored] maps every such value to [SPORTIO] so the launcher
 * and the in-app logo converge on the Sportio mark without a data migration.
 */
enum class AppIcon(
    val activitySuffix: String,
    @StringRes val label: Int,
    /** Flat in-app mark, cropped to the card: the sidebar, lockups, About. */
    @DrawableRes val mark: Int,
    /** The simpler drawing for 32 dp and below. */
    @DrawableRes val markSmall: Int,
    /** 320×180 Android TV banner, also the logo of the TV home-screen channel. */
    @DrawableRes val banner: Int,
    /** ARGB of the wordmark's "Sport" on a dark surface (the brand guide's pitch green). */
    val accent: Long,
    /** ARGB of the wordmark's "Sport" on a light surface (the brand guide's deep green). */
    val accentOnLight: Long,
) {
    SPORTIO("", R.string.app_icon_sportio, R.drawable.sportio_mark, R.drawable.sportio_mark_small, R.drawable.sportio_banner, 0xFF4FEE94, 0xFF0E6B41);

    /**
     * Whether the drawing at this size is the pitch tile, whose play triangle sits exactly where
     * the in-app lockup draws it. Always true: the Sportio mark is one tile at every size.
     */
    fun isCard(small: Boolean): Boolean = true

    companion object {
        val DEFAULT = SPORTIO

        fun fromStored(value: String?): AppIcon = SPORTIO
    }
}
