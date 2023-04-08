package utilities.configUtil

import com.google.gson.GsonBuilder
import java.io.File

// Json config file to store data inside it, the config.json will not be included in Github
class ConfigManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val config: JsonConfig
    private val file = File("System/config.json")

    init {
        val jsonString = file.readText()
        config = gson.fromJson(jsonString, JsonConfig::class.java) ?: JsonConfig("-", "r?", "-", emptyList())
    }

    val token: String
        get() = config.token
    val prefix: String
        get() = config.prefix
    val username: String
        get() = config.username
    val activity: List<String>
        get() = config.activity

    /**
     * To save the file after any change made
     */
    fun save() {
        val json = gson.toJson(config)
        file.writeText(json)
    }

    /**
     * To save the file after a [config] specific change made
     */
    fun save(config: JsonConfig) {
        val json = gson.toJson(config)
        file.writeText(json)
    }

    fun getConfig(): JsonConfig {
        return config
    }

    /**
     * This method to save an existing object inside the config file!
     * otherwise it will just make a change of the existing property.
     *
     * @param [token] related to api token to identify the bot user and make it online (launch).
     * @param [prefix] related to bot prefix that use to identify the commands.
     * @param [activity] related to the bot online activity which can be for several usage.
     * @exception IllegalArgumentException if the property that called is not exist, it will return the error message (config is really special file that is not allowed to add any unneeded information into it)!
     */
    fun set(field: String, value: Any?) {
        when (field) {
            "token" -> config.token = value as String
            "prefix" -> config.prefix = value as String
            "username" -> config.username = value as String
            "activity" -> config.activity = value as List<String>
            else -> throw IllegalArgumentException("Unknown field $field")
        }
    }

    fun addActivity(activity: String) {
        val newActivities = config.activity.toMutableList().apply { add(activity) }
        save(config.copy(activity = newActivities))
    }

    fun removeActivity(activity: String) {
        val newActivities = config.activity.toMutableList().apply { remove(activity) }
        save(config.copy(activity = newActivities))
    }

    data class JsonConfig(var token: String, var prefix: String, var username: String, var activity: List<String>) {
        var extraProperties: MutableMap<String, Any>? = null
    }
}