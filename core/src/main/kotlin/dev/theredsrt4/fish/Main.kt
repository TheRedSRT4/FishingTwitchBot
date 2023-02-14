package dev.theredsrt4.fish

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.ITwitchClient
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


/** Holds the client */
internal val twitchClient: ITwitchClient = createClient()
internal val channel = "JVLIA"
internal var listening = false
internal const val version = "1.3 BETA"

/** Check to see if Channel is offline **/
private fun isOffline(): Boolean{
    return twitchClient.helix.
    getStreams("", null, null, null, null, null, null, listOf(channel)).execute()
        .streams.isEmpty()
}

/** Main Run Function **/
fun main() {
    val onmessage = OnMessage()
    val LogChat = WriteChannelChatToConsole()
    val eventMang = twitchClient.eventManager
    GlobalScope.launch {
        while(true){
            println("[Fish] Version $version")
            HttpClient.newBuilder().build()
                .send(HttpRequest.newBuilder()
                    .uri(URI.create(Keys.HeartBeat.text))
                    .build(), HttpResponse.BodyHandlers.ofString())
            println("[Fish] Heartbeat Sent - Yo")
            if(isOffline() && !listening) {
                println("[Fish] Channel is Offline - Enabling...")
                twitchClient.chat.joinChannel(channel)
                twitchClient.chat.sendMessage(channel, "PogU You wanna go fishing")
                eventMang.onEvent(ChannelMessageEvent::class.java) { event: ChannelMessageEvent ->
                    onmessage.handleMessages(event)
                    LogChat.onChannelMessage(event)
                }
                listening = true
            }
            else if(!isOffline() && listening){
                /** Just a little thing for JVLIA **/
                if(channel == "JVLIA")
                {
                    twitchClient.chat.sendMessage(channel, "ope streamer is live gotta go jvliaLEAVE")
                }

                println("[Fish] Channel is Offline - Disabling...")
                twitchClient.chat.leaveChannel(channel)
                listening = false
            }
            //update site
            delay(60000)
        }
    }
}

/** Create Client **/
private fun createClient(): TwitchClient {
    var clientBuilder = TwitchClientBuilder.builder()
    //region Chat related configuration
    val credential = OAuth2Credential(
        "twitch", Keys.OAUTH.text
    )
    clientBuilder = clientBuilder
        .withChatAccount(credential)
        .withEnableChat(true)
    //endregion

    //region Api related configuration
    clientBuilder = clientBuilder
        .withClientId(Keys.CLIENT_ID.text)
        .withClientSecret(Keys.CLIENT_SECRET.text)
        .withEnableHelix(true)
    //endregion

    // Build the client out of the configured builder
    return clientBuilder.build()
}
