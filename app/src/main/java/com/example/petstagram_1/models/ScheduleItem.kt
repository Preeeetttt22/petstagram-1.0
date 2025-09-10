package com.example.petstagram_1.models // Or your preferred package

import com.example.petstagram_1.models.Appointment

/**
 * Holds the combined information for an appointment, including details
 * fetched from other collections like pet and owner names.
 */
data class ScheduleItem(
    val appointment: Appointment,
    val petName: String,
    val ownerName: String
)
