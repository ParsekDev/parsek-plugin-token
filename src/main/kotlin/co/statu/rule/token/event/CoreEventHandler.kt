package co.statu.rule.token.event

import co.statu.parsek.PluginEventManager
import co.statu.parsek.api.annotation.EventListener
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.parsek.api.event.CoreEventListener
import co.statu.parsek.config.ConfigManager
import co.statu.rule.database.DatabaseManager
import co.statu.rule.token.TokenConfig
import co.statu.rule.token.TokenPlugin
import co.statu.rule.token.provider.TokenProvider
import org.slf4j.Logger

@EventListener
class CoreEventHandler(
    private val logger: Logger,
    private val tokenPlugin: TokenPlugin
) : CoreEventListener {
    private val databaseManager by lazy {
        tokenPlugin.pluginBeanContext.getBean(DatabaseManager::class.java)
    }

    override suspend fun onConfigManagerReady(configManager: ConfigManager) {
        val pluginConfigManager = PluginConfigManager(
            tokenPlugin,
            TokenConfig::class.java,
        )
        tokenPlugin.pluginBeanContext.beanFactory.registerSingleton(
            pluginConfigManager.javaClass.name,
            pluginConfigManager
        )

        logger.info("Initialized plugin config")

        val tokenProvider = TokenProvider.create(databaseManager, pluginConfigManager)

        tokenPlugin.registerSingletonGlobal(tokenProvider)

        val tokenEventHandlers = PluginEventManager.getEventListeners<TokenEventListener>()

        tokenEventHandlers.forEach { it.registerTokenType(TokenPlugin.tokenTypes) }
    }
}