package com.aktech.celebrareassignment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var canvasLayout: FrameLayout
    private lateinit var undoBtn: ImageButton
    private lateinit var redoBtn: ImageButton
    private lateinit var fontSpinner: Spinner
    private lateinit var sizeIncreaseBtn: ImageButton
    private lateinit var sizeDecreaseBtn: ImageButton
    private lateinit var fontSizeText: TextView
    private lateinit var boldToggle: ToggleButton
    private lateinit var italicToggle: ToggleButton
    private lateinit var underlineToggle: ToggleButton
    private lateinit var colorBtn: ImageButton
    private lateinit var editTextBtn: ImageButton

    private val undoStack: Stack<Pair<TextView, Pair<Float, Float>>> = Stack()
    private val redoStack: Stack<Pair<TextView, Pair<Float, Float>>> = Stack()

    private lateinit var defaultTextView: TextView
    private var currentSize = 20
    private var currentColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        canvasLayout = findViewById(R.id.canvasLayout)
        undoBtn = findViewById(R.id.undoBtn)
        redoBtn = findViewById(R.id.redoBtn)
        fontSpinner = findViewById(R.id.fontSpinner)
        sizeIncreaseBtn = findViewById(R.id.sizeIncreaseBtn)
        sizeDecreaseBtn = findViewById(R.id.sizeDecreaseBtn)
        fontSizeText = findViewById(R.id.fontSizeText)
        boldToggle = findViewById(R.id.boldToggle)
        italicToggle = findViewById(R.id.italicToggle)
        underlineToggle = findViewById(R.id.underlineToggle)
        colorBtn = findViewById(R.id.colorBtn)
        editTextBtn = findViewById(R.id.editTextBtn)

        defaultTextView = createMovableTextView("Celebrare")
        canvasLayout.addView(defaultTextView)

        fontSizeText.text = currentSize.toString()

        sizeIncreaseBtn.setOnClickListener {
            currentSize++
            fontSizeText.text = currentSize.toString()
            defaultTextView.textSize = currentSize.toFloat()
        }

        sizeDecreaseBtn.setOnClickListener {
            if (currentSize > 8) {
                currentSize--
                fontSizeText.text = currentSize.toString()
                defaultTextView.textSize = currentSize.toFloat()
            }
        }

        boldToggle.setOnCheckedChangeListener { _, _ ->
            applyTextStyle()
        }

        italicToggle.setOnCheckedChangeListener { _, _ ->
            applyTextStyle()
        }

        underlineToggle.setOnCheckedChangeListener { _, _ ->
            if (underlineToggle.isChecked) {
                defaultTextView.paintFlags =
                    defaultTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                defaultTextView.paintFlags =
                    defaultTextView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }

        fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> defaultTextView.typeface = Typeface.SERIF
                    2 -> defaultTextView.typeface = Typeface.MONOSPACE
                    3 -> defaultTextView.typeface = Typeface.SANS_SERIF
                    else -> defaultTextView.typeface = Typeface.DEFAULT
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        colorBtn.setOnClickListener {
            val colorPicker = AmbilWarnaDialog(this, currentColor,
                object : AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {}
                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                        currentColor = color
                        defaultTextView.setTextColor(color)
                    }
                })
            colorPicker.show()
        }

        editTextBtn.setOnClickListener {
            showTextInputDialog()
        }

        undoBtn.setOnClickListener {
            if (undoStack.isNotEmpty()) {
                val (textView, position) = undoStack.pop()
                redoStack.push(Pair(textView, Pair(textView.x, textView.y)))
                textView.x = position.first
                textView.y = position.second
            }
        }

        redoBtn.setOnClickListener {
            if (redoStack.isNotEmpty()) {
                val (textView, position) = redoStack.pop()
                undoStack.push(Pair(textView, Pair(textView.x, textView.y)))
                textView.x = position.first
                textView.y = position.second
            }
        }
    }

    private fun applyTextStyle() {
        val bold = if (boldToggle.isChecked) Typeface.BOLD else Typeface.NORMAL
        val italic = if (italicToggle.isChecked) Typeface.ITALIC else Typeface.NORMAL
        defaultTextView.setTypeface(defaultTextView.typeface, bold or italic)
    }

    private fun createMovableTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.textSize = currentSize.toFloat()
        textView.setTextColor(currentColor)
        textView.x = 100f
        textView.y = 100f
        textView.setOnTouchListener(MovableTouchListener())
        return textView
    }

    private fun showTextInputDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextInput)
        editText.setText(defaultTextView.text.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Text")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                defaultTextView.text = editText.text.toString()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    inner class MovableTouchListener : View.OnTouchListener {
        private var dX = 0f
        private var dY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val textView = view as TextView
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = textView.x - event.rawX
                    dY = textView.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    undoStack.push(Pair(textView, Pair(textView.x, textView.y)))
                    textView.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
            }
            return true
        }
    }
}
