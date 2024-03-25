package co.statu.rule.token.event

import co.statu.parsek.api.event.PluginEventListener
import co.statu.rule.token.provider.TokenProvider
import co.statu.rule.token.type.TokenType

interface TokenEventListener : PluginEventListener {
    suspend fun onReady(tokenProvider: TokenProvider)

    fun registerTokenType(tokenTypeList: MutableList<TokenType>) {}
}