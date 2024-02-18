package net.punchtree.util.interaction

import org.bukkit.Bukkit

private const val CALLBACK_PREFIX = "punchtree:callback:"

object InteractionCallbackManager {

    private val callbackMap = mutableMapOf<String, InteractionCallback>()

    fun registerCallback(tag: String, callback: InteractionCallback) {
        Bukkit.getLogger().fine("registering callback for tag $tag")
        callbackMap[tag] = callback
    }

    internal fun getCallback(tag: String): InteractionCallback? {
        return callbackMap[tag.substring(CALLBACK_PREFIX.length)]
    }

}