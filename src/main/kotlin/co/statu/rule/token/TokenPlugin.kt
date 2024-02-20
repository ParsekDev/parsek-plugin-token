package co.statu.rule.token

import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.PluginContext
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.database.Dao
import co.statu.rule.database.DatabaseManager
import co.statu.rule.database.DatabaseMigration
import co.statu.rule.token.db.impl.TokenDaoImpl
import co.statu.rule.token.event.DatabaseEventHandler
import co.statu.rule.token.event.ParsekEventHandler
import co.statu.rule.token.type.TokenType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TokenPlugin(pluginContext: PluginContext) : ParsekPlugin(pluginContext) {
    companion object {
        internal val logger: Logger = LoggerFactory.getLogger(TokenPlugin::class.java)

        internal lateinit var pluginConfigManager: PluginConfigManager<TokenConfig>

        internal val tables by lazy {
            mutableListOf<Dao<*>>(
                TokenDaoImpl(),
            )
        }
        internal val migrations by lazy {
            listOf<DatabaseMigration>()
        }

        internal lateinit var INSTANCE: TokenPlugin

        internal lateinit var databaseManager: DatabaseManager

        internal val tokenTypes by lazy {
            mutableListOf<TokenType>()
        }
    }

    init {
        INSTANCE = this

        logger.info("Initialized instance")

        context.pluginEventManager.register(this, ParsekEventHandler())
        context.pluginEventManager.register(this, DatabaseEventHandler())

        logger.info("Registered events")
    }
}
