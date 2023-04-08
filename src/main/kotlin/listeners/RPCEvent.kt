package listeners

import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import utilities.games.rpsutility.RPSTYPES
import utilities.games.rpsutility.RpsUtil
import java.awt.Color

class RPCEvent : ListenerAdapter() {

    override fun onGuildReady(event: GuildReadyEvent) {
        val commandData = ArrayList<CommandData>()

        commandData.add(
            Commands.slash("rps", "Rock Paper Scissor Game!")
                .addOption(OptionType.USER, "opponent", "The user that you want to play against him", false)
                .addOption(OptionType.INTEGER, "rounds", "The amount of rounds you want to play", false)
                .addOption(
                    OptionType.BOOLEAN,
                    "repeat",
                    "This will make you play unlimited time instead of using rounds option",
                    false
                )
                .addOption(
                    OptionType.BOOLEAN,
                    "queue",
                    "You will enter to global queue to play with random people who already inside the queue",
                    false
                )
        )

        event.guild.updateCommands().addCommands(commandData).queue()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = event.name

        if (event.guild != null) {
            if (command == "rps") {
                val embed = EmbedBuilder()
                val opponent: OptionMapping? = event.getOption("opponent")
                val rounds: OptionMapping? = event.getOption("rounds")
                val repeat: OptionMapping? = event.getOption("repeat")
                val queue: OptionMapping? = event.getOption("queue")

                if (opponent != null) {

                    val rps = RpsUtil(
                        event.user,
                        opponent.asUser,
                        event.guild!!,
                        event.channel.asTextChannel(),
                        embed
                    )

                    val messageCreateData = rps.messageUtility.create()

                    if (rounds != null) {
                        if (repeat != null) {
                            messageCreateData.message {
                                embed.setDescription("Process failed, You can't use a limited rounds options & repeatable option at the same time, please use one of them only!")
                                embed.setColor(Color(250, 79, 79))
                                it.addEmbeds(embed.build())
                            }

                            event.interaction.reply(messageCreateData.build()).setEphemeral(true).queue()
                            return
                        } else {
                            rps.rounds = rounds.asInt

                            rps.createGame()
                        }
                    } else if (repeat != null) {
                        if (queue != null) {
                            messageCreateData.message {
                                embed.setDescription("Process failed, You can't use repeatable option when you're in queue!")
                                embed.setColor(Color(250, 79, 79))
                                it.addEmbeds(embed.build())
                            }

                            event.interaction.reply(messageCreateData.build()).setEphemeral(true).queue()
                            return
                        } else {
                            rps.loop = repeat.asBoolean
                            println(rps.loop)
                            rps.createGame()
                        }
                    } else if (queue != null) {
                        messageCreateData.message {
                            embed.setDescription("Process failed, you can't able to use queue when you're already provide an opponent!")
                            embed.setColor(Color(250, 79, 79))
                            it.addEmbeds(embed.build())
                        }

                        event.interaction.reply(messageCreateData.build()).setEphemeral(true).queue()
                        return
                    } else {
                        rps.createGame()
                        println(rps.loop)
                    }

                    event.interaction.reply("Your pending message has been reach ${rps.opponent!!.name}, please wait till they accept!").setEphemeral(true).queue()
                } else {
                    val rps = RpsUtil(event.user, null, event.guild!!, event.channel.asTextChannel(), embed)

                    val messageCreateData = rps.messageUtility.create()

                    if (rounds != null) {
                        if (repeat != null) {
                            messageCreateData.message {
                                embed.setDescription("Process failed, You can't use a limited rounds options & repeatable option at the same time, please use one of them only!")
                                embed.setColor(Color(250, 79, 79))
                                it.addEmbeds(embed.build())
                            }

                            event.interaction.reply(messageCreateData.build()).setEphemeral(true).queue()
                            return
                        } else {
                            rps.rounds = rounds.asInt
                            println(rps.rounds)
                            rps.createGame()
                        }
                    } else if (repeat != null) {
                        if (queue != null) {
                            messageCreateData.message {
                                embed.setDescription("Process failed, You can't use repeatable option when you're in queue!")
                                embed.setColor(Color(250, 79, 79))
                                it.addEmbeds(embed.build())
                            }

                            event.interaction.reply(messageCreateData.build()).setEphemeral(true).queue()
                            return
                        } else {
                            rps.loop = repeat.asBoolean
                            println(rps.loop)
                            rps.createGame()
                        }
                    } else if (queue != null) {
                        messageCreateData.message {
                            embed.setDescription("Process failed, you can't able to use queue when you're already provide an opponent!")
                            embed.setColor(Color(250, 79, 79))
                            it.addEmbeds(embed.build())
                        }

                        event.interaction.reply(messageCreateData.build()).setEphemeral(true).queue()
                        return
                    } else {
                        rps.createGame()
                        println(rps.loop)
                    }

                    event.interaction.reply("You and ${event.guild!!.selfMember.asMention} playing Rock Paper Scissors \\:D!").queue()
                }
            }
        }

    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val user = event.user

        if (RpsUtil.pending.containsKey(user)) {

            val rps = RpsUtil.pending[user]

            if (event.button.id.equals("accept-button-${rps!!.opponent!!.id}")) {

                val embed = rps.embed.also {
                    it.setAuthor("${rps.sender.name} Against ${rps.opponent!!.name}")
                    it.setColor(Color(200, 177, 99))
                }

                event.editMessageEmbeds(embed.build()).setComponents(ActionRow.of(rps.buttons(false))).queue()

                rps.timer!!.cancel(false)
                rps.updatePendingRequest(true)
            } else if (event.button.id.equals("deny-button-${rps.opponent!!.id}")) {

                val embed = rps.embed.also {
                    it.setAuthor("${rps.sender.name} Against ${rps.opponent?.name} ( Denied )")
                    it.setColor(Color(200, 0, 0))
                }

                event.editMessageEmbeds(embed.build()).queue()
                rps.timer!!.cancel(false)
                rps.updatePendingRequest(false)
            }
        }

        if (RpsUtil.game.containsKey(event.message)) {
            val rps = RpsUtil.game[event.message]

            if (event.button.id.equals("${rps!!.gameId}-cancel") && (event.user == rps.sender || event.user == rps.opponent)) {
                rps.endTasks()

                val embed = rps.embed
                rps.messageUtility.edit().message {
                    embed.clearFields()
                    embed.addField("Game Over", "${event.user.asMention} has been cancel the match!", false)
                    it.setEmbeds(embed.build())
                }

                event.deferEdit().setComponents(ActionRow.of(event.message.buttons.map { it.asDisabled() }))
                    .setEmbeds(embed.build()).queue()
                return
            }

            val sender = rps.sender
            val opponent = rps.opponent

            if (event.button.id == "emoji-${rps.gameId}-rock" || event.button.id == "emoji-${rps.gameId}-paper" || event.button.id == "emoji-${rps.gameId}-scissors")
                if (event.user == sender || event.user == opponent) {
                    val selection = event.interaction.button.id!!.replace("emoji-${rps.gameId}-", "")

                    if (user == sender) {
                        rps.playersUtil.senderMove = RPSTYPES.getTypeByName(selection)
                    }

                    if (user == opponent) {
                        rps.playersUtil.opponentMove = RPSTYPES.getTypeByName(selection)
                    }

                    if (rps.playersUtil.isAllMove()) {
                        val embed = rps.embed.also {
                            it.setDescription(
                                rps.gameGUI().toString()
                                    .replace("-", "\uD83D\uDFE5")
                                    .replace("|", "\uD83D\uDFE5")
                                    .replace(".", "⬛")
                                    .replace("P1", rps.playersUtil.senderMove!!.emoji.name)
                                    .replace("P2", rps.playersUtil.opponentMove!!.emoji.name)
                            )
                        }

                        event.deferEdit().setEmbeds(embed.build()).setComponents(ActionRow.of(rps.buttons(true))).queue()
                        rps.gameEnd()

                        if (rps.loop || rps.rounds >= 1) {
                            rps.rematch()
                        }
                    } else {

                        val embed = rps.embed.also {
                            it.setDescription(
                                rps.gameGUI().toString()
                                    .replace("-", "\uD83D\uDFE5")
                                    .replace("|", "\uD83D\uDFE5")
                                    .replace(".", "⬛")
                                    .replace("P1", "◽")
                                    .replace("P2", "◽")
                            )
                        }

                        event.interaction.deferEdit().setEmbeds(embed.build()).queue()
                    }
                }

        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {

    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (RpsUtil.game.entries.any { it.key.id == event.messageId }) {
            val rps = RpsUtil.game.entries.find { it.key.id == event.messageId }?.value

            rps!!.resendGameMessage()
        }
    }

    override fun onShutdown(event: ShutdownEvent) {
        println("Bot shutdown, ${event.timeShutdown}")
        if (RpsUtil.game.isNotEmpty()) {
            RpsUtil.game.forEach { game ->
                run {
                    game.value.loop = false
                    val embed = Embed {
                        description = game.value.gameGUI().toString()
                        field {
                            name = "Game over!"
                            value =
                                "The bot has been shutdown, sorry for ending the game here, you can play again after the bot resume!"
                            inline = false
                        }
                    }

                    game.value.messageUtility.message.editMessageEmbeds(embed).queue()
                    game.value.endTasks()
                }
            }
        }
    }
}