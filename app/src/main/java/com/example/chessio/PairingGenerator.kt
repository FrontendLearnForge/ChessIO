package com.example.chessio

import kotlin.math.abs

class PairingGenerator {

    // Основная функция для генерации пар
    fun generatePairings(players: List<Player>, roundNumber: Int, tournamentFormat:String): List<Pair<Player, Player>> {
        return if (tournamentFormat == "Круговая система") {
//            var size = players.size
//            bergerTable(size)
            generateFirstRoundPairings(players)
        }
            else {
                if (roundNumber == 1) {
                generateFirstRoundPairings(players)
            } else {
                generateNextRoundPairings(players)
            }
        }
    }

    // Жеребьевка первого тура
    private fun generateFirstRoundPairings(players: List<Player>): List<Pair<Player, Player>> {
        val sortedPlayers = players.sortedByDescending { it.rate }
        val half = sortedPlayers.size / 2
        val topHalf = sortedPlayers.take(half)
        val bottomHalf = sortedPlayers.drop(half)

        val pairings = mutableListOf<Pair<Player, Player>>()

        for (i in topHalf.indices) {
            if (i < bottomHalf.size) {
                val (white, black) = assignColors(topHalf[i], bottomHalf[i])
                pairings.add(white to black)
                black.id?.let { white.prevOpponents.add(it) }
                white.id?.let { black.prevOpponents.add(it) }
            } else {
                // Нечетное количество игроков - последний получает очко без игры
                topHalf[i].points += 1f
            }
        }

        return pairings
    }

    // Жеребьевка последующих туров
    private fun generateNextRoundPairings(players: List<Player>): List<Pair<Player, Player>> {
        val sortedPlayers = players
            .sortedWith(compareByDescending<Player> { it.points }.thenByDescending { it.rate })

        // Группируем по очкам
        val groups = sortedPlayers.groupBy { it.points }.values.toList()
        val pairings = mutableListOf<Pair<Player, Player>>()
        val pairedIds = mutableSetOf<Int>()

        for (group in groups) {
            if (group.size % 2 != 0) {
                // Для нечетной группы перемещаем одного игрока в следующую группу
                val playerToMove = findPlayerToMove(group, groups)
                playerToMove?.let {
                    group.toMutableList().remove(it)
                    groups.getOrNull(groups.indexOf(group) + 1)?.toMutableList()?.add(it)
                }
            }

            val sortedGroup = group.sortedByDescending { it.rate }
            val half = (sortedGroup.size + 1) / 2
            val topHalf = sortedGroup.take(half)
            val bottomHalf = sortedGroup.drop(half)

            for (i in topHalf.indices) {
                if (i < bottomHalf.size) {
                    val topPlayer = topHalf[i]
                    val bottomPlayer = findSuitableOpponent(topPlayer, bottomHalf, pairedIds)

                    bottomPlayer?.let {
                        val (white, black) = assignColors(topPlayer, it)
                        pairings.add(white to black)
                        white.id?.let { it1 -> pairedIds.add(it1) }
                        black.id?.let { it1 -> pairedIds.add(it1) }
                        black.id?.let { it1 -> white.prevOpponents.add(it1) }
                        white.id?.let { it1 -> black.prevOpponents.add(it1) }
                    } ?: run {
                        // Если не нашли подходящего соперника, даем очко без игры
                        topPlayer.points += 1f
                    }
                }
            }
        }

        return pairings
    }

    private fun findPlayerToMove(group: List<Player>, allGroups: List<List<Player>>): Player? {
        // Ищем игрока с наименьшим рейтингом в группе
        return group.minByOrNull { it.rate }
    }

    private fun findSuitableOpponent(
        player: Player,
        candidates: List<Player>,
        pairedIds: Set<Int>
    ): Player? {
        return candidates
            .filter { it.id !in pairedIds }
            .filter { it.id !in player.prevOpponents }
            .minByOrNull { abs(it.rate - player.rate) }
    }

    private fun assignColors(player1: Player, player2: Player): Pair<Player, Player> {
        return when {
            player1.colorBalance < player2.colorBalance -> player1 to player2
            player1.colorBalance > player2.colorBalance -> player2 to player1
            player1.rate >= player2.rate -> player1 to player2
            else -> player2 to player1
        }.also { (white, black) ->
            white.colorBalance += 1
            black.colorBalance -= 1
        }
    }
}