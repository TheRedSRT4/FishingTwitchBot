package dev.theredsrt4.fish

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FishChart (channel: String, private val database: Database){
    private val fishTable = FishTable(channel)

    init {
        transaction(database){
            println("[Fish] Creating SQL Table....")
            SchemaUtils.createMissingTablesAndColumns(fishTable)
        }
    }
    fun AddCatch(date: Int, chatter: String, size: Int){
        transaction(database){
            val row = fishTable.select {fishTable.name eq chatter }.firstOrNull()
            if(row != null){ //If chatter caught a fish before
                fishTable.update ({ fishTable.name eq  chatter }){ //update row that equals chatters name
                    val catches = row[fishTable.count]+1 //add +1 to count via val
                    val lastsize = row[fishTable.biggest]
                    it[fishTable.last] = date //add today as last catch
                    it[fishTable.count] = catches //add count of catches using val
                    if(lastsize <= size){//if new catch is greater than last biggest
                        it[fishTable.biggest] = size
                    }
                }
            } else{//if chatter never caught before
                fishTable.insert {
                    it[fishTable.name] = chatter //add name
                    it[fishTable.count] = 1 //set count to 1
                    it[fishTable.last] = date //todays date
                    it[fishTable.biggest] = size //size of catch
                }
            }
        }
    }
    fun getTotalCaught(): String{
        var i = ""
        transaction(database){
            val result = fishTable.slice(fishTable.count.sum()).selectAll()
            i = result.first().toString().split("=")[1].trim()
        }
        return i
    }
    fun getTopCaught(): String {
        var message = ""
        transaction(database) {
            val top3 = fishTable.selectAll().orderBy(fishTable.biggest, SortOrder.DESC).limit(3).map {
                "${it[fishTable.name]} ${it[fishTable.biggest]}lbs"
            }
            message =  "Top 3 Catches: ${top3.joinToString(", ")}"
        }
        return message
    }

}
class FishTable(channel: String): Table("${channel}_fish"){
    val id = integer("id").autoIncrement() //for batch
    val name = varchar("name", 50).uniqueIndex() //name of chatter
    val count = integer("catches") //amount of catches, size of catches array
    val last = integer("last") //the last catch
    val biggest = integer("biggest") //largest catch not displaying lbs
}