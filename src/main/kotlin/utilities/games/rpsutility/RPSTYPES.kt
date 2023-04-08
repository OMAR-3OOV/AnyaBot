package utilities.games.rpsutility

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji

enum class RPSTYPES(val key: String, val displayName: String, val id: Int, val emoji: UnicodeEmoji) {

    ROCK("rock", "Rock", 0, Emoji.fromUnicode("✊")),
    PAPER("paper", "Paper", 1, Emoji.fromUnicode("\uD83D\uDD90")),
    SCISSORS("scissors", "Scissors", 2, Emoji.fromUnicode("✌"));

    companion object {
        fun getTypeByName(displayName: String): RPSTYPES {
            return RPSTYPES.valueOf(displayName.uppercase())
        }
    }
}