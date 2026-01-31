package co.statu.rule.token

import co.statu.parsek.PluginEventManager
import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.database.DatabaseManager
import co.statu.rule.token.event.TokenEventListener
import co.statu.rule.token.provider.TokenProvider
import co.statu.rule.token.type.TokenType
import org.springframework.beans.factory.getBean

class TokenPlugin : ParsekPlugin() {
    companion object {
        internal val tokenTypes by lazy {
            mutableListOf<TokenType>()
        }
    }

    override suspend fun onStart(){
        val configManager = PluginConfigManager(this, TokenConfig::class.java)

        pluginBeanContext.beanFactory.registerSingleton(PluginConfigManager::class.java.name, configManager)

        logger.info("Initialized plugin config")

        val databaseManager = pluginGlobalBeanContext.beanFactory.getBean<DatabaseManager>()

        databaseManager.migrateNewPluginId("token", this)
        databaseManager.initialize(this)

        val tokenProvider = TokenProvider.create(databaseManager, configManager)

        registerSingletonGlobal(tokenProvider)

        val tokenEventHandlers = PluginEventManager.getEventListeners<TokenEventListener>()

        tokenEventHandlers.forEach { it.registerTokenType(tokenTypes) }
    }
}
