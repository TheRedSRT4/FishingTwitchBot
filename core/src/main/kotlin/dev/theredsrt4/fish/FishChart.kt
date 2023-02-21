package dev.theredsrt4.fish

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File


class FishChart (channel: String, private val database: Database){
    private val fishTable = FishTable(channel)

    init {
        transaction(database){
            println("[Fish] Creating SQL Table....")
            SchemaUtils.createMissingTablesAndColumns(fishTable)
        }
    }
    fun addCatch(date: Int, chatter: String, size: Int, goldfish: Boolean){
        transaction(database){
            val row = fishTable.select {fishTable.name eq chatter }.firstOrNull()
            if(row != null){ //If chatter caught a fish before
                fishTable.update ({ fishTable.name eq  chatter }){ //update row that equals chatters name
                    val catches = row[fishTable.catches] + size //add size to array via val
                    val biggest = row[fishTable.biggest] //val to show biggest catch
                    it[fishTable.catches] = catches
                    it[fishTable.last] = date //add today as last catch
                    it[fishTable.count] = catches.size //add size of array as count
                    if(biggest <= size){//if new catch is greater than last catch
                        it[fishTable.biggest] = size
                    }
                    if(goldfish)
                    {
                        val gold = row[fishTable.goldfish]+1 //add +1 to goldfish column
                        it[fishTable.goldfish] = gold
                    }
                }
            } else{//if chatter never caught before
                fishTable.insert {
                    it[fishTable.name] = chatter //add name
                    it[fishTable.catches] = arrayOf(size) //add catch to array
                    it[fishTable.last] = date //todays date
                    it[fishTable.count] = 1 //put count to 1
                    it[fishTable.biggest] = size //size of catch
                    if(goldfish){
                        it[fishTable.goldfish] = 1
                    }
                    else{
                        it[fishTable.goldfish] = 0
                    }
                }
            }
        }
    }
    fun getTotalCaught(): Int {
        var i = 0
        transaction(database){
            val result = fishTable.slice(fishTable.count.sum()).selectAll()
            i = result.first().toString().split("=")[1].trim().toInt()
        }
        return i
    }
    fun getTopWeightCaught(): String {
        var message = ""
        transaction(database) {
            val top3 = fishTable.selectAll().orderBy(fishTable.biggest, SortOrder.DESC).limit(3).map {
                "${it[fishTable.name]} ${it[fishTable.biggest]}lbs"
            }
            message =  "Top 3 Catches: ${top3.joinToString(", ")}"
        }
        return message
    }
    fun getTopCountCaught(): String{
        var message = ""
        transaction(database) {
            val top3 = fishTable.selectAll().orderBy(fishTable.count, SortOrder.DESC).limit(3).map {
                "${it[fishTable.name]}: ${it[fishTable.count]}"
            }
            message =  "Top 3 Fisherman: ${top3.joinToString(", ")}"
        }
        return message
    }
    fun getGoldCaught(): Int{
        var i = 0
        transaction(database){
            val result = fishTable.slice(fishTable.goldfish.sum()).selectAll()
            i = result.first().toString().split("=")[1].trim().toInt()
        }
        return i
    }
    fun getUserCaught(username: String): Int?{
        var count: Int? = null
        transaction(database){
            val query = fishTable.select { fishTable.name eq username }.limit(1)
            count = query.firstOrNull()?.get(fishTable.count)
        }
        return count
    }
    fun getUserGold(username: String): Int?{
        var gold: Int? = null
        transaction(database){
            val query = fishTable.select { fishTable.name eq username }.limit(1)
            gold = query.firstOrNull()?.get(fishTable.goldfish)
        }
        return gold
    }
    fun forEachDescending(batch: Int, count: Int, callback: (Iterable<FishUser>) -> Unit){
        val total = getTotalCaught()
        val list = arrayListOf<FishUser>()
        var curr = 1
        transaction(database){
            val range = if(count >= total) total..total else total downTo count
            for(i in range) {
                fishTable.selectBatched { fishTable.count eq i }.forEach { it.forEach { row ->
                    list.add(FishUser(row[fishTable.name], row[fishTable.count], row[fishTable.biggest], row[fishTable.goldfish]))

                    if(curr++ > batch) {
                        callback(list)
                        list.clear()
                        curr = 1
                    }
                }}
            }
            if(list.size != 0) callback(list)
        }
    }

}
class FishTable(channel: String): Table("${channel}_fish"){
    val id = integer("id").autoIncrement() //for batch
    val name = varchar("name", 50).uniqueIndex() //name of chatter
    val catches = array<Int>("catches", IntegerColumnType())
    val count = integer("count") //amount of catches, size of catches array
    val biggest = integer("biggest") //largest catch not displaying lbs
    val last = integer("last")
    val goldfish = integer("goldfish")
}
data class FishUser(val name: String, val count: Int, val biggest: Int, val goldfish: Int)