package co.statu.rule.token.db.dao

import co.statu.rule.database.Dao
import co.statu.rule.token.db.model.Token
import co.statu.rule.token.type.TokenType
import io.vertx.jdbcclient.JDBCPool
import java.util.*

abstract class TokenDao : Dao<Token>(Token::class) {

    abstract suspend fun add(
        token: Token,
        jdbcPool: JDBCPool
    ): UUID

    abstract suspend fun isExistsByTokenAndType(
        token: String,
        tokenType: TokenType,
        jdbcPool: JDBCPool
    ): Boolean

    abstract suspend fun deleteByToken(token: String, jdbcPool: JDBCPool)

    abstract suspend fun deleteBySubject(subject: String, jdbcPool: JDBCPool)

    abstract suspend fun deleteBySubjectAndType(subject: String, type: TokenType, jdbcPool: JDBCPool)

    abstract suspend fun getByTokenSubjectAndType(
        token: String,
        subject: String,
        type: TokenType,
        jdbcPool: JDBCPool
    ): Token?

    abstract suspend fun getLastBySubjectAndType(
        subject: String,
        type: TokenType,
        jdbcPool: JDBCPool
    ): Token?

    abstract suspend fun deleteById(
        id: UUID,
        jdbcPool: JDBCPool
    )
}