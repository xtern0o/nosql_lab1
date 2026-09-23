package org.example.nosql_lab1.repository

import org.example.nosql_lab1.entity.UserSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserSettingsRepository : JpaRepository<UserSettings, UUID>
