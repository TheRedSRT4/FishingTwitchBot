package dev.theredsrt4.fish

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.ITwitchClient
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.system.exitProcess
import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration


internal val configuration: ConfigurationFile =
    loadConfiguration()

/** Holds the client */
internal val twitchClient: ITwitchClient = createClient()
internal var listening = false


internal val channel = configuration.bot["channel"]

/**
private val folder = File(channel).also { if(!it.exists()) it.mkdir() }
private val data = File(folder, "data").also { if(!it.exists()) it.mkdir() }
private val rows = arrayListOf<File>()
private val index = File(folder, "index.html").also { if(!it.exists()) it.createNewFile() }
private val objectMapper = jacksonObjectMapper()
*/


/** Check to see if Channel is offline **/
private fun isOffline(): Boolean{
    return twitchClient.helix.
    getStreams("", null, null, null, null, null, null, listOf(channel)).execute()
        .streams.isEmpty()
}

/** Main Run Function **/
fun main() {

    /**
    val freemarker = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).also {
        it.templateLoader = ClassTemplateLoader(it::class.java.classLoader, "")
    }
    freemarker.template("fish.ftl", mapOf(
    "channel" to channel), index)
    freemarker
    val fishchart = FishChart(channel!!, Database.connect(
    configuration.database["ip"].toString(), driver = "org.postgresql.Driver", user = configuration.database["user"].toString(), password = configuration.database["password"].toString()))

     */
    val onmessage = OnMessage()
    val LogChat = WriteChannelChatToConsole()
    val eventMang = twitchClient.eventManager


    //updateSite()
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
            delay(60000)
        }
    }
}

/** Update Site
fun updateSite(){
    var num = 0
    val temps = arrayListOf<File>()
    fishchart.forEachDescending(10000, 1){ group ->
        val temp = File(data, "${num+1}.temp.json")
        temp.createNewFile()
        temps.add(temp)
        objectMapper.writeValue(temp, group)
        num++
    }
    for(i in 0 until num){
        temps[i].inputStream().use { fis ->
            (rows.getOrNull(i) ?: File(data, "${i+1}.json").also { rows.add(i, it) }).outputStream().use { os ->
                os.write("""{"last_page":$num,"data":""".toByteArray())

                var len: Int
                val buffer = ByteArray(1024)
                while (fis.read(buffer).also { len = it } != -1) os.write(buffer, 0, len)

                os.write(0x7D)
            }
        }
    }
    if(rows.size > num){
        for(i in num until rows.size)
            rows[i].delete()
    }
    temps.forEach{ it.delete() }
}
 **/

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
private fun loadConfiguration(): ConfigurationFile{
    val config: ConfigurationFile
    val classloader = Thread.currentThread().contextClassLoader
    val inputStream = classloader.getResourceAsStream("config.yaml")
    val mapper = ObjectMapper(YAMLFactory())

    try {
        config = mapper.readValue(inputStream, ConfigurationFile::class.java)
    }catch (ex:Exception){
        println("Unable to load config file... Exiting")
        exitProcess(1)
    }
    return config
}

private fun Configuration.template(template: String, data: Any, out: File) {
    out.writer().use {
        getTemplate(template).process(data, it)
    }
}
