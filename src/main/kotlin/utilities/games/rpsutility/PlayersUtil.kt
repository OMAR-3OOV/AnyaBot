package utilities.games.rpsutility

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

open class PlayersUtil(val guild: Guild, val sender: User, var opponent: User?) {

    var senderMove: RPSTYPES? = null
    var opponentMove: RPSTYPES? = null

    init {
        if (opponent == null) {
            opponent = guild.selfMember.user
        }
    }

    fun isSenderMove(): Boolean {
        return (senderMove != null)
    }

    fun isOpponentMove(): Boolean {
        return (opponentMove != null)
    }

    fun isAllMove(): Boolean {
        return checkNotNull(isSenderMove() && isOpponentMove())
    }
}