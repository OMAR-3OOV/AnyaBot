package utilities.messengerUtility

import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onEntitySelect
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.exceptions.ContextException
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.Color
import java.net.URL
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors


/**
 * This manager can use only one time when it's already used.
 */
data class MessengerManager(val sender: User, val channel: TextChannel) {

    lateinit var getter: User
    lateinit var message: Message
    private var checkThread = false

    var pause = false
    private var save = false

    /**
     * It describes if the messenger is still ON or OFF, the default result is false, it will to be true when the [MessengerManager.messengerStart] used.
     */
    var started = false

    companion object {
        /**
         * ### [User] related to the [sender].
         * ### [MessengerManager] related to this class.
         *
         * This function is usable when the [sender] do any action.
         */
        val messenger: HashMap<User, MessengerManager> = HashMap()

        /**
         * ### [User] related to the [getter].
         * ### [MessengerManager] related to this class.
         *
         * This function is usable when the [getter] do any action.
         */
        val dm: HashMap<User, MessengerManager> = HashMap()

        /**
         * The messages that [sender] send it and the [TextChannel] receive it.
         */
        val getterMessagesHistory: HashMap<Long, Message> = HashMap()

        /**
         * The messages that [getter] send it and the [sender] receive it in DM.
         */
        val senderMessagesHistory: HashMap<Long, Message> = HashMap()

        /**
         * ### [Long] related to message id.
         * ### [String] related to message content.
         *
         * This hashmap will sort all [sender] messages that sent to bot, so it will be useful when [sender] delete a message.
         */
        val messageCache: HashMap<Long, String> = HashMap()

        /**
         * ### [Long] related to message id.
         * ### [User] related to [sender].
         * This hashmap will sort [sender] when send a message, it can be useful in MessageDeleteEvent: [net.dv8tion.jda.api.events.message.MessageDeleteEvent].
         */
        val userCache: HashMap<Long, User> = HashMap()

        /**
         * ### [Long] RELATED TO control panel message id.
         * ### [MessengerManager] related to the data class that contained with the control panel.
         *
         * This hashmap will store the control panel message, it can be useful when the message get deleted to it can be turn back automatically.
         */
        val managerMessage: HashMap<Long, MessengerManager> = HashMap()

        /**
         * ### [Message] related to the [message].
         * ### [ThreadChannel] related to the [createThreadMessages].
         *
         * This hashmap to store the thread channel from the message that have been created the thread, so this can help to get the information
         */
        val threadMessages: HashMap<Long, ThreadChannel> = HashMap()

        /**
         * ### [ThreadChannel] related to the thread that [message] created it from [createThreadMessages].
         * ### [Message] related to [message].
         *
         * This hashmap to store the message that created the thread, so if the thread deleted it will automatically create new thread
         */
        val threadManager: HashMap<ThreadChannel, MessengerManager> = HashMap()
    }

    /**
     * To start the messenger between [getter] & [sender]
     *
     * @exception Exception when [messengerStart] failed to start.
     */
    fun messengerStart() {
        try {
            this.started = true
            this.sender.openPrivateChannel().queue { private ->
                private.sendMessage("Hi ${sender.name}! 😀").queue() // First message send
                messenger[this.sender] = this
                dm[this.getter] = this
            }

            setMessage(controlPanel().complete())
            managerMessage[message.idLong] = this
            createThreadMessages(message).queue { thread ->
                setThread(thread)

                threadMessageInfoAction(thread)

                thread.sendMessage("${this.getter.asMention} Now you can start chatting with ${this.sender.name} here")
                    .queueAfter(2, TimeUnit.SECONDS)

                message.editMessageEmbeds(defaultEmbed()).queue()
            }

        } catch (error: Exception) {
            this.channel.sendMessage(":x: | I can't DM this user! `Error: ${error.message}`").queue()
            error.printStackTrace()
        }
    }

    /**
     * This method to send the messenger between the users
     */
    private fun messengerEnds() {
        this.started = false
        threadMessages.clear()
        threadManager.clear()
        messenger.clear()
        dm.clear()
        senderMessagesHistory.clear()
        getterMessagesHistory.clear()
        messageCache.clear()
        userCache.clear()
        managerMessage.clear()
        return
    }

    /**
     * This method to set [getter] the one who will send the messages to the channel and the bot going to send it to direct message to [sender]
     */
    @JvmName("setSenderMessenger")
    fun setGetter(user: User) {
        this.getter = user
    }

    /**
     * This method to set the Manage message the going to manage the messenger between [getter] and [sender]
     *
     * @param message related to the message going to send after the command used
     */
    @JvmName("setMessageMessenger")
    fun setMessage(message: Message) {
        this.message = message
        managerMessage[message.idLong] = this
    }

    /**
     * This method to get the thread from [threadMessages]
     */
    fun getThread(): ThreadChannel {
        return threadMessages[this.message.idLong]!!
    }

    /**
     * This method to set new thread, most use when the old thread get deleted
     */
    fun setThread(thread: ThreadChannel) {
        threadMessages[this.message.idLong] = thread
        threadManager[thread] = this
    }

    fun threadMessageInfoAction(thread: ThreadChannel) {
        val desc = StringBuilder()

        desc.append("<a:writingklee:1092172631110865027> **User:** ${this.sender.asTag}").append("\n \n")
        desc.append("<a:writingklee:1092172631110865027> **Time Created:** <t:${Timestamp.from(Instant.now()).toInstant().epochSecond}:R>").append("\n \n")
        desc.append("<a:writingklee:1092172631110865027> **Panel:** [Click Here](${this.message.jumpUrl})")
            .append("\n \n")

        thread.sendMessageEmbeds(
            Embed {
                title = "Information about this conversation"
                color = Color.PINK.rgb
                description = desc.toString()
                image = "https://imgur.com/hBW33t0.png"
            }
        ).setActionRow(Button.link(message.jumpUrl, "Control Panel").asEnabled()).queue { it.pin().queue() }
    }

    /**
     * This method will add the message to [messageCache] Hashmap.
     */
    private fun addMessageCache(message: Message) {
        messageCache[message.idLong] = message.contentRaw
    }

    /**
     * This method will add the user to [userCache] to be used to support [messageCache].
     */
    private fun addUserCache(message: Message) {
        userCache[message.idLong] = this.getter
    }

    /**
     * This method to send message to [sender] to the [channel], it will add to history automatically by using [addSenderMessage].
     *
     * @param message related to the message the [sender] send to bot.
     * @exception Exception if the message failed to reach to channel.
     */
    fun sendMessageToChannel(message: Message) {
        try {
            val msg = getThread()
                .sendMessage("**${sender.name}:** ${message.contentRaw}")
                .complete()

            addSenderMessage(message, msg)
            addMessageCache(message)
        } catch (err: Exception) {
            getThread().sendMessage(":x: | Failed to send! Error: ${err.message}").queue()
        }
    }

    /**
     * This method to send image message to [sender] to the [channel], it will add to history automatically by using [addSenderMessage].
     *
     * @param message related to the message the [sender] send to bot.
     * @exception Exception if the message failed to reach to channel.
     */
    fun sendMessageToChannelImage(message: Message, attach: Attachment) {
        try {
            val attachmentQueue: Queue<Attachment> = LinkedList()
            attachmentQueue.add(attach)
            val attachment = attachmentQueue.remove()
            val msg = getThread()
                .sendMessage("**${sender.name}:** ${message.contentRaw}")
                .setFiles(FileUpload.fromData(URL(attachment.url).openStream(), attachment.fileName))
                .complete()

            addSenderMessage(message, msg)
            addMessageCache(message)
        } catch (err: Exception) {
            getThread().sendMessage(":x: | Failed to send! Error: ${err.message}").queue()
        }
    }

    /**
     * This method to send message to [getter] DM, it will add to history automatically by using [addGetterMessage].
     *
     * @param message related to the message the [getter] send in [channel]
     * @param actionRow related to the message filter, if there is any button, it will display a button on the message that send to [sender]
     * @exception Exception if the message failed to reach to DM.
     */
    fun sendMessageToDm(message: Message, actionRow: ActionRow? = null) {
        try {
            this.sender.openPrivateChannel().queue { dm ->

                val msg: Message = if (actionRow != null) {
                    dm.sendMessage(message.contentDisplay).setComponents(actionRow).complete()
                } else {
                    dm.sendMessage(message.contentDisplay).complete()
                }

                addGetterMessage(message, msg)
                addMessageCache(message)
                addUserCache(message)
            }
        } catch (err: Exception) {
            message.reply(":x: | Failed to send! Error: ${err.message}").queue()
        }
    }

    /**
     * This method send image message to [getter] DM, it will add to history automatically by using [addGetterMessage].
     *
     * @param message related to the message the [getter] send in [channel]
     * @exception Exception if the message failed to reach to DM.
     */
    fun sendMessageToDmImage(message: Message, attach: Attachment) {
        try {
            this.sender.openPrivateChannel().queue { dm ->
                val attachmentQueue: Queue<Attachment> = LinkedList()
                attachmentQueue.add(attach)
                val attachment = attachmentQueue.remove()

                val msg: Message
                if (attachment != null) {
                    msg = dm.sendMessage(message.contentDisplay)
                        .setFiles(FileUpload.fromData(URL(attachment.url).openStream(), attachment.fileName)).complete()
                } else {
                    message.reply(":x: | Failed to send! Error: Attachment is null").queue()
                    return@queue
                }

                addGetterMessage(message, msg)
                addMessageCache(message)
                addUserCache(message)
            }
        } catch (err: Exception) {
            message.reply(":x: | Failed to send! Error: ${err.message}").queue()
        }
    }

    /**
     * ### [getterMessagesHistory] related to getter last massage
     *
     * This method to set the last message for [sender]
     */
    private fun addGetterMessage(recieved: Message, message: Message) {
        getterMessagesHistory[recieved.idLong] = message
    }

    /**
     * ### [senderMessagesHistory] related to getter last massage
     *
     * This method to set the last message for [getter]
     */
    private fun addSenderMessage(from: Message, message: Message) {
        senderMessagesHistory[from.idLong] = message
    }

    /**
     * ### [getterMessagesHistory] related to getter last massage
     *
     * This method is to edit the last message that getter sent to bot, if the message edited it going to edit the message cache as well.
     */
    fun getterMessageEdit(message: Message) {
        try {
            val content = message.contentRaw
            messageCache[message.idLong] = content
            getterMessagesHistory[message.idLong]!!.editMessage(content).queue()
        } catch (err: ContextException) {
            getThread().sendMessage(":x: | ${sender.name} has edit a message and it failed to edit! Error: ${err.message}")
                .queue()
            err.printStackTrace()
        } catch (err: Exception) {
            getThread().sendMessage(":x: | ${sender.name} has edit a message and it failed to edit! Error: ${err.message}")
                .queue()
            err.printStackTrace()
        }
    }

    /**
     * ### [senderMessagesHistory] related to sender last message
     *
     * This method is to edit the last message that sender sent to bot
     */
    fun senderMessageEdit(message: Message) {
        try {
            val content = message.contentRaw
            val msg = senderMessagesHistory[message.idLong]!!.editMessage("**${sender.name}:** $content (*Edited*)")
                .complete()
            senderMessagesHistory[message.idLong] = msg
        } catch (err: Exception) {
            getThread().sendMessage(":x: | Failed to edit! Error: ${err.message}").queue()
            err.printStackTrace()
        }
    }

    /**
     * This method to delete the last message that [sender] sent to bot
     */
    @Throws(ContextException::class)
    fun senderMessageDeleted(messageId: Long, content: String) {
        try {
            // This will check if the message can be deleted!
            senderMessagesHistory[messageId]!!
                .editMessage("$content (*Deleted*)")
                .setCheck { senderMessagesHistory[messageId] != null }
                .queue()
        } catch (err: Exception) {
            senderMessagesHistory[messageId]!!.reply(":x: | ${sender.name} has been delete a message and failed to announce you! Error: ${err.message}")
                .queue()
            err.printStackTrace()
        } catch (err: ContextException) {
            channel.sendMessage(":x: | ${sender.name} has been delete a message and failed to announce you! Error: ${err.message}")
                .queue()
            err.printStackTrace()
        }
    }

    /**
     * This method to delete the last message that [getter] sent to [sender] in DM
     */
    fun getterMessageDeleted(messageId: Long) {
        try {
            // This will check if the message can be deleted to it won't throw any errors!
            if (getterMessagesHistory[messageId]!!.type.canDelete()) {
                getterMessagesHistory[messageId]!!.delete().queue()
            }
        } catch (err: Exception) {
            message.reply(":x: | Failed to delete! Error: ${err.message}").queue()
        }
    }

    /**
     * To pause the messages that [getter] send it to [channel], which means if this option is on it won't let the [sender] receive any message till it get [resume]
     */
    private fun pause() {
        this.pause = true
    }

    /**
     * To resume the messages that [getter] will send, which mean every message send to channel from the [getter], the [sender] will get them.
     */
    private fun resume() {
        this.pause = false
    }

    /**
     * This method to check if the control panel message get deleted or not,
     * most use to manage the events because when the message delete the thread keeps,
     * so it should to delete the thread as well, after deleting thread, the delete event will not throw errors,
     * so it should check if the message is exists though this method.
     */
    fun isThread(): Boolean {
        return this.checkThread
    }

    /**
     * This method to set the checker to true or false when the message get deleted,
     * so it will not throw an error anymore if the message get deleted because of thread channel.
     */
    fun setThreadChecker(boolean: Boolean) {
        this.checkThread = boolean
    }

    /**
     * The default embed control panel [message].
     *
     * if the containers is null it will use the default containers that is set already!.
     */
    fun defaultEmbed(
        t: String = "messenger between ${getter.name} & ${sender.name}",
        c: Color = Color(0x2F3136),
    ): MessageEmbed {
        val desc = ArrayList<String>()

        desc.add("**> Information**")
        desc.add(" • You can end the conversation by pressing `End Messenger` Button!")
        desc.add(" • You can Pause/Resume the conversation by pressing `Pause/Resume` Button!")
        desc.add(" • Chat filters, **Usage:** `r?filters`")

        val embed = EmbedBuilder {
            title = t
            description = desc.stream().collect(Collectors.joining("\n"))
            color = c.rgb
        }

        embed.field {
            name = "Conversation Store"
            value = save.toString()
            inline = true
        }

        embed.field {
            name = "Conversation Status"
            value = if (!started) "Ended" else (if (pause) "Paused" else "Active")
            inline = true
        }

        if (threadManager.isNotEmpty()) {
            embed.field {
                name = "Thread"
                value = "[Join the thread](${getThread().jumpUrl})"
                inline = false
            }
        }

        return embed.build()
    }

    fun controlPanel(): MessageCreateAction {
        val jda = channel.jda

        val endButton: Button = Button.danger("${this.getter.id}-end", "End Messenger")
        val pauseButton: Button = Button.secondary("${this.getter.id}-pause", "Pause")
        val resumeButton: Button = Button.success("${this.getter.id}-resume", "Resume")

        val saveButton: Button = Button.success("${this.getter.id}-save", "Save this conversation")
        val unsaveButton: Button = Button.danger("${this.getter.id}-unsave", "UnSave this conversation")

        val usuallyButtonList: List<Button> =
            listOf(endButton, if (pause) resumeButton else pauseButton, if (save) unsaveButton else saveButton)


        jda.onButton(endButton.id!!) {
            if (it.user == getter && !it.isAcknowledged && started) {
                it.deferEdit().queue()

                it.hook.editOriginalEmbeds(
                    defaultEmbed(
                        t = "messenger between ${getter.name} & ${sender.name} (*Ends*)",
                        c = Color.RED
                    )
                ).setActionRow(
                    endButton.asDisabled(),
                    if (pause) resumeButton.asDisabled() else pauseButton.asDisabled(),
                    if (save) unsaveButton.asDisabled() else saveButton.asDisabled()
                ).queue()

                if (save) {
                    getThread().manager.setLocked(true).queue()
                } else {
                    getThread().delete().queue()
                }

                messengerEnds()

                println(started)
                return@onButton
            }
        }

        jda.onButton(pauseButton.id!!) {
            if (it.user == getter && !it.isAcknowledged && started) {
                it.deferEdit().queue()

                it.hook.editOriginalEmbeds(
                    defaultEmbed(
                        t = "messenger between ${getter.name} & ${sender.name} (*Paused*)",
                        c = Color.ORANGE
                    )
                ).setActionRow(endButton, resumeButton, if (save) unsaveButton else saveButton).queue()

                pause()
                return@onButton
            }
        }

        jda.onButton(resumeButton.id!!) {
            if (it.user == getter && !it.isAcknowledged && started) {
                it.deferEdit().queue()

                it.hook.editOriginalEmbeds(
                    defaultEmbed(
                        t = "messenger between ${getter.name} & ${sender.name} (*Resumed*)",
                        c = Color(0x2F3136)
                    )
                ).setActionRow(endButton, pauseButton, if (save) unsaveButton else saveButton).queue()

                resume()
                return@onButton
            }
        }

        jda.onButton(saveButton.id!!) {
            if (it.user == getter && !it.isAcknowledged && started) {
                save = true
                it.deferEdit().queue()

                it.hook.editOriginalEmbeds(
                    defaultEmbed(
                        t = "messenger between ${getter.name} & ${sender.name} (*${if (save) "Saved" else "Unsaved"}*)",
                    )
                ).setActionRow(endButton, if (pause) resumeButton else pauseButton, unsaveButton).queue()

                return@onButton
            }
        }

        jda.onButton(unsaveButton.id!!) {
            if (it.user == getter && !it.isAcknowledged && started) {
                save = false
                it.deferEdit().queue()

                it.hook.editOriginalEmbeds(
                    defaultEmbed(
                        t = "messenger between ${getter.name} & ${sender.name} (*${if (save) "Saved" else "Unsaved"}*)",
                    )
                ).setActionRow(endButton, if (pause) resumeButton else pauseButton, saveButton).queue()

                return@onButton
            }
        }


        return this.channel.sendMessageEmbeds(defaultEmbed()).setActionRow(usuallyButtonList)
    }


    /**
     * To create new thread channel to [Control panel message][MessengerManager.message]
     */
    fun createThreadMessages(message: Message): RestAction<ThreadChannel> {
        return channel.createThreadChannel("${sender.name} Messenger", message.id)
    }
}