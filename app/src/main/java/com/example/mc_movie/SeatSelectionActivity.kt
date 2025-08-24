package com.example.mc_movie

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class SeatSelectionActivity : AppCompatActivity() {

    private lateinit var gridSeats: GridLayout
    private lateinit var btnConfirm: Button
    private val selectedSeats = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)

        // UI references
        gridSeats = findViewById(R.id.gridSeats)
        btnConfirm = findViewById(R.id.btnConfirm)

        // Get data from Intent

        val cinemaId = intent.getStringExtra("cinemaId") ?: return
        val movieId = intent.getStringExtra("movieId") ?: return
        val date = intent.getStringExtra("date") ?: return
        val showtimeId = intent.getStringExtra("showtimeId") ?: return
        val availableSeats = intent.getIntExtra("availableSeats", 0)
        val bookedSeats = intent.getSerializableExtra("bookedSeats") as? HashMap<String, List<String>> ?: hashMapOf()

        // Optional: Get username (if passed)
        val username = intent.getStringExtra("username") ?: "guest"

        // Generate seat buttons
        generateSeats(availableSeats, bookedSeats)

        // Confirm booking
        btnConfirm.setOnClickListener {
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = FirebaseDatabase.getInstance()
                .getReference("cinemas/$cinemaId/movies/$movieId/showtimes/$date/$showtimeId/bookedSeats/$username")

            ref.setValue(selectedSeats).addOnSuccessListener {
                Toast.makeText(this, "Booked: $selectedSeats", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateSeats(availableSeats: Int, booked: Map<String, List<String>>) {
        val bookedSeatSet = booked.values.flatten().toSet()
        val columns = 10
        val totalRows = (availableSeats + columns - 1) / columns  // ceil division

        gridSeats.removeAllViews()
        gridSeats.columnCount = columns

        var count = 0

        for (rowIndex in 0 until totalRows) {
            val rowChar = ('A' + rowIndex).toString()
            for (colIndex in 1..columns) {
                if (count >= availableSeats) return

                val seatId = "$rowChar$colIndex"
                val button = Button(this).apply {
                    text = seatId
                    textSize = 12f
                    setPadding(0, 0, 0, 0)

                    if (seatId in bookedSeatSet) {
                        setBackgroundColor(Color.GRAY)
                        isEnabled = false
                    } else {
                        setBackgroundColor(Color.LTGRAY)
                        setOnClickListener {
                            if (selectedSeats.contains(seatId)) {
                                selectedSeats.remove(seatId)
                                setBackgroundColor(Color.LTGRAY)
                            } else {
                                selectedSeats.add(seatId)
                                setBackgroundColor(Color.GREEN)
                            }
                        }
                    }

                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(6, 6, 6, 6)
                    }
                }

                gridSeats.addView(button)
                count++
            }
        }
    }



}
