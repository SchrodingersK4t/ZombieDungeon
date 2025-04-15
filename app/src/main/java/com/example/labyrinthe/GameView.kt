package com.example.labyrinthe

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    //  Taille de la map
    private val gridWidth = 5   // nombre de colonnes
    private val gridHeight = 7 // nombre de lignes

    //  Position et direction du joueur
    private var playerX = gridWidth / 2
    private var playerY = gridHeight - 1
    private var direction = 3 // 0: droite, 1: bas, 2: gauche, 3: haut

    private var tileSize = 0

    //  Carte des tuiles (0 = sol, 1 = mur)
    private val tileMap = Array(gridHeight) { y ->
        IntArray(gridWidth) { x -> 0 } // par défaut, tout est du sol
    }

    private val paintTile = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val paintWall = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    private val paintPlayer = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    init {
        holder.addCallback(this)

        // Exemple de murs (blocage au milieu de la grille)
        tileMap[3][2] = 1
        tileMap[4][2] = 1
        tileMap[5][2] = 1
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        tileSize = min(width / gridWidth, height / gridHeight)
        render()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        tileSize = min(width / gridWidth, height / gridHeight)
        render()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    fun render() {
        val canvas = holder.lockCanvas()
        canvas?.let {
            it.drawColor(Color.BLACK)

            // Dessin des tuiles (sol + murs)
            for (y in 0 until gridHeight) {
                for (x in 0 until gridWidth) {
                    val left = x * tileSize
                    val top = y * tileSize
                    val right = left + tileSize
                    val bottom = top + tileSize

                    if (tileMap[y][x] == 1) {
                        it.drawRect(
                            left.toFloat(), top.toFloat(),
                            right.toFloat(), bottom.toFloat(), paintWall
                        )
                    }

                    it.drawRect(
                        left.toFloat(), top.toFloat(),
                        right.toFloat(), bottom.toFloat(), paintTile
                    )
                }
            }

            drawPlayer(it)

            holder.unlockCanvasAndPost(it)
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        val centerX = (playerX * tileSize + tileSize / 2).toFloat()
        val centerY = (playerY * tileSize + tileSize / 2).toFloat()

        val length = tileSize * 0.45f // pointe
        val base = tileSize * 0.3f    // largeur arrière

        val angleRad = when (direction) {
            0 -> 0.0
            1 -> PI / 2
            2 -> PI
            3 -> -PI / 2
            else -> 0.0
        }

        val tipX = centerX + cos(angleRad).toFloat() * length
        val tipY = centerY + sin(angleRad).toFloat() * length

        val leftBaseX = centerX + cos(angleRad + 5 * PI / 6).toFloat() * base
        val leftBaseY = centerY + sin(angleRad + 5 * PI / 6).toFloat() * base

        val rightBaseX = centerX + cos(angleRad - 5 * PI / 6).toFloat() * base
        val rightBaseY = centerY + sin(angleRad - 5 * PI / 6).toFloat() * base

        val path = Path().apply {
            moveTo(tipX, tipY)
            lineTo(leftBaseX, leftBaseY)
            lineTo(rightBaseX, rightBaseY)
            close()
        }

        canvas.drawPath(path, paintPlayer)
    }

    fun moveForward() {
        val (dx, dy) = directionVector()
        tryMove(playerX + dx, playerY + dy)
    }

    fun moveBackward() {
        val (dx, dy) = directionVector()
        tryMove(playerX - dx, playerY - dy)
    }

    fun rotateRight() {
        direction = (direction + 1) % 4
        render()
    }

    fun rotateLeft() {
        direction = (direction + 3) % 4
        render()
    }

    private fun directionVector(): Pair<Int, Int> {
        return when (direction) {
            0 -> Pair(1, 0)   // droite
            1 -> Pair(0, 1)   // bas
            2 -> Pair(-1, 0)  // gauche
            3 -> Pair(0, -1)  // haut
            else -> Pair(0, 0)
        }
    }

    private fun tryMove(newX: Int, newY: Int) {
        if (
            newX in 0 until gridWidth &&
            newY in 0 until gridHeight &&
            tileMap[newY][newX] == 0 // mur = bloqué
        ) {
            playerX = newX
            playerY = newY
        }
        render()
    }
}


