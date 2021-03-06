package net.blumia.droid

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

import net.blumia.pcm.privatecloudmusic.R


/**
 * TODO: document your custom view class.
 */
class AvatarView : View {
    private var mTextString: String? = context.getString(R.string.placeholder_short)
    private var mFillColor = ContextCompat.getColor(context, R.color.colorAccent)
    private var mTextColor = ContextCompat.getColor(context, R.color.colorPrimaryText)

    private var mTextPaint: TextPaint? = null
    private var mTextWidth: Float = 0.toFloat()
    private var mTextHeight: Float = 0.toFloat()

    private var mBitmap: Bitmap? = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_menu_share)
    private var mBitmapPaint: Paint? = Paint()
    private var mBitmapShader: BitmapShader? = null

    var bitmap: Bitmap?
        get() = mBitmap
        set(theBitmap) {
            mBitmap = theBitmap
            invalidateTextPaintAndMeasurements()
        }

    var textString: String?
        get() = mTextString
        set(theString) {
            mTextString = theString
            invalidateTextPaintAndMeasurements()
        }

    var fillColor: Int
        get() = mFillColor
        set(theColor) {
            mFillColor = theColor
            invalidateTextPaintAndMeasurements()
        }

    var textColor: Int
        get() = mTextColor
        set(theColor) {
            mTextColor = theColor
            invalidateTextPaintAndMeasurements()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.AvatarView, defStyle, 0)

        mTextString = a.getString(
                R.styleable.AvatarView_av_textString)
        mFillColor = a.getColor(
                R.styleable.AvatarView_av_fillColor,
                mFillColor)
        mTextColor = a.getColor(
                R.styleable.AvatarView_av_textColor,
                mTextColor)
        mBitmapPaint?.color = a.getColor(
                R.styleable.AvatarView_av_fillColor,
                mFillColor)
/*
        if (a.hasValue(R.styleable.AvatarView_exampleDrawable)) {
            exampleDrawable = a.getDrawable(
                    R.styleable.AvatarView_exampleDrawable)
            exampleDrawable!!.callback = this
        }
*/
        a.recycle()

        // Set up a default TextPaint object
        mTextPaint = TextPaint()
        mTextPaint!!.flags = Paint.ANTI_ALIAS_FLAG
        mTextPaint!!.textAlign = Paint.Align.LEFT

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        //val fontMetrics = mTextPaint!!.fontMetrics
        mTextPaint!!.color = mTextColor
        mTextPaint!!.textSize = 20f * resources.displayMetrics.density

        val rect = Rect()
        mTextPaint!!.getTextBounds(textString, 0, textString!!.length, rect)

        mTextWidth = rect.width().toFloat()//mTextPaint!!.measureText(mTextString)
        mTextHeight = rect.height().toFloat()//fontMetrics.bottom
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        //canvas.drawColor(Color.BLUE);
        val circleRadius = resources.displayMetrics.density * 60f / 2
        mBitmapPaint?.let { canvas.drawCircle(circleRadius, circleRadius, circleRadius, it) };

        // Draw the text.
        canvas.drawText(mTextString!!,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint!!)
    }
}
