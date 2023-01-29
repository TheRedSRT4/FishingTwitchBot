package dev.theredsrt4.fish

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

class WriteChannelChatToConsole{

    /** Subscribe to the ChannelMessage Event and write the output to the console */
    fun onChannelMessage(event: ChannelMessageEvent) {
        System.out.printf(
            "Channel [%s] - User[%s] - Message [%s]%n",
            event.channel.name,
            event.user.name,
            event.message
        )
    }

}