package me.abhigya.discordintegration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.bukkit.plugin.java.JavaPlugin
import org.zibble.discordmessenger.DiscordMessenger
import org.zibble.discordmessenger.components.action.RegisterWebhookClientAction
import org.zibble.discordmessenger.components.action.WebhookUrl
import java.io.File
import java.io.FileInputStream

class DiscordIntegration : JavaPlugin(), CoroutineScope by DiscordMessenger.instance.coroutineScope{

    override fun onEnable() {
        val file = File(dataFolder, "config.yml")
        if (!file.exists()) {
            saveResource("config.yml", false)
        }

        val config = Yaml.default.decodeFromStream<Config>(FileInputStream(file))

        runBlocking {
            DiscordMessenger.sendAction(RegisterWebhookClientAction.of(WebhookUrl.of(config.webhookUrl)))
        }
    }
}

@Serializable
data class Config(
    val webhookUrl: String
)