package co.statu.rule.token.deserializer

import co.statu.rule.token.TokenPlugin
import co.statu.rule.token.type.TokenType
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class TokenTypeDeserializer : JsonDeserializer<TokenType> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TokenType {
        val tokenTypeName = json.asString

        return TokenPlugin.tokenTypes.find { it.getTokenName() == tokenTypeName }!!
    }
}