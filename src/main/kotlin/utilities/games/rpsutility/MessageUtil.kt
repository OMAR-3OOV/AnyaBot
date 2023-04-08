package utilities.games.rpsutility

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.util.LinkedList
import java.util.function.Consumer

/**
 * Message utility
 */
open class MessageUtil<D : MessageData, B : AbstractMessageBuilder<D, B>> (private val builder: B) {

    private val buttons: LinkedList<Button> = LinkedList()

    companion object {
        lateinit var message: Message

        fun create(): MessageUtil<MessageCreateData, MessageCreateBuilder> {
            return MessageUtil(MessageCreateBuilder())
        }

        fun edit(): MessageUtil<MessageEditData, MessageEditBuilder> {
            return MessageUtil(MessageEditBuilder())
        }
    }

    fun addButton(btn: Button): MessageUtil<D, B> {
        buttons.add(btn)
        message { it.setActionRow(buttons) }
        return this
    }

    fun addButtons(btns: List<Button>): MessageUtil<D, B> {
        buttons.addAll(btns)
        message { it.setActionRow(buttons) }
        return this
    }

    fun message(consumer: Consumer<B>): MessageUtil<D, B> {
        consumer.accept(builder)
        return this
    }

    fun build(): D {
        return builder.build().also { buttons.clear() }
    }

}