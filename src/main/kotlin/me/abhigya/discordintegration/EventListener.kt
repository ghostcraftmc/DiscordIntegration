package me.abhigya.discordintegration

import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
import org.bukkit.advancement.Advancement
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.zibble.discordmessenger.DiscordMessenger
import org.zibble.discordmessenger.components.action.SendMessageAction
import org.zibble.discordmessenger.components.action.SendWebhookMessageAction
import org.zibble.discordmessenger.components.action.WebhookUrl
import org.zibble.discordmessenger.components.readable.DiscordEmbed
import org.zibble.discordmessenger.components.readable.DiscordMessage
import org.zibble.discordmessenger.components.readable.WebhookMessage
import java.awt.Color

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
        val msg = DiscordEmbed.builder()
            .color(Color.green)
            .author(
                DiscordEmbed.EmbedAuthor(
                    "${player.name} joined the server",
                    "https://minotar.net/avatar/${player.name}/100.png",
                    null
                )
            )
            .build()


        discordIntegration.launch {
            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, DiscordMessage.embeds(msg)))
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerQuitEvent.handlePlayerQuit() {
        val msg = DiscordEmbed.builder()
            .author(
                DiscordEmbed.EmbedAuthor(
                    "${player.name} left the server",
                    "https://minotar.net/avatar/${player.name}/100.png",
                    null
                )
            )
            .color(Color.red)
            .build()

        discordIntegration.launch {
            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, DiscordMessage.embeds(msg)))
        }

    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerDeathEvent.handlePlayerDeath() {
        val msg = DiscordEmbed.builder()
            .author(
                DiscordEmbed.EmbedAuthor("$deathMessage","https://minotar.net/avatar/${entity.name}/100.png",null)
            )
            .color(Color.black)
            .build()

        discordIntegration.launch {
            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, DiscordMessage.embeds(msg)))
        }

    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerAdvancementDoneEvent.handlePlayerAdvancement() {
        val advancement = advancement
        val player = player

        discordIntegration.launch {
            val display = advancement.display ?: return@launch

              val msg = DiscordEmbed.builder()
                  .author(
                      DiscordEmbed.EmbedAuthor("${player.name} has made an advancement $display","https://minotar.net/avatar/${player.name}/100.png",null)
                  )
                  .color(Color.yellow)
                  .build()

            DiscordMessenger.sendAction(SendMessageAction.of(config.chatChannelId, DiscordMessage.embeds(msg)))
        }
    }

}

private val ADVANCEMENT_TITLES: MutableMap<NamespacedKey, String?> = mutableMapOf()

val Advancement.display: String?
    get() {
        if (ADVANCEMENT_TITLES.containsKey(key)) {
            return ADVANCEMENT_TITLES[key]
        }

        val handle = javaClass.getDeclaredField("handle").run {
            isAccessible = true
            get(this@display)
        }
        val display = handle.javaClass.declaredFields
            .firstOrNull { it.type.simpleName == "AdvancementDisplay" }
            ?.also { it.isAccessible = true }
            ?.get(handle) ?: run {
                ADVANCEMENT_TITLES[key] = null
                return null
        }

        val json = display.javaClass.getDeclaredMethod("k").invoke(display) as JsonObject

        val shouldDisplay = json["announce_to_chat"].asBoolean

        if (!shouldDisplay) {
            ADVANCEMENT_TITLES[key] = null
            return null
        }

        val title = json["title"]

        return PlainTextComponentSerializer.plainText().serialize(GsonComponentSerializer.gson().deserializeFromTree(title)).also {
            ADVANCEMENT_TITLES[key] = it
        }

//        return runCatching {
//            return@runCatching title.javaClass.getDeclaredMethod("getString").run {
//                isAccessible = true
//                invoke(title) as String
//            }
//        }.recoverCatching {
//            return@recoverCatching title.javaClass.getDeclaredMethod("getText").run {
//                isAccessible = true
//                val s = invoke(title) as String
//                s.ifBlank {
//                    val serializer = title.javaClass.declaredClasses.first { it.simpleName == "ChatSerializer" }
//                    val json = serializer.getDeclaredMethod("a", title.javaClass).invoke(null, title) as String
//                    PlainTextComponentSerializer.plainText().serialize(GsonComponentSerializer.gson().deserialize(json))
//                }
//            }
//        }.getOrElse {
//            val key = key.key
//            key.substring(key.lastIndexOf("/") + 1).lowercase().split("_")
//                .joinToString { it.substring(0, 1).uppercase() + it.substring(1) }
//        }.also {
//            ADVANCEMENT_TITLES[key] = it
//        }
    }