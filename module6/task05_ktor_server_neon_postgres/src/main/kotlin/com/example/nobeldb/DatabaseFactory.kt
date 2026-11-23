package com.example.nobeldb

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object UsersTable : IntIdTable("users") {
    val username = varchar("username", 64).uniqueIndex()
    val passwordHash = varchar("password_hash", 128)
    val role = varchar("role", 32).default("user")
}

object PrizesTable : IntIdTable("prizes") {
    val awardYear = integer("award_year")
    val category = varchar("category", 64)
    val fullName = varchar("full_name", 255)
    val motivation = text("motivation")
    val detailLink = varchar("detail_link", 512)
}

object LaureatesTable : IntIdTable("laureates") {
    val prizeId = reference("prize_id", PrizesTable)
    val fullName = varchar("full_name", 255)
    val portion = varchar("portion", 16)
    val motivation = text("motivation")
    val portraitUrl = varchar("portrait_url", 512).nullable()
}

object UserPrizesTable : Table("user_prizes") {
    val userId = reference("user_id", UsersTable)
    val prizeId = reference("prize_id", PrizesTable)
    val addedAt = long("added_at")
    override val primaryKey = PrimaryKey(userId, prizeId)
}

object DatabaseFactory {
    fun init(
        jdbcUrl: String = System.getenv("NEON_DATABASE_URL")
            ?: "jdbc:postgresql://ep-example.eu-central-1.aws.neon.tech/neondb?sslmode=require",
        user: String = System.getenv("NEON_DB_USER") ?: "neondb_owner",
        pass: String = System.getenv("NEON_DB_PASS") ?: "password"
    ) {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.driverClassName = "org.postgresql.Driver"
            this.username = user
            this.password = pass
            this.maximumPoolSize = 5
            this.minimumIdle = 1
            this.idleTimeout = 300_000
            this.connectionTimeout = 30_000
        }
        Database.connect(HikariDataSource(config))
        transaction {
            SchemaUtils.create(UsersTable, PrizesTable, LaureatesTable, UserPrizesTable)
        }
    }
}
