package co.statu.rule.token.db.impl

import co.statu.parsek.api.ParsekPlugin
import co.statu.rule.token.db.dao.TokenDao
import co.statu.rule.token.db.model.Token
import co.statu.rule.token.type.TokenType
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import java.util.*

class TokenDaoImpl : TokenDao() {

    override suspend fun init(jdbcPool: JDBCPool, plugin: ParsekPlugin) {
        jdbcPool
            .query(
                """
                        CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                            `id` UUID NOT NULL,
                            `subject` String NOT NULL,
                            `token` String NOT NULL,
                            `type` String NOT NULL,
                            `expireDate` Int64 NOT NULL,
                            `startDate` Int64 NOT NULL
                        ) ENGINE = MergeTree() order by `expireDate`;
                        """
            )
            .execute()
            .await()
    }

    override suspend fun add(token: Token, jdbcPool: JDBCPool): UUID {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (${fields.toTableQuery()}) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    token.id,
                    token.subject,
                    token.token,
                    token.type.getTokenName(),
                    token.expireDate,
                    token.startDate
                )
            )
            .await()

        return token.id
    }

    override suspend fun isExistsByTokenAndType(
        token: String,
        tokenType: TokenType,
        jdbcPool: JDBCPool
    ): Boolean {
        val query = "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` WHERE `token` = ? AND `type` = ?"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(Tuple.of(token, tokenType.getTokenName()))
            .await()

        return rows.toList()[0].getLong(0) == 1L
    }

    override suspend fun deleteByToken(token: String, jdbcPool: JDBCPool) {
        val query =
            "DELETE from `${getTablePrefix() + tableName}` WHERE `token` = ?"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(token)
            )
            .await()
    }

    override suspend fun deleteBySubject(subject: String, jdbcPool: JDBCPool) {
        val query =
            "DELETE from `${getTablePrefix() + tableName}` WHERE `subject` = ?"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(subject)
            )
            .await()
    }

    override suspend fun deleteBySubjectAndType(subject: String, type: TokenType, jdbcPool: JDBCPool) {
        val query =
            "DELETE from `${getTablePrefix() + tableName}` WHERE `subject` = ? AND `type` = ?"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(subject, type.getTokenName())
            )
            .await()
    }

    override suspend fun getByTokenSubjectAndType(
        token: String,
        subject: String,
        type: TokenType,
        jdbcPool: JDBCPool
    ): Token? {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `token` = ? AND `subject` = ? AND `type` = ?"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    token,
                    subject,
                    type.getTokenName()
                )
            )
            .await()

        if (rows.size() == 0) {
            return null
        }

        val row = rows.toList()[0]

        return row.toEntity()
    }

    override suspend fun getLastBySubjectAndType(
        subject: String,
        type: TokenType,
        jdbcPool: JDBCPool
    ): Token? {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `subject` = ? AND `type` = ? order by `expireDate` DESC limit 1"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    subject,
                    type.getTokenName()
                )
            )
            .await()

        if (rows.size() == 0) {
            return null
        }

        val row = rows.toList()[0]

        return row.toEntity()
    }

    override suspend fun deleteById(
        id: UUID,
        jdbcPool: JDBCPool
    ) {
        val query =
            "DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        jdbcPool
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .await()
    }
}