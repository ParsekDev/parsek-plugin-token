package co.statu.rule.token.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.rule.database.DatabaseManager
import co.statu.rule.database.event.DatabaseEventListener
import co.statu.rule.token.TokenPlugin
import co.statu.rule.token.deserializer.TokenTypeDeserializer
import co.statu.rule.token.type.TokenType
import com.google.gson.GsonBuilder

@EventListener
class DatabaseEventHandler(private val tokenPlugin: TokenPlugin) : DatabaseEventListener {
    override suspend fun onReady(databaseManager: DatabaseManager) {
        databaseManager.migrateNewPluginId("token", tokenPlugin.pluginId, tokenPlugin)
        databaseManager.initialize(tokenPlugin, tokenPlugin)
    }

    override fun onGsonBuild(gsonBuilder: GsonBuilder) {
        gsonBuilder.registerTypeAdapter(TokenType::class.java, TokenTypeDeserializer())
    }
}