package com.ruislan


class Packet(
    val type: Type = Type.Stop
) {
    enum class Type {
        Gold, Stop, Dead;

        companion object {
            fun ofCode(code: Int) =
                when (code) {
                    1 -> Gold
                    2 -> Stop
                    3 -> Dead
                    else -> Stop
                }

        }
    }
}

class Ladder(
    val past: Ladder? = null,
    val packets: List<Packet>
) {
    fun height(): Int = if (null == past) 1 else past.height() + 1
}

class Player(
    val name: String,
    val initGold: Int = 0
) {
    var gold: Int = initGold
        private set

    fun gain(amount: Int) {
        if (amount > 0) gold += amount
    }

    fun lost(amount: Int) {
        if (amount > 0) gold -= amount
    }

    override fun toString(): String = name
}

class Game(
    val height: Int = 3,
    val player: Player,
    val bet: Int = 100, // 100 gold
    val muiltplier: Int = 1 // 1 * 100
) {
    var currentHeight = 1
        private set
    var canContinue = true
        private set
    var prize = 0
        private set
    val ladders: List<Ladder>

    init {
        require(height <= 5) {
            "height must higher than 0 and lower than 6"
        }
        val _laders = mutableListOf<Ladder>()
        var lastLadder: Ladder? = null
        for (i in 1..height) {
            val laderPackets = mutableListOf<Packet>()
            IntRange(1, 3).shuffled().forEach {
                laderPackets.add(Packet(Packet.Type.ofCode(it)))
            }
            lastLadder = Ladder(
                lastLadder,
                laderPackets
            )
            _laders.add(lastLadder)
        }
        ladders = _laders
    }


    private fun rewardPrize() {
        if (!canContinue && prize > 0)
            player.gain(prize)
    }

    private fun calculatePrize(): Int =
        when (currentHeight) {
            1 -> (bet * 1.7).toInt() * muiltplier
            2 -> bet * 4 * muiltplier
            3 -> (bet * 11.2).toInt() * muiltplier
            4 -> (bet * 32.8).toInt() * muiltplier
            5 -> (bet * 97.6).toInt() * muiltplier
            else -> 0
        }


    private fun clearPrize() {
        this.prize = 0
    }

    private fun incrementHeight() = currentHeight++

    fun open(index: Int): Packet? {
        if (canContinue && index > 0 && index <= 3) {
            val packet = ladders[currentHeight - 1].packets[index - 1]
            when (packet.type) {
                Packet.Type.Gold -> {
                    prize = calculatePrize() // prize for this height
                    if (currentHeight == height) endGame() // on the highest ladder
                    else incrementHeight()
                }
                Packet.Type.Stop -> {
                    prize = bet
                    endGame()
                }
                Packet.Type.Dead -> {
                    clearPrize()
                    endGame()
                }
            }
            return packet
        } else return null
    }

    fun endGame() {
        canContinue = false
        rewardPrize()
    }

    fun printLadders() {
        println("Ladders: ")
        ladders.forEach {
            println("ladder height: ${it.height()}")
            it.packets.forEachIndexed { index, packet ->
                print("index[$index] packet[${packet.type}]")
                if (index < it.packets.size - 1) print(" | ")
            }
            println()
        }
    }
}

fun main(args: Array<String>) {
    println("what's your name?")
    val name = readLine() ?: "somebody"
    val player = Player(name, 10000)
    var times = 0
    var retry: Boolean
    do {
        retry =
                if (player.gold > 100) {
                    player.lost(100) // lost 100 to play game
                    newGame(player)
                    times++
                    println("Retry ?")
                    val retryInput = readLine() ?: "y"
                    retryInput.equals("y", true)
                } else {
                    println("You don't have enough money!!!")
                    false
                }
    } while (retry)

    println("Player $player played $times games, init gold is ${player.initGold}, end gold is ${player.gold}, totally earn prize ${player.gold - player.initGold}")
}

fun newGame(player: Player) {
    val game = Game(5, player, 100, 1)

    println("Game start!")
    println("There ara ${game.height} ladders, each ladder has 3 packets, enter 1, 2, 3 to open,  enter other numbers to end game and take reward away.")
//    game.printLadders() //uncomment this to cheat

    // game start
    while (game.canContinue) {
        val currentHeight = game.currentHeight
        val ask = "current ladder height is: $currentHeight, input index[1-3] to open"
        if (currentHeight == 1) println("$ask?")
        else println("$ask or take prize ${game.prize} to end ?")
        val index = readLine()?.toInt() ?: 0
        if (1 > index || 3 < index) {
            println("now ending game...")
            game.endGame()
        } else {
            val packet = game.open(index)
            println("open in height[$currentHeight] with packet [${packet?.type}]")
        }
    }
    // game over
    println("Game ended at height: ${game.currentHeight - 1}")
    println("$player win prize: ${game.prize}")
    println("show all ladders?")
    val showIt = readLine() ?: "y"
    if (showIt.equals("y", true)) {
        println("this game ladders: ")
        game.printLadders()
    }
}
