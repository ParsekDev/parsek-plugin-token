package co.statu.rule.token

import co.statu.parsek.api.ParsekPlugin
import co.statu.rule.database.Dao
import co.statu.rule.database.DatabaseMigration
import co.statu.rule.database.api.DatabaseHelper
import co.statu.rule.token.db.impl.TokenDaoImpl
import co.statu.rule.token.type.TokenType

class TokenPlugin : ParsekPlugin(), DatabaseHelper {

    override val tables: List<Dao<*>> = listOf(TokenDaoImpl())
    override val migrations: List<DatabaseMigration> = listOf()

    companion object {
        internal val tokenTypes by lazy {
            mutableListOf<TokenType>()
        }
    }
}
