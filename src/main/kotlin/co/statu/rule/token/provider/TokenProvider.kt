package co.statu.rule.token.provider

import co.statu.parsek.PluginEventManager
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.database.DatabaseManager
import co.statu.rule.token.TokenConfig
import co.statu.rule.token.db.dao.TokenDao
import co.statu.rule.token.db.impl.TokenDaoImpl
import co.statu.rule.token.db.model.Token
import co.statu.rule.token.event.TokenEventListener
import co.statu.rule.token.type.TokenType
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.vertx.core.json.JsonObject
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import java.util.*

class TokenProvider private constructor(
    private val databaseManager: DatabaseManager,
    private val pluginConfigManager: PluginConfigManager<TokenConfig>
) {
    internal companion object {
        suspend fun create(
            databaseManager: DatabaseManager,
            pluginConfigManager: PluginConfigManager<TokenConfig>
        ): TokenProvider {
            val tokenProvider = TokenProvider(databaseManager, pluginConfigManager)

            val handlers = PluginEventManager.getEventListeners<TokenEventListener>()

            handlers.forEach {
                it.onReady(tokenProvider)
            }

            return tokenProvider
        }
    }

    private val tokenDao: TokenDao by lazy {
        TokenDaoImpl()
    }

    fun getAlgorithm(): Algorithm {
        val secretKey = String(Base64.getDecoder().decode(pluginConfigManager.config.secretKey))

        return Algorithm.HMAC512(secretKey)
    }

    fun generateToken(subject: String, tokenType: TokenType, claims: Map<String, Any> = mapOf()): Pair<String, Long> {
        val expireDate = tokenType.getExpireDateFromNow()

        val tokenBuilder = JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withSubject(subject)
            .withClaim("tokenType", tokenType.getTokenName())
            .withExpiresAt(Date(expireDate))

        claims.forEach {
            when (it.value) {
                is Boolean -> {
                    tokenBuilder.withClaim(it.key, it.value as Boolean)
                }

                is String -> {
                    tokenBuilder.withClaim(it.key, it.value as String)
                }

                is Int -> {
                    tokenBuilder.withClaim(it.key, it.value as Int)
                }

                is Long -> {
                    tokenBuilder.withClaim(it.key, it.value as Long)
                }

                is Double -> {
                    tokenBuilder.withClaim(it.key, it.value as Double)
                }

                is Map<*, *> -> {
                    tokenBuilder.withClaim(it.key, it.value as Map<String, *>)
                }

                is List<*> -> {
                    tokenBuilder.withClaim(it.key, it.value as List<*>)
                }
            }
        }

        val token = tokenBuilder
            .sign(getAlgorithm())

        return Pair(token, expireDate)
    }

    suspend fun saveToken(
        token: String,
        subject: String,
        tokenType: TokenType,
        expireDate: Long,
        additionalClaims: JsonObject = JsonObject(),
        jdbcPool: Pool
    ): UUID {
        val tokenObject = Token(
            subject = subject,
            token = token,
            type = tokenType,
            expireDate = expireDate,
            additionalClaims = additionalClaims
        )

        return tokenDao.add(tokenObject, jdbcPool)
    }

    suspend fun isTokenValid(token: String, tokenType: TokenType): Boolean {
        try {
            parseToken(token)
        } catch (exception: Exception) {
            return false
        }

        val jdbcPool = databaseManager.getConnectionPool()

        return tokenDao.isExistsByTokenAndType(token, tokenType, jdbcPool)
    }

    suspend fun validateToken(token: String): Boolean {
        try {
            parseToken(token)
        } catch (exception: Exception) {
            return false
        }

        return true
    }

    suspend fun invalidateToken(token: String) {
        val jdbcPool = databaseManager.getConnectionPool()

        tokenDao.deleteByToken(token, jdbcPool)
    }

    suspend fun invalidateTokensBySubjectAndType(subject: String, type: TokenType, jdbcPool: Pool) {
        tokenDao.deleteBySubjectAndType(subject, type, jdbcPool)
    }

    fun parseToken(token: String): DecodedJWT {
        val verifier = JWT.require(getAlgorithm())
            .build()

        return verifier.verify(token)
    }

    suspend fun invalidateByTokenId(id: UUID, jdbcPool: Pool) {
        tokenDao.deleteById(id, jdbcPool)
    }

    suspend fun getByTokenSubjectAndType(
        token: String,
        subject: String,
        type: TokenType,
        jdbcPool: Pool
    ) = tokenDao.getByTokenSubjectAndType(
        token,
        subject,
        type,
        jdbcPool
    )

    suspend fun getByTokenAndSubject(
        token: String,
        subject: String,
        jdbcPool: Pool
    ) = tokenDao.getByTokenAndSubject(
        token,
        subject,
        jdbcPool
    )

    suspend fun getAllBySubjectAndType(
        subject: String,
        type: TokenType,
        jdbcPool: Pool
    ) = tokenDao.getAllBySubjectAndType(
        subject,
        type,
        jdbcPool
    )

    suspend fun getByToken(
        token: String,
        jdbcPool: Pool
    ) = tokenDao.getByToken(
        token,
        jdbcPool
    )
}