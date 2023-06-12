package me.abhigya.discordintegration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.zibble.discordmessenger.DiscordMessenger
import org.zibble.discordmessenger.components.action.readable.ChannelMessageAction
import org.zibble.discordmessenger.components.action.readable.DiscordMessageListener
import org.zibble.discordmessenger.components.action.sendable.SendMessageAction
import org.zibble.discordmessenger.components.entity.User
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
            DiscordMessenger.sendAction(SendMessageAction.of(
                config.chatChannelId,
                DiscordMessage.builder()
                    .appendContent(":white_check_mark: **Server has started**")
                    .build()
            ))
        }

        ChannelMessageAction.registerListener(DiscordMessageListener(config.chatChannelId))
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
    @SerialName("webhook-id") val webhookId: Int,
    @SerialName("chat-channel-id") val chatChannelId: Long
)

class DiscordMessageListener(
    private val channelId: Long
) : DiscordMessageListener {

    override suspend fun onDiscordMessage(channelId: Long, user: User, message: DiscordMessage) {
        if (channelId != this.channelId) return
        if (user.bot) return

        if (message.content.equals("list", true) || message.content.equals("playerlist", true)) {
            DiscordMessenger.sendAction(SendMessageAction.of(
                channelId,
                DiscordMessage.builder()
                    .appendContent("```\n")
                    .appendContent(Bukkit.getOnlinePlayers().joinToString { it.name })
                    .appendContent("\n```")
                    .build()
            ))
            return
        }

        val msg = buildString {
            append(ChatColor.translateAlternateColorCodes('&',"&9[Discord] "))
            append(ChatColor.WHITE)
            append(user.name)
            append(": ")
            append(ChatColor.GRAY)
            append(message.content)
        }

        Bukkit.broadcastMessage(msg)
    }

}