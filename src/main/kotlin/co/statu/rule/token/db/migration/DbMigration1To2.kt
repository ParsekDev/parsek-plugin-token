package co.statu.rule.token.db.migration

import co.statu.rule.database.DatabaseMigration
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.kotlin.coroutines.*

class DbMigration1To2(
    override val FROM_SCHEME_VERSION: Int = 1,
    override val SCHEME_VERSION: Int = 2,
    override val SCHEME_VERSION_INFO: String = "Add additionalClaims column to token table"
) : DatabaseMigration() {
    override val handlers: List<suspend (jdbcPool: Pool, tablePrefix: String) -> Unit> = listOf(
        addAdditionalClaimsColumnToTokenTable(),
    )

    private fun addAdditionalClaimsColumnToTokenTable(): suspend (jdbcPool: Pool, tablePrefix: String) -> Unit =
        { jdbcPool: Pool, tablePrefix: String ->
            jdbcPool
                .query("ALTER TABLE `${tablePrefix}token` ADD COLUMN `additionalClaims` String DEFAULT '{}';")
                .execute()
                .coAwait()
        }
}