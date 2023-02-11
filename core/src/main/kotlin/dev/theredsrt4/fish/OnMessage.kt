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
    val fishchart = FishChart(channel, Database.connect(
        Keys.DATABASE_IP.text, driver = "org.postgresql.Driver", user = Keys.DATABASE_USER.text, password = Keys.DATABASE_PWD.text))


    //Put Boolean onCoolDown here...



    private fun getDateInt(): Int {
        return Date.from(Instant.now()).toInstant().atZone(ZoneId.of("America/New_York")).format(DateFormatter).toInt()
    }

    fun handleMessages(event: ChannelMessageEvent) {

        /** Admin Commands **/
        if(event.user.name.equals("theredsrt4"))
        {

        }
        /** if Fishing emote is displayed **/
        if (event.message.startsWith("fishing"))
        {
            val distance = Random.nextInt(1,100)
            val currentTime = System.currentTimeMillis()
            val lastUse = cooldownMap[event.user.name]

            //Cooldown of 5 seconds
            if (lastUse == null || currentTime - lastUse >= TimeUnit.SECONDS.toMillis(cooldownTime.toLong())) {
                cooldownMap[event.user.name] = currentTime

                /** Caught a Fish **/
                //region Caught A Fish
                if (distance == 37) {
                    val date = getDateInt()
                    size = Random.nextInt(1, 200)
                    rare = Random.nextInt(1, 1000)
                    if(rare == 752){
                        event.twitchChat.sendMessage(
                            event.channel.name, String.format(
                                "@s, PogU you caught a RARE Fish! It was %s lb fish!",
                                event.user.name,
                                size.toString()
                            )
                        )
                        println("[Fish] Adding ${event.user.name}'s ${size}lb fish to database...")
                        fishchart.AddCatch(date, event.user.name, size, true)
                    }
                    else{
                        event.twitchChat.sendMessage(
                            event.channel.name, String.format(
                                "@%s, PogU you caught a %s lb fish!",
                                event.user.name,
                                size.toString()
                            )
                        )

                        println("[Fish] Adding ${event.user.name}'s ${size}lb fish to database...")
                        fishchart.AddCatch(date, event.user.name, size, false)
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
        if (event.message.startsWith("!fishing")) {
            /** Fish Top command */
            if(event.message.contains("top")) {
                event.twitchChat.sendMessage(channel, fishchart.getTopCaught())
            }
            /** Fish Count command */
            else if(event.message.contains("count"))
            {
                if(fishchart.getTotalCaught() == 0)
                {
                    event.twitchChat.sendMessage(
                        event.channel.name, String.format(
                            "@%s, no fish have been caught yet Sadge", event.user.name
                        )
                    )
                }
                else{
                    event.twitchChat.sendMessage(event.channel.name, String.format(
                        "@%s, Total Fish Caught: %s Rare: %s", event.user.name, fishchart.getTotalCaught(), fishchart.getGoldCaught()
                    ))
                }
            }
            /** Fish OpenSource command */
            else if(event.message.contains("opensource")){
                    event.twitchChat.sendMessage(
                        event.channel.name, String.format(
                            "@%s, This project is completely open source! https://github.com/TheRedSRT4/FishingTwitchBot",
                            event.user.name
                        )
                    )
            }
            /** Fish About command **/
            else if(event.message.contains("about")){
                event.twitchChat.sendMessage(
                    event.channel.name, String.format(
                        "@%s, Fishing Bot Verison: %s     This bot allow you to fish in offline chat only. You have a 1/100 chance of catching a fish. Your fish's weight can be between 1-200. You also have a chance to get a rare fish (1/1000).    Bot Created by TheRedSRT4.    Emotes made by CrenoHD.    Please report all issues to me on Discord: TheRedSRT4#9652",
                        event.user.name, version
                    )
                )
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