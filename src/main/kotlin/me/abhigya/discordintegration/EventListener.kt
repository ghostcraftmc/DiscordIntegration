package me.abhigya.discordintegration

import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.zibble.discordmessenger.DiscordMessenger
import org.zibble.discordmessenger.components.action.SendMessageAction
import org.zibble.discordmessenger.components.action.SendWebhookMessageAction
import org.zibble.discordmessenger.components.action.WebhookUrl
import org.zibble.discordmessenger.components.readable.DiscordMessage
import org.zibble.discordmessenger.components.readable.WebhookMessage

class EventListener(
    private val discordIntegration: DiscordIntegration,
    private val config: Config
) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun AsyncPlayerChatEvent.handlePlayerChat() {
        val msg = WebhookMessage.builder()
            .username(player.displayName)
            .avatarUrl("https://minotar.net/avatar/${player.name}/100.png")
            .content(message)
            .build()

        discordIntegration.launch {
            DiscordMessenger.sendAction(SendWebhookMessageAction.of(WebhookUrl.of(config.webhookUrl), msg))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerJoinEvent.handlePlayerJoin() {
        val msg = DiscordMessage.builder()
            .appendContent("${player.name} joined the server")
            .build()

        discordIntegration.launch {
            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, msg))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerQuitEvent.handlePlayerQuit() {
        val msg = DiscordMessage.builder()
            .appendContent("${player.name} left the server")
            .build()

        discordIntegration.launch {
            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, msg))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerAdvancementDoneEvent.handlePlayerAdvancement() {
        val msg = DiscordMessage.builder()
            .appendContent("${player.name} has made an advancement $advancement")
            .build()

        discordIntegration.launch {
            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, msg))

        }
    }


}