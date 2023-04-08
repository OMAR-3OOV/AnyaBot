package utilities.commandsutility

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.categoryUtility.Categories
import utilities.staffUtility.Roles
import java.io.FileNotFoundException

interface Command {
    @Throws(FileNotFoundException::class)
    fun handle(args: List<String>, event: MessageReceivedEvent)
    fun onSlashCommand(event: SlashCommandInteractionEvent)

    val help: String

    val command: String

    val category: Categories

    val roles: List<Roles>

    val description: String

    val isDisplay: Boolean
}