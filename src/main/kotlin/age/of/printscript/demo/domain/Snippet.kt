package age.of.printscript.demo.domain

data class Snippet(
    val id: String,
    val title: String,
    val description: String,
    val ownerId: String,
    val language: String,
    val version: String,
)
