package co.statu.rule.token.event

import co.statu.parsek.PluginEventManager
import co.statu.parsek.api.annotation.EventListener
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.database.DatabaseManager
import co.statu.rule.database.event.DatabaseEventListener
import co.statu.rule.token.TokenConfig
import co.statu.rule.token.TokenPlugin
import co.statu.rule.token.deserializer.TokenTypeDeserializer
import co.statu.rule.token.provider.TokenProvider
import co.statu.rule.token.type.TokenType
import com.google.gson.GsonBuilder

@EventListener
class DatabaseEventHandler(private val tokenPlugin: TokenPlugin) : DatabaseEventListener {
    private val pluginConfigManager by lazy {
        tokenPlugin.pluginBeanContext.getBean(PluginConfigManager::class.java) as PluginConfigManager<TokenConfig>
    }

    override suspend fun onReady(databaseManager: DatabaseManager) {
        databaseManager.migrateNewPluginId("token", tokenPlugin.pluginId, tokenPlugin)
        databaseManager.initialize(tokenPlugin, tokenPlugin)

        val tokenProvider = TokenProvider.create(databaseManager, pluginConfigManager)

        tokenPlugin.registerSingletonGlobal(tokenProvider)

        val tokenEventHandlers = PluginEventManager.getEventListeners<TokenEventListener>()

        tokenEventHandlers.forEach { it.registerTokenType(TokenPlugin.tokenTypes) }
    }

    override fun onGsonBuild(gsonBuilder: GsonBuilder) {
        gsonBuilder.registerTypeAdapter(TokenType::class.java, TokenTypeDeserializer())
    }
}