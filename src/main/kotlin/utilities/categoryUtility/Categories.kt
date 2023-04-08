package utilities.categoryUtility

enum class Categories (val key: String,val displayName: String,val description: String,val icon: String,val id: Int) {

    /**
     * Each command in this project should sort in this category type to specify the usage of the command.
     *
     * @param FUN related to the commands that people could have when with it.
     * @param MANAGEMENT related to the commands that staff can use in the bot or the guild to make a action with it.
     * @param GAMES related to the commands that create a games and make people play with together and have fun.
     * @param INFORMATION related to the commands that can get information's from such as user information or server information.
     *
     * @see CategoryManager
     */
    FUN("fun", "Fun", "This category for having fun and chill using some fun commands!", "\uD83C\uDF8A", 0),
    MANAGEMENT("management", "Management", "Management category made to be supported to your server!", "\uD83D\uDEE1", 1),
    GAMES("utilities/games", "Games", "There is a server games that you can use to play with your friends", "\uD83C\uDFAE", 2),
    INFORMATION("information", "Information", "You can have some information about your discord account or your server", "📃", 3),
    ;

    companion object {
        @JvmStatic
        fun getCategory(name: String): Categories {
            return Categories.valueOf(name.uppercase())
        }
    }
}