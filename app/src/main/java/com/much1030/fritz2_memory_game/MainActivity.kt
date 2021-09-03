package com.much1030.fritz2_memory_game

import android.animation.ArgbEvaluator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.much1030.fritz2_memory_game.models.BoardSize
import com.much1030.fritz2_memory_game.models.MemoryGame
import com.much1030.fritz2_memory_game.utils.EXTRA_BOARD_SIZE

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 248
    }

    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {

        /*  fritz2-Alternative für Main Layout inkl. Menu
            (funktioniert nur unter Einbindung der fritz2-libraries und ohne die Android Studio xml-Layout-Dateien;
            Veknüpfung zu Methoden nicht vollständig)

        flexBox({
            direction { column }
        }) {
            box { + "fritz2 memory game"
                  + menu {
                        entry {
                            icon { refresh }
                        }
                        entry {
                            text("Choose new size")
                            events {
                                clicks handledBy modal { // Code zu Alertfenster mit Größenauswahl }
                            }
                        }
                        entry {
                            text("Create custom game")
                            events {
                                clicks handledBy modal { // Code zu Alertfenster für neues Spiel }
                            }
                        }
                    }
            }
            box { + setupBoard() }
            box {
                flexBox({
                    direction { row }
                }) {
                    box { +"Easy: 4 x 2" }
                    box { +"Pairs: 0/4" }
                }
            }
        }


         */

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog("Quit your current game?", null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {

        /*  fritz2-Alternative für Modal "Create custom game"
            (funktioniert nur unter Einbindung der fritz2-libraries und ohne die Android Studio xml-Layout-Dateien;
            Veknüpfung zu Methoden nicht vollständig)

        clickButton {
            text("Create custom game")
        } handledBy modal {
            placement { center }
            content {
                p { +"Create your own memory board" }
                val choices = listOf("Easy (4 x 2)", "Medium (6 x 3)", “Hard (6 x 4)”)
                radioGroup(value = storeOf(""), items = choices) {
                    size { normal }
                }
                clickButton {
                    id { create_custom_game_cancel }
                    text("Cancel")
                    variant { outline }
                }
                clickButton {
                    id { create_custom_game_ok }
                    text("OK")
                    variant { outline }
                }
            }
        }


         */

        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroupSize)
        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener {
            // Set a new value for the board size
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // Navigate to a new activity
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {

        /*  fritz2-Alternative für Modal "Choose new size"
            (funktioniert nur unter Einbindung der fritz2-libraries und ohne die Android Studio xml-Layout-Dateien;
            Veknüpfung zu Methoden nicht vollständig)

        clickButton {
            text("Choose new size")
        } handledBy modal {
            placement { center }
            content {
                p { +"Choose new size" }
                val choices = listOf("Easy (4 x 2)", "Medium (6 x 3)", “Hard (6 x 4)”)
                radioGroup(value = storeOf(""), items = choices) {
                    size { normal }
                }
                clickButton {
                    id { choose_new_size_cancel }
                    text("Cancel")
                    variant { outline }
                }
                clickButton {
                    id { choose_new_size_ok }
                    text("OK")
                    variant { outline }
                }
            }
        }


         */

        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroupSize)
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            // Set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setupBoard() {
        memoryGame = MemoryGame(boardSize)
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0/4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0/9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard: 6 x 4"
                tvNumPairs.text = "Pairs: 0/12"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener {
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        // Error handling:
        if (memoryGame.haveWonGame()) {
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Actually flip the card
        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()) {

                /* fritz2-Alternative für Toast "You won! Congratulations."
                (funktioniert nur unter Einbindung der fritz2-libraries und ohne die Android Studio xml-Layout-Dateien;
                Veknüpfung zu Methoden nicht vollständig)

                handledBy toast {
                    placement { bottom }
                    content { "You won! Congratulations." }
                    duration { 5000 }
                }


                 */

                Snackbar.make(clRoot, "You won! Congratulations.", Snackbar.LENGTH_LONG).show()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}