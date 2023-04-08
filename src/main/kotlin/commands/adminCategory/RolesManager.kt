package commands.adminCategory

import utilities.commandsutility.Command
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utilities.categoryUtility.Categories
import utilities.staffUtility.Roles
import utilities.staffUtility.RolesData
import java.util.regex.Matcher
import java.util.regex.Pattern

class RolesManager: Command {
    override fun handle(args: List<String>, event: MessageReceivedEvent) {
        try {
            val handler = ArrayList<String>(args)

            if (handler.isNotEmpty()) {

                if (handler[0].contains(Regex("\\d+"))) {
                    var userMentioned = handler[0]

                    val regex: Pattern = Pattern.compile(Message.MentionType.USER.pattern.pattern())
                    val matcher: Matcher = regex.matcher(userMentioned)

                    var user: User? = null
                    if (matcher.find()) {
                        userMentioned =
                            userMentioned.replace("<", "").replace("!", "").replace("@", "").replace("#", "")
                                .replace("&", "").replace(">", "")
                        user = event.guild.retrieveMemberById(userMentioned).complete().user
                    } else {
                        event.channel.sendMessage("Sorry, i can't decide did you type `${handler[1]}` please use r?help role")
                            .queue()
                    }

                    if (handler.size <= 1) {
                        val roledata = RolesData(user!!)
                        val embed = Embed {
                            title = "[${roledata.getUserRole().displayName}] ${user.name}"
                            color = roledata.getUserRole().color.rgb
                            description = "**People with same role:** *${
                                roledata.config().getStringList(roledata.getUserRole().key).size
                            }*"
                        }

                        event.channel.sendMessageEmbeds(embed).queue()
                    } else if (handler[1] == "add") {
                        val roledata = RolesData(user!!)

                        if (handler.size >= 3) {
                            val role = handler[2]

                            if (roledata.getUserRole().key == Roles.valueOf(role.uppercase()).key) {
                                event.channel.sendMessage(":x: | ${user.asMention} have been already got this role before!").queue()
                                return
                            }

                            if (Roles.values().any { any -> any.key == role }) {
                                roledata.addRole(Roles.valueOf(role.uppercase()))
                                roledata.config()
                                event.channel.sendMessage("✅ | The ${Roles.valueOf(role.uppercase()).displayName} Role has been added to ${user.asMention}").queue()
                            } else {
                                event.channel.sendMessage(":x: | This role is not existing, use `r?role roles` to see the roles list").queue()
                            }
                        } else {

                        }
                    } else if (handler[1] == "remove") {
                        val roledata = RolesData(user!!)

                        if (handler.size >= 3) {
                            val role = handler[2]

                            if (roledata.getUserRole().key != Roles.valueOf(role.uppercase()).key) {
                                event.channel.sendMessage(":x: | ${user.asMention} doesn't have this role yet!").queue()
                                return
                            }

                            if (Roles.values().any { any -> any.key == role }) {
                                roledata.removeRole(Roles.valueOf(role.uppercase()))
                                roledata.config()
                                event.channel.sendMessage("✅ | The ${Roles.valueOf(role.uppercase()).displayName} Role has been removed from ${user.asMention}").queue()
                            } else {
                                event.channel.sendMessage(":x: | This role is not existing, use `r?role roles` to see the roles list").queue()
                            }
                        } else {

                        }
                    }
                } else {

                }
            }
        } catch (argsErr: NullPointerException) {
            argsErr.printStackTrace()
        } catch (roleErr: java.lang.IllegalArgumentException) {
            event.channel.sendMessage(":x: | This role is not existing, use `r?role roles` to see the roles list").queue()
            roleErr.printStackTrace()
        }

    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent
    ) {
        TODO("Not yet implemented")
    }

    override val help: String
        get() = "r?role <user> <add/remove> <role>`(you can use r?role <user> to get info)!`"
    override val command: String
        get() = "role"
    override val category: Categories
        get() = Categories.MANAGEMENT
    override val roles: List<Roles>
        get() = arrayListOf(Roles.CEO, Roles.ADMIN)
    override val description: String
        get() = "To add/remove the role from the discord user (*ON DISCORD BOT CONTROL*) or get the info"
    override val isDisplay: Boolean
        get() = false
}