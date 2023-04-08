package utilities.categoryUtility

import utilities.commandsutility.Command

data class CategoryManager(val category: Categories) {

    // category visible data
    val name: String = category.displayName
    val description: String = category.description
    val icon: String = category.icon

    // category invisible data
    val key: String = category.key
    val id: Int = category.id

    fun checkCommand(command: Command): Boolean {
        return command.category == category
    }

    fun keyId(): String {
        return "${category.key}:${category.id}"
    }
}
