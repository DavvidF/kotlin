
package minesweeper
import kotlin.random.Random

const val xFieldSize = 9
const val yFieldSize = 9


fun main() {


    println("How many mines do you want on the field?")
    var minesNumber = readln().toInt()

    var field = createMutableLisOfEmptyGround()

    var firstRun = true
    while (minesNumber>0) {

        printField(field)
        println("Set/unset mine marks or claim a cell as free:")
        val userPosition = readln().split(" ").map { it }

        if (firstRun) field = initializationField(minesNumber, field, userPosition )
        firstRun =false

        val selectGround = field[userPosition[1].toInt()-1][userPosition[0].toInt()-1]
        if (userPosition[2] == "mine") {
            minesNumber = selectGround.mark(minesNumber)
        } else if (userPosition[2] == "free") {
            if (selectGround.isAMines) {
                printField(field, false)
                println("You stepped on a mine and failed!\n")
                break
            } else {
                selectGround.explored = true
                if (selectGround.stringPrinted == "/") {
                    exploreAroundEmpty(field,listOf(userPosition[1].toInt()-1,userPosition[0].toInt()-1))
                }
            }
        }
        if (minesNumber == 0) {
            printField(field, false)
            println("Congratulations! You found all the mines!")
        }
        //printField(field, false)
    }
}

fun createMutableLisOfEmptyGround(): MutableList<MutableList<Ground>> {
    val field: MutableList<MutableList<Ground>> = mutableListOf(mutableListOf())

    for (x in 0..xFieldSize-1) {
        val row =  mutableListOf<Ground>()
        for (y in 0..yFieldSize-1) {
            val position = Ground(x,y)
            row.add(y, position)
        }
        field.add(row)
    }
    field.removeFirst()
    return field
}

fun initializationField(minesNumber: Int, field:MutableList<MutableList<Ground>>, userPosition: List<String>): MutableList<MutableList<Ground>> {

    val randomList: MutableList<Int> = mutableListOf()

    for (i in 1..minesNumber) {
        randomList.add(Random.nextInt(0, xFieldSize * yFieldSize))
    }

// Check multiple mines are on the same position or on the user's first choice
    for (i in 0..<randomList.size) {
        var value = randomList[i]
        randomList.removeAt(i)
        while (randomList.contains(value) || value == userPosition[1].toInt() * 9 + userPosition[0].toInt()) {
            value = Random.nextInt(0,xFieldSize * xFieldSize)
        }
        randomList.add(i, value)
    }

// Put mines following random list
    for (i in 0..<randomList.size) {
        field[randomList[i]/9][randomList[i]%9].isAMines = true
    }

// Create the hints on the fields
    createHints(field)


    return field
}


fun createHints(field: MutableList<MutableList<Ground>>): MutableList<MutableList<Ground>> {
    for (x in 0..<field.size) {
        for (y in 0..<field[x].size) {
            if (field[x][y].isAMines) {
                for (k in -1..1) {
                    for (j in -1..1) {
                        field.getOrNull(x+k)?.getOrNull(y+j)?.addMinesSide()
                    }
                }
            }
        }
    }
    return field
}

fun exploreAroundEmpty(field: MutableList<MutableList<Ground>>, userChoice: List<Int> = listOf(0,0)): MutableList<MutableList<Ground>> {
    val listNeighbour: MutableList<List<Int>> = mutableListOf(userChoice)

    while (listNeighbour.isNotEmpty()) {
        for (k in -1..1) {
            for (j in -1..1) {
                val kPosition = listNeighbour[0][0] + k
                val jPosition = listNeighbour[0][1] + j
                if (field.getOrNull(kPosition)?.getOrNull(jPosition)?.isAMines == false) {
                    if (field.getOrNull(kPosition)?.getOrNull(jPosition)?.hintsValue == 0) {
                        if (!field[kPosition][jPosition].explored) listNeighbour.add(mutableListOf(kPosition,jPosition))
                    }
                    if (field.getOrNull(kPosition)?.getOrNull(jPosition)?.hintsValue!! >= 0) {
                        field[kPosition][jPosition].explored = true
                    }
                }
            }
        }
        listNeighbour.removeFirst()
    }

    return field
}

class Ground(val xPosition: Int = 0, val yPosition: Int = 0) {
    var isAMines = false
    var explored = false
    var marked = false
    var hintsValue = 0

    var stringPrinted = "."
        get() = if (!explored) {
            if (marked) {
                "*"
            } else{
                "."
            }
        } else if (hintsValue == 0) {
            "/"
        }  else {
            hintsValue.toString()
        }
    val stringPrintedUnmasked = stringPrinted
        get() {
            return if (isAMines) "x" else if (hintsValue == 0) field else hintsValue.toString()
        }

    fun mark(minesNumber: Int): Int {
        if (hintsValue != 0 && explored) {
            println("There is a number here!")
            return minesNumber
        }
        if (marked) {
            marked = false
            return if (isAMines) minesNumber + 1 else minesNumber -1
        }
        marked = true
        return if (isAMines) minesNumber-1 else minesNumber +1
    }

    fun addMinesSide() {
        if (!isAMines) {
            hintsValue += 1
        }
    }
}

fun printField(field: MutableList<MutableList<Ground>>, masked: Boolean = true) {
// Print the field
    println(" │123456789│")
    println("—│—————————│")
    for (x in 0..<field.size) {
        print("${x+1}|")
        for (y in 0..<field[x].size) {
            if (masked) print(field[x][y].stringPrinted) else   print(field[x][y].stringPrintedUnmasked)
        }
        print("|")
        println()
    }
    println("—│—————————│")
}

