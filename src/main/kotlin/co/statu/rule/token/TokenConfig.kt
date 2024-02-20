package co.statu.rule.token

import co.statu.parsek.api.config.PluginConfig
import co.statu.parsek.util.KeyGeneratorUtil

data class TokenConfig(
    val secretKey: String = KeyGeneratorUtil.generateSecretKey(),
) : PluginConfig()