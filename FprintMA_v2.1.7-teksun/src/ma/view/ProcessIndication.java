package ma.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import ma.fprint.R;

public class ProcessIndication extends View {

	private Paint mPaint = null;

	private int mWidth = 0;
	private int mHeight = 0;

	private int CIRCLE_COUNT = 4;

	private int CIRCLE_RADIUS = 7;

	private Paint mCirclePaint = null;

	private int mProcessIndex = 0;

	private int mStartColor = 0;
	private int mEndColor = 0;

	public int mCircleColor = Color.GRAY;//Color.WHITE;
	private boolean mEnabelGradient = false;
	public ProcessIndication(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public ProcessIndication(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ProcessIndication(Context context) {
		super(context);
		init(context, null);
	}

	public void init(Context context, AttributeSet attrs) {
		mPaint = new Paint();

		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStrokeWidth(5);
		mPaint.setColor(mCircleColor);
		mPaint.setStyle(Style.FILL);

		// mCirclePaint
		mCirclePaint = new Paint();
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setDither(true);
		mCirclePaint.setStrokeWidth(2);
		mCirclePaint.setStyle(Style.STROKE);

		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ProcessIndication);
		mProcessIndex = array.getInt(0, 0);

		mStartColor = array.getColor(1, 0xFFDEFFFD);
		mEndColor = array.getColor(2, 0xFF00867F);
	}

	public void enableGradient(boolean able){
		mEnabelGradient = able;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//Log.e("wxp", "onMeasure");
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		if (mEnabelGradient) {
			LinearGradient mShader = new LinearGradient(0, 0, getMeasuredWidth(),
					0, new int[] { mStartColor, mEndColor, mStartColor },
					new float[] { 0, 1.0f, 1.0f }, TileMode.CLAMP);
			mPaint.setShader(mShader);
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		drawAllCircle(mProcessIndex, canvas);

	}

	private boolean mEnableNumberIndex = false;
	public void enableNumberIndex(boolean able) {
		mEnableNumberIndex = able;
	}
	public void drawAllCircle(int index, Canvas canvas) {
		int undoCount = CIRCLE_COUNT - index;
		int dx = (mWidth - CIRCLE_RADIUS * 2 * CIRCLE_COUNT) / (CIRCLE_COUNT+1);
		int dotHeight = mHeight / 2;
		
		Paint numberPaint = new Paint();
		numberPaint.setAntiAlias(true);
		numberPaint.setDither(true);
		numberPaint.setStrokeWidth(5);
		numberPaint.setColor(Color.BLACK);
		numberPaint.setStyle(Style.FILL);
		mPaint.setColor(mCircleColor);
		for (int i = 0; i < index; i++) {
			int leftdx = dx * (i + 1) + CIRCLE_RADIUS * 2 * i + CIRCLE_RADIUS;
			canvas.drawCircle(leftdx, dotHeight, CIRCLE_RADIUS, mPaint);


		}

		if (undoCount > 0) {
			mCirclePaint.setColor(mCircleColor);
			for (int i = index; i < CIRCLE_COUNT; i++) {
				int leftdx = dx * (i + 1) + CIRCLE_RADIUS * 2 * i
						+ CIRCLE_RADIUS;
				canvas.drawCircle(leftdx, dotHeight, CIRCLE_RADIUS,
						mCirclePaint);
			}
		}
	}

	public void setIndex(int index) {
		mProcessIndex = index;
		invalidate();
	}
}
