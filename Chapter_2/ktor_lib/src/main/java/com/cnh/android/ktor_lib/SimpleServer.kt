package com.cnh.android.ktor_lib

import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
//val gson = GsonBuilder().serializeNulls().create()

data class Category(val id: Int = 0, val name: String)
data class Item(val categoryId: Int, val subcategoryId: Int, val id: Int, val name: String)
data class SubCategory(val categoryId: Int, val id: Int, val name: String)

val categoriesJson = gson.toJson(
    Category(1, "Menu")
)

val subcategoriesJson = gson.toJson(
    listOf(
        SubCategory(1, 1, "Salads"),
        SubCategory(1, 2, "Fish And Seafood"),
        SubCategory(1, 3, "Soups")
    )
)
val itemsJson = gson.toJson(
    listOf(
        Item(1, 1, 1, "Overseas herring caviar"),
        Item(1, 1, 2, "Salted anchovy with baked potatoes and homemade butter"),
        Item(1, 1, 3, "Fried Anchovy with Georgian sauce"),
        Item(1, 1, 4, "Forshmak in a new way")
    )
)

fun main(args: Array<String>) {

    println(gson.toJson(gson.fromJson(categoriesJson, Category::class.java)))

    val server = embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
            get("/categories") {
                call.respond(categoriesJson)
            }
            get("/subcategory/{categoryId}") {
                call.respond(subcategoriesJson)
            }
            get("/items/{subCategoryId}") {
                call.respond(itemsJson)
            }
//            get("/items")    {
//                call.respond(itemsJson)
//            }
        }
    }
    server.start(wait = true)
}
