package dev.theredsrt4.fish

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
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
import kotlin.system.exitProcess

internal val configuration: Configuration =
    loadConfiguration()

/** Holds the client */
internal val twitchClient: ITwitchClient = createClient()
internal var listening = false


internal val channel = configuration.bot["channel"]

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
            println("[Fish] Version " + configuration.bot["version"])

            if(configuration.heartbeat){
                HttpClient.newBuilder().build()
                    .send(HttpRequest.newBuilder()
                        .uri(URI.create(configuration.api["hbkey"]))
                        .build(), HttpResponse.BodyHandlers.ofString())
                println("[Fish] Heartbeat Sent - Yo")
            }
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
        "twitch", configuration.credentials["irc"]
    )
    clientBuilder = clientBuilder
        .withChatAccount(credential)
        .withEnableChat(true)
    //endregion

    //region Api related configuration
    clientBuilder = clientBuilder
        .withClientId(configuration.api["client_id"])
        .withClientSecret(configuration.api["client_secret"])
        .withEnableHelix(true)
    //endregion

    // Build the client out of the configured builder
    return clientBuilder.build()
}

/** Load Config File **/
private fun loadConfiguration(): Configuration{
    val config: Configuration
    val classloader = Thread.currentThread().contextClassLoader
    val inputStream = classloader.getResourceAsStream("config.yaml")
    val mapper = ObjectMapper(YAMLFactory())

    try {
        config = mapper.readValue(inputStream, Configuration::class.java)
    }catch (ex:Exception){
        println("Unable to load config file... Exiting")
        exitProcess(1)
    }
    return config
}
