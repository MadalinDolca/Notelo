package com.madalin.notelo.core.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Returns `true` if this [Date] is today, `false` otherwise.
 */
fun Date.isToday(): Boolean {
    val now = Date()
    return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
}

/**
 * Returns this [Date] formatted as `hh:mm a`.
 */
fun Date.asHourAndMinute(): String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(this)

/**
 * Returns this [Date] formatted as `dd MMM yy`.
 */
fun Date.asDate(): String = SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(this)