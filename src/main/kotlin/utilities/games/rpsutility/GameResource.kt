package utilities.games.rpsutility

import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.ArrayList

interface GameResource {

    var gameId: Int
    var rounds: Int
    var loop: Boolean
    var queue: Boolean

    fun buttons(disable: Boolean) : MutableList<Button> {
        val bts : MutableList<Button> = ArrayList()

        for (type in RPSTYPES.values()) {
            if (disable) {
                bts.add(Button.primary("emoji-${gameId}-${type.key}", type.emoji).asDisabled())
            } else {
                bts.add(Button.primary("emoji-${gameId}-${type.key}", type.emoji).asEnabled())
            }
        }

        return bts
    }

    /**
     * This is winning move method to check who's the winner
     *
     * @return number of list [0, 1, 2 , 3]
     * @see 1 return to win.
     * @see 2 return to lose.
     * @see 3 return to draw.
     * @exception NullPointerException 0 return to null.
     * @Note it created as this way to make the system way easier to understand the concepts of the game.
     */
    fun WinningMove(playersUtil: PlayersUtil): Int {
        if (playersUtil.senderMove == null || playersUtil.opponentMove == null) {
            throw NullPointerException("Both of users have null move, this most be happen because of some bug in setup!")
        }

        if (playersUtil.senderMove!! == playersUtil.opponentMove!!) { // draw
            return 3
        }

        if (playersUtil.senderMove!! == RPSTYPES.ROCK) {
            if (playersUtil.opponentMove!! == RPSTYPES.SCISSORS) { // win
                return 1
            } else if (playersUtil.opponentMove!! == RPSTYPES.PAPER) { // lose
                return 2
            }
        } else if (playersUtil.senderMove!! == RPSTYPES.PAPER) {
            if (playersUtil.opponentMove!! == RPSTYPES.ROCK) { // win
                return 1
            } else if (playersUtil.opponentMove!! == RPSTYPES.SCISSORS) { // lose
                return 2
            }
        } else if (playersUtil.senderMove!! == RPSTYPES.SCISSORS) {
            if (playersUtil.opponentMove!! == RPSTYPES.PAPER) { // win
                return 1
            } else if (playersUtil.opponentMove!! == RPSTYPES.ROCK) { // lose
                return 2
            }
        }

        return 0
    }
}