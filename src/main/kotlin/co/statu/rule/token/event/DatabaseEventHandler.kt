package co.statu.rule.token.event

import co.statu.rule.database.DatabaseManager
import co.statu.rule.database.event.DatabaseEventListener
import co.statu.rule.token.TokenPlugin
import co.statu.rule.token.deserializer.TokenTypeDeserializer
import co.statu.rule.token.type.TokenType
import com.google.gson.GsonBuilder

class DatabaseEventHandler : DatabaseEventListener {

    override suspend fun onReady(databaseManager: DatabaseManager) {
        databaseManager.initialize(TokenPlugin.INSTANCE, TokenPlugin.tables, TokenPlugin.migrations)

        TokenPlugin.databaseManager = databaseManager
    }

    override fun onGsonBuild(gsonBuilder: GsonBuilder) {
        gsonBuilder.registerTypeAdapter(TokenType::class.java, TokenTypeDeserializer())
    }
}