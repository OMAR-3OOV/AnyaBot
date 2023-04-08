package commands.adminCategory

import utilities.commandsutility.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.categoryUtility.Categories
import utilities.staffUtility.Roles

class Shutdown: Command {

    override fun handle(args: List<String>, event: MessageReceivedEvent) {
        if (args.isEmpty()){
            event.jda.shutdown()
        }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }

    override val help: String
        get() = "r?shutdown"
    override val command: String
        get() = "shutdown"
    override val category: Categories
        get() = Categories.MANAGEMENT
    override val roles: List<Roles>
        get() = arrayListOf(Roles.ADMIN)
    override val description: String
        get() = "this command to shutdown the bot!"
    override val isDisplay: Boolean
        get() = false
}