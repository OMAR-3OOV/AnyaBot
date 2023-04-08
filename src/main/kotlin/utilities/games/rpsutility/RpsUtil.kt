package utilities.games.rpsutility

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.*
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

data class RpsUtil(
    val sender: User,
    var opponent: User?,
    val guild: Guild,
    val channel: TextChannel,
    var embed: EmbedBuilder
) : GameResource {

    companion object {
        val game: HashMap<Message, RpsUtil> = HashMap()

        @JvmStatic
        val pending: HashMap<User, RpsUtil> = HashMap()
    }
    
    var timer: ScheduledFuture<*>? = null
    var id = 0

    val playersUtil: PlayersUtil = PlayersUtil(this.guild, this.sender, this.opponent)
    val messageUtility = MessageUtil
    val games: HashSet<RpsUtil> = HashSet()

    init {
        this.opponent = playersUtil.opponent!!

        if (queue) {
            val queueUtil = QueueUtil(this.sender)

            embed.setAuthor("You are in queue :searching_ryuko_system:")
            embed.setDescription("> Currently users in queue: ${queueUtil.queue.size}")
            embed.setFooter(SimpleDateFormat("dd MMM - yyyy").format(Date()))
        } else {
            let {
                if (this.opponent == guild.selfMember.user) {
                    if (isInGame(this.sender)) {
                        return@let channel.sendMessage(
                            ":x: | `Process failed, you are already playing against ${
                                game.entries.stream().filter { it.value.sender == this.sender }.findFirst()
                                    .get().value.opponent
                            }"
                        ).queue()
                    }

                    println("bot")

                    return@let generateNewGameId()
                } else if (this.opponent!!.isBot || this.opponent!!.isSystem) {
                    return@let channel.sendMessage(":x: | **YOU CAN'T PLAY WITH OTHER BOTS, ONLY WITH ANYA >:c**")
                        .queue()
                } else if (this.opponent == this.sender) {
                    return@let channel.sendMessage(":x: | `Are you that lonely to play with yourself?`").queue()
                } else if (isInGame(this.sender)) {
                    return@let channel.sendMessage(
                        ":x: | `Process failed, you are already playing against ${
                            game.entries.stream().filter { it.value.sender == this.sender }.findFirst()
                                .get().value.opponent
                        }"
                    ).queue()
                } else if (isInGame(this.opponent!!)) {
                    return@let channel.sendMessage(
                        ":x: | `Process failed, it seems that ${this.opponent!!.name} is already playing against ${
                            game.entries.stream().filter { it.value.opponent == this.opponent }.findFirst()
                                .get().value.sender
                        }"
                    ).queue()
                } else {
                    generateNewGameId()
                    pending[opponent!!] = this
                }
            }
        }
    }

    fun createGame() {

        val embed: EmbedBuilder = this.embed

        embed.setDescription(
            gameGUI().toString()
                .replace("-", "\uD83D\uDFE5")
                .replace("|", "\uD83D\uDFE5")
                .replace(".", "⬛")
                .replace("P1", "◽")
                .replace("P2", "◽")
        )

        embed.setColor(Color(255, 150, 0))

        // message create data.
        val messageCreateData: MessageUtil<MessageCreateData, MessageCreateBuilder> = this.messageUtility.create()

        if (this.opponent == guild.selfMember.user) {
            // If the opponent are system
            val random = Random()

            val choice = when (val nextStep = random.nextInt(3)) { // 0, 1, 2 ( there is 3 choice only in rps game )
                0 -> RPSTYPES.ROCK
                1 -> RPSTYPES.PAPER
                2 -> RPSTYPES.SCISSORS
                else -> throw IllegalStateException("Unexpected value: $nextStep")
            }
            playersUtil.opponentMove = choice

            embed.setAuthor(sender.name + " Against Me")
            messageCreateData.addButtons(buttons(false))
        } else if (pending.containsKey(this.opponent)) {
            // Accept - Deny
            embed.setAuthor(sender.name + " Against " + opponent!!.name + " ( Pending request )")

            val pendingButtons: MutableList<Button> = arrayListOf(
                Button.success("accept-button-" + opponent!!.id, "Accept"),
                Button.danger("deny-button-" + opponent!!.id, "Deny")
            )

            timer = Executors.newSingleThreadScheduledExecutor().schedule(
                {
                    val messageEditData =
                        messageUtility.edit().message {
                            embed.setAuthor(sender.name + " Against " + opponent!!.name + " ( Non response )")
                            embed.setColor(Color(255, 79, 79))
                            it.setEmbeds(embed.build())
                        }.addButtons(pendingButtons.stream().map(Button::asDisabled).toList()).build()

                    messageUtility.message.editMessage(messageEditData).queue()
                    updatePendingRequest(false)
                }, 5, TimeUnit.SECONDS
            )
            messageCreateData.addButtons(pendingButtons)
        }

        messageCreateData.message {
            val messageEmbed: MessageEmbed = embed.build()
            it.addEmbeds(messageEmbed)
        }

        val message = this.channel.sendMessage(messageCreateData.build()).complete()
        this.messageUtility.message = message
        game[message] = this
    }

    fun rematch() {
        playersUtil.senderMove = null
        playersUtil.opponentMove = null

        if (opponent === guild.selfMember.user) {
            val random = Random()
            val choice = when (val nextStep = random.nextInt(3)) { // 0, 1, 2 ( there is 3 choice only in rps game )
                0 -> RPSTYPES.ROCK
                1 -> RPSTYPES.PAPER
                2 -> RPSTYPES.SCISSORS
                else -> throw IllegalStateException("Unexpected value: $nextStep")
            }
            playersUtil.opponentMove = choice
        }

        updateMessage(true)
    }

    fun gameEnd() {
        val move = WinningMove(playersUtil)

        val embed = this.embed
        var title = ""
        var subtitle = ""
        var color = Color(255, 150, 0)

        when (move) {
            0 -> { // null moves
                title = "Game over"
                subtitle =
                    "It seems that there is some technical problem, if it happened again please report for the developers!"
                color = Color(255, 79, 79)
            }
            1 -> { // winning moves
                title = "${sender.name} Won!"
                subtitle = "${sender.name} win against ${opponent!!.name} in rock paper scissors!"
                color = Color(100, 250, 100)
            }
            2 -> { // losing moves
                title = "${sender.name} Lost!"
                subtitle = "${sender.name} lost against ${opponent!!.name} in rock paper scissors!"
                color = Color(255, 79, 79)
            }
            3 -> { // drawing moves
                title = "${sender.name} Draw!"
                subtitle = "${sender.name} and ${opponent!!.name} Draw in rock paper scissors!"
                color = Color(100, 100, 250)
            }
        }

        embed.addField(title, subtitle, false).setColor(color)

        val messageEditData: MessageUtil<MessageEditData, MessageEditBuilder> = this.messageUtility.edit().message {
            it.setEmbeds(embed.build())
        }

        if (loop || rounds >= 1) {

            if (rounds >= 1) {
                rounds -= 1
            }

            this.messageUtility.message.editMessage(messageEditData.build()).queue()
        } else {
            messageEditData.addButtons(buttons(true))
            this.messageUtility.message.editMessage(messageEditData.build()).queue()
            endTasks()
        }
    }

    private fun  updateMessage(rematch: Boolean) {
        val embed = this.embed

        embed.setAuthor(sender.name + " Against " + opponent!!.name)
        embed.setDescription(
            gameGUI().toString()
                .replace("-", "\uD83D\uDFE5")
                .replace("|", "\uD83D\uDFE5")
                .replace(".", "⬛")
                .replace("P1", "◽")
                .replace("P2", "◽")
        )

        embed.setColor(Color(255, 150, 0))

        val messageEditData = messageUtility.edit().message {}

        if (rematch) {
            messageEditData.addButtons(buttons(false)).addButton(Button.danger("${gameId}-cancel", "❌").asEnabled())
            messageUtility.message.editMessage(messageEditData.build()).queue()
        } else {
            messageUtility.message.editMessage(messageEditData.build()).queue()
        }
    }

    fun resendGameMessage() {
        val messageCreateData = messageUtility.create().message {
            val messageEmbed = embed.build()
            it.setEmbeds(messageEmbed)
        }.build()

        val message = this.channel.sendMessage(messageCreateData).complete()
        messageUtility.message = message
        game[message] = this
    }

    fun updatePendingRequest(update: Boolean): Boolean {
        if (update) {
            pending.remove(this.opponent)
        } else {
            endTasks()
        }

        return false
    }

    fun endTasks() {
        games.remove(this)
        pending.remove(this.opponent)
        game.remove(this.messageUtility.message)
    }


    fun gameGUI(): StringBuilder {
        val gui: StringBuilder = StringBuilder()

        gui.append("> **").append(sender.name).append("** ( ")
            .append(if (playersUtil.isSenderMove()) "Ready" else "Waiting")
            .append(" )").append(" vs **").append(opponent!!.name).append("** ( ")
            .append(if (playersUtil.isOpponentMove()) "Ready" else "Waiting").append(" )").append("\n")

        gui.append(" ").append("\n")
        gui.append("---------").append("\n")
        gui.append("|...|...|").append("\n")
        gui.append("|.P1.|.P2.|").append("\n")
        gui.append("|...|...|").append("\n")
        gui.append("---------").append("\n")
        gui.append(" ").append("\n")

        return gui
    }

    /**
     * Check if user is exists in any running games
     *
     * @return boolean
     */
    private fun isInGame(user: User): Boolean {
        return game.entries.stream().anyMatch { all -> user == all.value.sender || user == all.value.opponent }
    }

    private fun generateNewGameId(): Int {
        val random = Random()
        val minimum = 10000
        val maximum = 99999

        this.id = random.nextInt((maximum - minimum) + 1)

        games.add(this)
        return id
    }

    override var gameId: Int = this.id
        set(value) {
            field = value
        }

    override var rounds: Int = 1
        set(value) {
            field = value
        }

    override var loop: Boolean = false
        set(value) {
            field = value
        }

    override var queue: Boolean
        get() = false
        set(value) { }
}