package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent

@Suppress("UNREACHABLE_CODE")
class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

    // Path that stores the drawn path on canvas
    private var mDrawPath: CustomPath? = null

    // Bitmap to hold the canvas where the drawing will take place
    private var mCanvasBitmap: Bitmap? = null

    // Paint object used to describe how paths will be drawn (color, stroke, etc.)
    private var mDrawPaint: Paint? = null

    // Paint object for canvas properties (background, etc.)
    private var mCanvasPaint: Paint? = null

    // Brush size used for drawing
    private var mBrushSize: Float = 0.toFloat()

    // Current color of the drawing brush
    private var color = Color.BLACK

    // Canvas object where the drawing will happen
    private var canvas: Canvas? = null

    // List to store all drawn paths
    private var mPaths = ArrayList<CustomPath>()

    private var mUndoPaths = ArrayList<CustomPath>()

    init {
        // Initialize drawing setup
        setUpDrawing()
    }

    fun onClickUndo(){
        if(mPaths.size > 0)
        {
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }


    // Function to set up drawing properties such as paint styles and brush sizes
    private fun setUpDrawing() {
        mDrawPaint = Paint() // Initialize Paint object for drawing
        mDrawPath = CustomPath(color, mBrushSize) // Initialize custom path with default color and brush size

        // Set up paint properties: color, style, join, cap
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE // Set to stroke mode for drawing outlines
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // Set stroke join type to round
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // Set stroke cap type to round

        // Set up paint for the canvas with dithering to improve quality
        mCanvasPaint = Paint(Paint.DITHER_FLAG)

        // Set default brush size to 20 pixels
//        mBrushSize = 20.toFloat()
    }

    // Called when the view's size is changed, such as on rotation or when initially displayed
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Create a new bitmap based on the new width and height of the view
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        // Create a canvas object with the new bitmap
        canvas = Canvas(mCanvasBitmap!!)
    }

    // Function to draw on the canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the saved bitmap (with previous paths) onto the canvas
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        // Draw each saved path from the mPaths list
        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness // Set stroke width of the paint
            mDrawPaint!!.color = path.color // Set color of the paint
            canvas.drawPath(path, mDrawPaint!!) // Draw the path on the canvas
        }

        // Draw the current path as it's being drawn (not yet added to mPaths)
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness // Set stroke width for the current path
            mDrawPaint!!.color = mDrawPath!!.color // Set color for the current path
            canvas.drawPath(mDrawPath!!, mDrawPaint!!) // Draw the current path
        }
    }

    // Function to handle touch events for drawing
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x // Get the x-coordinate of the touch
        val touchY = event?.y // Get the y-coordinate of the touch

        // Handle the different touch actions (DOWN, MOVE, UP)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> { // When touch starts
                mDrawPath!!.color = color // Set the current color
                mDrawPath!!.brushThickness = mBrushSize // Set the brush thickness

                // Reset the path and move to the touch position
                mDrawPath!!.reset()
                if (touchX != null && touchY != null) {
                    mDrawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> { // When the finger moves
                // Draw line from the last position to the new touch position
                if (touchX != null && touchY != null) {
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> { // When the touch is lifted
                mPaths.add(mDrawPath!!) // Add the path to the list of paths
                mDrawPath = CustomPath(color, mBrushSize) // Create a new path for future drawing
            }
            else -> return false
        }
        invalidate() // Redraw the view to update the canvas
        return true // Return true to indicate the event was handled
    }

    fun setSizeForBrush(newSize: Float) {
        // Convert the new brush size from device-independent pixels (dp) to pixels
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, // Unit type for device-independent pixels (dp)
            newSize, // The new size provided as dp
            resources.displayMetrics // Get the device's display metrics for accurate conversion
        )

        // Set the stroke width of the paint object to the new brush size in pixels
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor:String)
    {
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    // Custom class to define a path with a specific color and brush thickness
    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path() {

    }
}
