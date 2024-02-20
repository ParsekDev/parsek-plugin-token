package co.statu.rule.token.event

import co.statu.parsek.api.PluginEvent
import co.statu.rule.token.provider.TokenProvider
import co.statu.rule.token.type.TokenType

interface TokenEventListener : PluginEvent {
    suspend fun onReady(tokenProvider: TokenProvider)

    fun registerTokenType(tokenTypeList: MutableList<TokenType>) {}
}