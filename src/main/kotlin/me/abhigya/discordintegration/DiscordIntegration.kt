package me.abhigya.discordintegration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.plugin.java.JavaPlugin
import org.zibble.discordmessenger.DiscordMessenger
import org.zibble.discordmessenger.components.action.RegisterWebhookClientAction
import org.zibble.discordmessenger.components.action.SendMessageAction
import org.zibble.discordmessenger.components.action.WebhookUrl
import org.zibble.discordmessenger.components.readable.DiscordMessage
import java.io.File
import java.io.FileInputStream

@kr.entree.spigradle.Plugin
class DiscordIntegration : JavaPlugin(), CoroutineScope by DiscordMessenger.instance.coroutineScope {

    private lateinit var config: Config

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val file = File(dataFolder, "config.yml")
        if (!file.exists()) {
            saveResource("config.yml", false)
        }

        config = Yaml.default.decodeFromStream<Config>(FileInputStream(file))

        server.pluginManager.registerEvents(EventListener(this, config), this)

        runBlocking {
            DiscordMessenger.sendAction(RegisterWebhookClientAction.of(WebhookUrl.of(config.webhookUrl)))
            DiscordMessenger.sendAction(SendMessageAction.of(
                config.chatChannelId,
                DiscordMessage.builder()
                    .appendContent(":white_check_mark: **Server has started**")
                    .build()
            ))
        }
    }

    override fun onDisable() {
        runBlocking {
            DiscordMessenger.sendAction(SendMessageAction.of(
                config.chatChannelId,
                DiscordMessage.builder()
                    .appendContent(":octagonal_sign: **Server has stopped**")
                    .build()
            ))
        }
    }
}

@Serializable
data class Config(
    @SerialName("webhook-url") val webhookUrl: String,
    @SerialName("chat-channel-id") val chatChannelId: Long
)