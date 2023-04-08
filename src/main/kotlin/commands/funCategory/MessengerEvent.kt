package commands.funCategory

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import utilities.messengerUtility.MessengerManager

class MessengerEvent : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.author.isSystem) return

        if (event.isFromType(ChannelType.PRIVATE)) {
            if (!MessengerManager.messenger.containsKey(event.author)) return
            val messenger: MessengerManager = MessengerManager.messenger[event.author]!!
            if (!messenger.started) return

            if (event.message.attachments.isNotEmpty()) {
                for (attachment in event.message.attachments) {
                    messenger.sendMessageToChannelImage(event.message, attachment)
                }
            } else {
                messenger.sendMessageToChannel(event.message)
            }
        } else if (event.isFromGuild && event.isFromThread) {
            if (!MessengerManager.dm.containsKey(event.author)) return
            val messenger: MessengerManager = MessengerManager.dm[event.author]!!

            if (!messenger.started) return
            if (messenger.getThread().idLong != event.channel.asThreadChannel().idLong) return println("WRONG!") // Check if the thread exist
            if (messenger.pause) return

            if (event.message.attachments.isNotEmpty()) {
                for (attachment in event.message.attachments) {
                    messenger.sendMessageToDmImage(event.message, attachment)
                }
            } else {
                messenger.sendMessageToDm(event.message)
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            val user = event.channel.asPrivateChannel().user
            if (!MessengerManager.messenger.containsKey(user)) return
            if (MessengerManager.messageCache.containsKey(event.messageIdLong)) {
                val content: String = MessengerManager.senderMessagesHistory[event.messageIdLong]!!.contentRaw
                val messenger = MessengerManager.messenger[user]!!

                messenger.senderMessageDeleted(event.messageIdLong, content)
            }

        } else if (event.isFromGuild && event.isFromThread) {
            // If the deleted message is contains with managerMessage key so this will activate it
            val user = MessengerManager.userCache[event.messageIdLong]

            if (!MessengerManager.dm.containsKey(user)) return
            val messenger = MessengerManager.dm[user]!!

            if (MessengerManager.messageCache.containsKey(event.messageIdLong)) {
                if (messenger.pause) return
                messenger.getterMessageDeleted(event.messageIdLong)
            }
        } else if (event.isFromGuild) {
            if (MessengerManager.managerMessage.containsKey(event.messageIdLong)) {
                val messenger = MessengerManager.managerMessage[event.messageIdLong]!!
                messenger.setThreadChecker(true)
                messenger.getThread().delete().queue()

                val msg = messenger.controlPanel().complete()

                messenger.createThreadMessages(msg).queue { thread ->
                    thread.sendMessage("${messenger.getter.asMention} Thread has been recreate you can continue chatting with ${messenger.sender.name} now!")
                        .queue()

                    messenger.setThread(thread)
                    thread.manager.setArchived(true).setLocked(true).queue()
                    messenger.setThreadChecker(false)

                    // to resend the thread panel and pin it
                    messenger.threadMessageInfoAction(thread)
                    msg.editMessageEmbeds(messenger.defaultEmbed()).queue()
                }

                messenger.setMessage(msg)
            }
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            if (!MessengerManager.messenger.containsKey(event.author)) return
            if (!MessengerManager.messenger[event.author]!!.started) return

            val messenger = MessengerManager.messenger[event.author]!!
            val message = event.message

            messenger.senderMessageEdit(message)
        } else if (event.isFromGuild && event.isFromThread) {
            if (!MessengerManager.dm.containsKey(event.author)) return
            val managerMessage = MessengerManager.dm[event.author]!!.message
            if (MessengerManager.threadMessages[managerMessage.idLong]!!.idLong != event.channel.asThreadChannel().idLong) return println("WRONG!")
            if (!MessengerManager.dm[event.author]!!.started) return

            val messenger = MessengerManager.dm[event.author]!!
            val message = event.message

            if (messenger.pause) return

            messenger.getterMessageEdit(message)
        }
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        if (event.channelType.isThread) {

            if (MessengerManager.threadManager.containsKey(event.channel.asThreadChannel())) {
                val mm = MessengerManager.threadManager[event.channel.asThreadChannel()]!!
                if (mm.isThread()) return
                if (mm.started) {
                    val thread = mm.message.createThreadChannel("${mm.sender.name} Messenger").complete()

                    // to resend the thread panel and pin it
                    mm.threadMessageInfoAction(thread)

                    thread.sendMessage("${mm.getter.asMention} Thread has been resumed you can start chatting with ${mm.sender.name} again")
                        .queue()
                    thread.manager.setArchived(true).setLocked(true).queue()

                    mm.setThread(thread)
                }
            }
        }
    }
}