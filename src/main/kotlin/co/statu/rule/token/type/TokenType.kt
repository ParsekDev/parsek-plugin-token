package co.statu.rule.token.type

import co.statu.parsek.util.TextUtil.convertToSnakeCase

interface TokenType {
    fun getTokenName() = javaClass.simpleName.replace("Token", "").convertToSnakeCase().uppercase()

    fun getExpireDateFromNow(): Long
}