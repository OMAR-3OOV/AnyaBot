package commands.adminCategory

import utilities.commandsutility.Command
import utilities.commandsutility.CommandManager
import Main
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.categoryUtility.Categories
import utilities.staffUtility.Roles
import java.awt.Color
import java.lang.NullPointerException
import java.util.*
import java.util.stream.Collectors

class Help(private val bot: Main.Companion) : Command {

    override fun handle(args: List<String>, event: MessageReceivedEvent) {

        val embed = EmbedBuilder()
        val commandManager = CommandManager(bot)

        val commands: MutableMap<String, Command> = commandManager.commands

        try {
            if (args.isEmpty()) {
                val description: StringBuilder = embed.descriptionBuilder

                embed.setTitle("⚙ Help assistant!")

                /**
                 *  it's really simple format for codes which is per each 3 values in the map.
                 *  it will be a "\n" so every line going to have 3 values only
                 *
                 *  it sorted like this: [k1, k2, k3] , [k4, k5, k6], [k7,k8] it will keep sorting as that till the whole map sort
                 *
                 *  NOTE: map.chunked() is only available to kotlin!
                 *  in Java it might be a little different you have to make a simple math.
                 *  Using AtomicInteger is the easiest way I find out!
                 *
                 *  @code
                 *  AtomicInteger counter = new AtomicInteger(0);
                 *
                 *  map.keySet().stream().map(map -> ((counter.getAndAdd(1) % 3 == 0)? "\n":"/") + map).collect(collectors.joining(""));
                 */

                commandManager.commandsByCategory.forEach { (category, commands) ->
                    description.append("${category.icon} **${category.displayName}**").append("\n").append(
                        commands.stream()
                            .collect(Collectors.toList()).chunked(4).stream()
                            .map { it.joinToString(" / ") { value -> "`${value.command}`" } }
                            .collect(Collectors.joining("\n"))
                    ).append("\n")
                }

                embed.setColor(Color(255, 170, 120))
                embed.setFooter("· Usage; r?help <command>")
            } else {
                if (Arrays.stream(Categories.values()).map { it.displayName.lowercase() }.toList().contains(args[0])) {
                    val description: StringBuilder = embed.descriptionBuilder
                    val info =
                        Categories.values().filter { checker -> checker.displayName.lowercase() == args[0] }.stream()
                            .findFirst().get()

                    embed.setTitle("⚙ ${info.displayName} (Category) :")

                    description.append("\uD83D\uDCD4 **Commands :**").append("\n> ")
                        .append(commandManager.commandsByCategory[info]!!.chunked(4).stream()
                            .map { it.joinToString ("/") { v -> "`${v.command}`" } }
                            .collect(Collectors.joining("\n> "))
                        ).append("\n \n")

                    description.append("\uD83D\uDCDC **Description:** ${info.description}")

                    embed.setFooter("· Usage; r?help <category>")
                } else if (commands.values.map { it.command.lowercase() }.toList().contains(args[0])) {
                    val info: Command =
                        commands.values.stream().filter() { checker -> checker.command.lowercase() == args[0] }
                            .findFirst().get()
                    val description: StringBuilder = embed.descriptionBuilder

                    embed.setTitle("${info.command} ( ${info.category.displayName} category ) :")
                    description.append("**Explaining for the command:**").append("\n")
                    description.append("> **Description:** ${info.description}").append("\n")
                    description.append("> **How to use:** ${info.help}").append("\n")
                    description.append("> **Status:**").append(" ")

                    if (info.isDisplay) {
                        description.append("Everyone can you this command")
                        embed.setColor(Color(0, 181, 13))
                    } else {
                        description.append("Staff only able to use this command")
                        embed.setColor(Color(255, 0, 0))
                    }

                    embed.setFooter("· Usage; r?help <command>")
                } else {
                    embed.setTitle(" ${args[0]} ??")
                    embed.setDescription("> This utilities.commandsutility.Command / Category is not existing on my system please use `r?help` to have more information")
                    embed.setColor(Color(255, 0, 0))
                }
            }
        } catch (error: NullPointerException) {
            embed.setTitle(" Something went wrong ( ${error.message} )")
            embed.setDescription("> Please report this issue to the developer, `Indra#4646`")
            embed.setColor(Color(255, 0, 0))
        }

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }
    override val help: String
        get() = "$command <command>"
    override val command: String
        get() = "help"
    override val category: Categories
        get() = Categories.INFORMATION
    override val roles: List<Roles>
        get() = Roles.values().toList()
    override val description: String
        get() = "i will assistant you with everything you need"
    override val isDisplay: Boolean
        get() = true
}