package co.statu.rule.token.event

import co.statu.parsek.api.config.PluginConfigManager
import co.statu.parsek.api.event.ParsekEventListener
import co.statu.parsek.config.ConfigManager
import co.statu.rule.token.TokenConfig
import co.statu.rule.token.TokenPlugin
import co.statu.rule.token.TokenPlugin.Companion.databaseManager
import co.statu.rule.token.TokenPlugin.Companion.logger
import co.statu.rule.token.provider.TokenProvider

class ParsekEventHandler : ParsekEventListener {
    override suspend fun onConfigManagerReady(configManager: ConfigManager) {
        TokenPlugin.pluginConfigManager = PluginConfigManager(
            configManager,
            TokenPlugin.INSTANCE,
            TokenConfig::class.java,
            logger
        )

        logger.info("Initialized plugin config")

        TokenProvider.create(databaseManager, TokenPlugin.pluginConfigManager)

        val tokenEventHandlers = TokenPlugin.INSTANCE.context.pluginEventManager.getEventHandlers<TokenEventListener>()

        tokenEventHandlers.forEach { it.registerTokenType(TokenPlugin.tokenTypes) }
    }
}