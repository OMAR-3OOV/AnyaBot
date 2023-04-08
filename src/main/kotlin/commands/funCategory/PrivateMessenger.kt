package commands.funCategory

import utilities.commandsutility.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.categoryUtility.Categories
import utilities.messengerUtility.MessengerManager
import utilities.staffUtility.Roles
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * This command is private and only few people who can access it.
 * It made to be a trolling between friends and having fun.
 */
class PrivateMessenger : Command {

    override fun handle(args: List<String>, event: MessageReceivedEvent) {
        try {

            val permissions = arrayListOf(Permission.MESSAGE_SEND, Permission.MANAGE_THREADS, Permission.MANAGE_CHANNEL)
            val permissionsDidntHave: ArrayList<Permission> = arrayListOf()
            var listChecker = false

            for (permission in permissions) {
                if (!event.guild.selfMember.hasPermission(permission)) {
                    permissionsDidntHave.add(permission)
                }

                if (!permissionsDidntHave.isEmpty() && !listChecker) {
                    listChecker = true
                }
            }

            if (listChecker) {
                event.channel.sendMessage(
                    ":x: | **The permissions requires to use this command is not available, please check if i have the following permissions**: ${
                        permissionsDidntHave.stream().map { "`${it.getName()}`" }.collect(
                            Collectors.joining(" / ")
                        )
                    }"
                ).queue()
                return
            }


            val handler = ArrayList<String>(args)
            if (event.channel.asTextChannel().permissionOverrides.any { map -> map.permissionHolder!!.hasPermission(Permission.MESSAGE_SEND) }) {
                event.channel.sendMessage(":x: | This is public text channel, please make sure to use this command in private channel so you will feel the experience!")
                    .queue()
            }

            var userMentioned = handler[0]

            val regex: Pattern = Pattern.compile(Message.MentionType.USER.pattern.pattern())
            val matcher: Matcher = regex.matcher(userMentioned)

            userMentioned = if (matcher.find()) {
                userMentioned.replace("<", "").replace("!", "").replace("@", "").replace("#", "")
                    .replace("&", "").replace(">", "")
            } else if (Pattern.compile("\\d+").matcher(userMentioned).find()) {
                userMentioned.replace("<", "").replace("!", "").replace("@", "").replace("#", "")
                    .replace("&", "").replace(">", "")
            } else {
                event.channel.sendMessage(":x: | Sorry i can't decide what $userMentioned means! **Make sure to mention a user or use the user id**")
                    .queue()
                return
            }

            val user = event.guild.retrieveMemberById(userMentioned).complete().user

            val channel: TextChannel = event.channel.asTextChannel()

            // There is no meaning off messaging bot at all because he will not respond!
            if (user.isBot || user.isSystem) {
                event.channel.sendMessage(":x: | YOU CAN'T USE THIS COMMAND WITH OTHER BOTS! >:c").queue()
                return
            }

            val messenger = MessengerManager(user, channel)

            if (MessengerManager.dm.containsKey(event.author) && MessengerManager.messenger.containsKey(user)) {
                event.channel.sendMessage(":x: | You're already with ${MessengerManager.dm[event.author]!!.sender.name} in messenger!")
                    .queue()
            }

            messenger.setGetter(event.author)
            messenger.messengerStart()
        } catch (userErr: IndexOutOfBoundsException) {
            event.channel.sendMessage(":x: | This user is not exist or wrong!").queue()
        }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {

    }

    override val help: String
        get() = "r?messenger <user>"
    override val command: String
        get() = "messenger"
    override val category: Categories
        get() = Categories.FUN
    override val roles: List<Roles>
        get() = arrayListOf(Roles.ADMIN, Roles.CEO)
    override val description: String
        get() = "Its mode who only admins can use to make the bot is messenger between two people"
    override val isDisplay: Boolean
        get() = false
}