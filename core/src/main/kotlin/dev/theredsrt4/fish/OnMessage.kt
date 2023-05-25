package dev.theredsrt4.fish

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import kotlin.random.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class OnMessage {

    private val DateFormatter = DateTimeFormatterBuilder().parseCaseInsensitive()
        .appendValue(ChronoField.YEAR, 4).appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendValue(ChronoField.DAY_OF_MONTH, 2).toFormatter()
    val cooldownTime = 5
    val cooldownMap = ConcurrentHashMap<String, Long>()
    var size = 0
    var rare = 0
    val fishchart = FishChart(channel!!, Database.connect(
        configuration.database["ip"].toString(), driver = "org.postgresql.Driver", user = configuration.database["user"].toString(), password = configuration.database["password"].toString()))



    //Put Boolean onCoolDown here...



    private fun getDateInt(): Int {
        return Date.from(Instant.now()).toInstant().atZone(ZoneId.of("America/New_York")).format(DateFormatter).toInt()
    }

    fun handleMessages(event: ChannelMessageEvent) {
        /** if Fishing emote is displayed **/
        if (event.message.startsWith("fishing", ignoreCase = true))
        {
            val chance = Random.nextInt(1,12)
            val distance = Random.nextInt(1,50)
            val currentTime = System.currentTimeMillis()
            val lastUse = cooldownMap[event.user.name]

            //Cooldown of 5 seconds
            if (lastUse == null || currentTime - lastUse >= TimeUnit.SECONDS.toMillis(cooldownTime.toLong())) {
                cooldownMap[event.user.name] = currentTime
                /** Caught a Fish **/
                //region Caught A Fish
                if (chance == 7) {
                    val date = getDateInt()
                    size = Random.nextInt(1, 201)
                    rare = Random.nextInt(1, 50)
                    if(rare == 37){
                        event.twitchChat.sendMessage(
                            event.channel.name, String.format(
                                "@%s, PogU you caught a RARE Fish! It was %s lb fish! PeepoGoldenFish",
                                event.user.name,
                                size.toString()
                            )
                        )
                        println("[Fish] Adding ${event.user.name}'s ${size}lb fish to database...")
                        fishchart.addCatch(date, event.user.name, size, true)
                    }
                    else{
                        if(size > 100) {
                            event.twitchChat.sendMessage(
                                event.channel.name, String.format(
                                    "@%s, PogU you caught a %s lb fish! peepoMEDFish" ,
                                    event.user.name,
                                    size.toString()
                                )
                            )
                        }
                        else{
                            event.twitchChat.sendMessage(
                                event.channel.name, String.format(
                                    "@%s, PogU you caught a %s lb fish! peepoSMOLFish" ,
                                    event.user.name,
                                    size.toString()
                                )
                            )
                        }

                        println("[Fish] Adding ${event.user.name}'s ${size}lb fish to database...")
                        fishchart.addCatch(date, event.user.name, size, false)
                    }
                }//endregion
                else {
                    event.twitchChat.sendMessage(
                        event.channel.name, String.format(
                            "@%s, try again (%s feet away)",
                            event.user.name,
                            distance.toString()
                        )
                    )
                }
            }
            else
            {
                val remainingTime = cooldownTime - (currentTime - lastUse) / 1000
                println("Command is on cooldown for $remainingTime seconds for user:" + event.user.name)
            }
        }
        /** Fish command */
        if (event.message.startsWith("!fishing", ignoreCase = true)) {
            /** Fish Top command */
            if(event.message.contains("top", ignoreCase = true)) {
                val args = event.message.split(" ")
                if(args.size == 3){
                    if(args[2] == "count")
                    {
                        event.twitchChat.sendMessage(channel, fishchart.getTopCountCaught())
                    }
                    if(args[2] == "size" || args[2] == "weight")
                    {
                        event.twitchChat.sendMessage(channel, fishchart.getTopWeightCaught())
                    }
                }
                else{
                    event.twitchChat.sendMessage(channel, "Usage of command is as follows: !fishing top count/weight/size")
                }
            }
            /** Fish Count command */
            else if(event.message.contains("count", ignoreCase = true)) {
                val args = event.message.split(" ")
                if(args.size == 3)
                {
                    val username: String = args[2].replace("@", "")
                    if(fishchart.getUserCaught(username.lowercase()) == null)
                    {
                        event.twitchChat.sendMessage(event.channel.name, String.format(
                            "@%s, user not found in database ope!", event.user.name)
                        )
                    }
                    else{
                        event.twitchChat.sendMessage(event.channel.name, String.format(
                            "@%s, %s has collected a total of %s fish and %s are RARE!", event.user.name, username, fishchart.getUserCaught(
                                username.lowercase()), fishchart.getUserGold(username.lowercase())
                        ))
                    }
                }
                if(fishchart.getTotalCaught() == 0)
                {
                    event.twitchChat.sendMessage(
                        event.channel.name, String.format(
                            "@%s, no fish have been caught yet Sadge", event.user.name
                        )
                    )
                }
                if(args.size == 2){
                    event.twitchChat.sendMessage(event.channel.name, String.format(
                        "@%s, Total Fish Caught: %s Rare: %s", event.user.name, fishchart.getTotalCaught(), fishchart.getGoldCaught()
                    ))
                }
            }
            /** Fish OpenSource command */
            else if(event.message.contains("opensource", ignoreCase = true)){
                    event.twitchChat.sendMessage(
                        event.channel.name, String.format(
                            "@%s, This project is completely open source! See TheRedSRT4 on Github!",
                            event.user.name
                        )
                    )
            }
            /** Fish About command **/
            else if(event.message.contains("about", ignoreCase = true)){
                event.twitchChat.sendMessage(
                    event.channel.name, String.format(
                        "@%s, Fishing Bot Verison: %s     This bot allow you to fish in offline chat only. You have a 1-12 chance of catching a fish. Your fish's weight can be between 1-200. You also have a chance to get a rare fish (1-50).    Bot Created by TheRedSRT4.    Emotes made by CrenoHD.    Please report all issues to me on Discord: TheRedSRT4#9652",
                        event.user.name, configuration.bot["version"]
                    )
                )
            }
            /** Fish Admin Command **/
            else if(event.message.contains("admin", ignoreCase = true)) {
                if(event.user.name.equals("theredsrt4")){
                    val args = event.message.split(" ")
                    if(event.message.contains("AddCatch", ignoreCase = true))
                    {
                        val chatter = args[3]
                        val size = args[4].toInt()
                        var goldfish = false
                        if(args[5] == "true")
                        {
                            goldfish = true
                        }
                        if(args[5] == "false"){
                            goldfish = false
                        }
                        println("[Fish] Adding ${chatter}'s ${size}lb fish to database...")
                        try {
                            fishchart.addCatch(getDateInt(), chatter, size, goldfish)
                            event.twitchChat.sendMessage(channel, "Successful Added To Database!")
                        }
                        catch (ex:Exception){
                            println("[Fish] Ummm. I broke..")
                            println(ex.toString())
                            event.twitchChat.sendMessage(channel, "AN ERROR OCCURRED CHECK LOG PANIC")
                        }
                    }
                    if(event.message.contains("rename", ignoreCase = true))
                    {

                    }
                    else{
                        event.twitchChat.sendMessage(channel, "Admin commands: AddCatch chatter size goldfish")
                    }
                }
                else{
                    event.twitchChat.sendMessage(channel, String.format(
                        "@%s, good try there buddy ol' pal. You aren't an admin. smh", event.user.name
                    ))
                }
            }
            /** Fish command (without anything after) **/
            else{
                event.twitchChat.sendMessage(channel, String.format(
                    "@%s, to fish, just type 'fishing'. Available Commands: !fishing top/count/admin/opensource/about", event.user.name
                ))
            }
        }
    }
}