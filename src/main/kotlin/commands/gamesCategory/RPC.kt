package commands.gamesCategory

import utilities.commandsutility.Command
import utilities.games.rpsutility.RPSUtility
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.categoryUtility.Categories
import utilities.staffUtility.Roles
import java.util.regex.Pattern
import java.util.stream.Collectors

class RPC : Command {

    override fun handle(args: List<String>, event: MessageReceivedEvent) {
        // codes
        val embed = EmbedBuilder()
        val user = event.author

        val gui: StringBuilder = StringBuilder()

        /**
         * The game gui
         *
         * ------------------
         * |................|
         * |....p1....p2....|
         * |................|
         * -----------------=
         *
         * it will be replacement string
         */

        gui.append("------------------").append("\n")
        gui.append("|................|").append("\n")
        gui.append("|....P1....P2....|").append("\n")
        gui.append("|................|").append("\n")
        gui.append("------------------").append("\n")

        // commands : r?rpc <opponent> <rounds>

        if (args.isNotEmpty()) {
            var opponent = args[0]

            if (opponent.equals("-r")) {
                val rps = RPSUtility(
                    user,
                    null,
                    event.guild,
                    event.channel.asTextChannel(),
                    embed
                )
                rps.isUnlimitedLoop = true
                return
            }

            val regex = Pattern.compile(Message.MentionType.USER.pattern.toString())
            val matcher = regex.matcher(opponent)

            opponent = if (matcher.find()) {
                opponent.replace("<", "").replace("!", "").replace("@", "").replace("#", "").replace("&", "")
                    .replace(">", "")
            } else {
                event.jda.getUsersByName(opponent, true).stream().map { m -> m.id }.collect(Collectors.joining())
            }

            val target = event.guild.retrieveMemberById(opponent).complete().user
            val rps = RPSUtility(
                user,
                target,
                event.guild,
                event.channel.asTextChannel(),
                embed
            )

            if (args.size > 1) {
                if (args[1] == "-r") {
                    rps.isUnlimitedLoop = true
                }
            }
        } else {
            RPSUtility(
                user,
                null,
                event.guild,
                event.channel.asTextChannel(),
                embed
            )
        }
    }

    override fun onSlashCommand(
        event: SlashCommandInteractionEvent
    ) {
        TODO("Not yet implemented")
    }


    override val help: String
        get() = String.format("\n```md\n# To play rpc with someone use r?${command} <user> / <user-id>\n# To play rpc with bot use r?${command}```")
    override val command: String
        get() = "rps"
    override val category: Categories
        get() = Categories.GAMES
    override val roles: List<Roles>
        get() = Roles.values().toList()
    override val description: String
        get() = "You can play rock paper scissors with bot/someone and having fun!"
    override val isDisplay: Boolean
        get() = true
}
