package org.example.nosql_lab1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "user_settings")
class UserSettings(
    @field:Id
    @field:Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID? = null,

    @field:Column(name = "notifications_active", nullable = false)
    var notificationsActive: Boolean = true,

    @field:Column(nullable = false, length = 16)
    var language: String = "en",

    @field:Column(name = "preferred_category", length = 100)
    var preferredCategory: String? = null,
)
