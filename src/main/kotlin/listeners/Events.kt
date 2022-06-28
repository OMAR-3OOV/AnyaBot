package listeners

import CommandManager
import Main
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.io.FileNotFoundException

class Events(bot: Main.Companion): ListenerAdapter() {

    private val commandManager = CommandManager(bot)

    override fun onReady(event: ReadyEvent) {
        event.jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("This bot is working on kotlin language!"))

        Main.Logger().info("Hey master, Im totally ready & im currently in ${event.guildTotalCount} Guilds right now :3!")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channelType.isGuild) {
            val prefix = "r?"

            if (event.message.contentRaw.lowercase().contains(prefix)) {

                try {
                    commandManager.handleCommand(event, prefix)
                } catch (error: FileNotFoundException) {
                    error.printStackTrace()
                }
            }
        }

    }

}