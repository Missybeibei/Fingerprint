package ma.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import ma.fprint.R;

public class NumberPad extends ViewGroup implements OnClickListener{

	private List<Button> mNumberBtns = new ArrayList<Button>();
	private Button mDeleteBtn = null;
	private Button mNullBtn = null;
	private Context mContext = null;
    private int[] mNumberBg = null;
	
	public NumberPad(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public NumberPad(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumberPad(Context context) {
		this(context, null, 0);
	}

	private int[] mNumberBgs = new int[]{R.drawable.number_0,R.drawable.number_1,R.drawable.number_2,
			R.drawable.number_3,R.drawable.number_4,R.drawable.number_5,R.drawable.number_6,
			R.drawable.number_7,R.drawable.number_8,R.drawable.number_9};
	public void init() {
		mNumberBg = getResources().getIntArray(R.array.number_bg_array);
		Button numberBtn = null;
		//数字1到9
		for (int i = 1; i < 10; i++) {
			numberBtn = new Button(mContext);
			numberBtn.setTextSize(40);
			numberBtn.setBackgroundResource(mNumberBgs[i]);
			numberBtn.setGravity(Gravity.CENTER);
			numberBtn.setTextColor(Color.BLACK);
			//numberBtn.setText(i + "");
			numberBtn.setTag(i);
			mNumberBtns.add(numberBtn);
			addView(numberBtn);
			
		}
		//数字0
		numberBtn = new Button(mContext);
		//numberBtn.setText(0 + "");
		numberBtn.setTextSize(40);
		numberBtn.setBackgroundResource(mNumberBgs[0]);
		numberBtn.setGravity(Gravity.CENTER);
		numberBtn.setTextColor(Color.BLACK);
		mNumberBtns.add(numberBtn);
		numberBtn.setTag(0);
		addView(numberBtn);
		
		//右下角删除按钮
		numberBtn = new Button(mContext);
		numberBtn.setText("");
		numberBtn.setTextSize(40);
		numberBtn.setGravity(Gravity.CENTER);
		numberBtn.setTextColor(Color.BLACK);
		numberBtn.setBackgroundResource(R.drawable.delete_bg);
		addView(numberBtn);
		numberBtn.setTag(-1);
		mNumberBtns.add(numberBtn);
		
		//左下角取消按钮
		numberBtn = new Button(mContext);
		numberBtn.setText("");
		numberBtn.setTextSize(40);
		/*[BIRD_PRESS_FP] yeyunfeng begin*/
		if(true){
		    numberBtn.setBackgroundResource(R.drawable.fp_number_bg);
		}else{
		    numberBtn.setBackgroundResource(R.drawable.number_bg);		
		}
		/*[BIRD_PRESS_FP] yeyunfeng end*/
		numberBtn.setGravity(Gravity.CENTER);
		numberBtn.setTextColor(Color.BLACK);
		addView(numberBtn);
		numberBtn.setTag(-2);
		mNumberBtns.add(numberBtn);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Button childNumberBtn = null;
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int childWidth = width / 3;
		int childHeight = height / 4;
		int toppadding = 0;
		for (int i = 0; i < getChildCount() - 3; i++) {
			int column = i % 3;
			int line = i / 3;
			childNumberBtn = (Button) mNumberBtns.get(i);
		    toppadding = childNumberBtn.getHeight()/2-(int)childNumberBtn.getTextSize()/2;
			childNumberBtn.setPadding(0, toppadding,0 , 0);
			childNumberBtn.setOnClickListener(this);
			childNumberBtn.layout(column * childWidth, line * childHeight, (column + 1) * childWidth, (line + 1) * childHeight);
		}
		//数字0特殊处理
		childNumberBtn = (Button) mNumberBtns.get(getChildCount() - 3);
	    toppadding = childNumberBtn.getHeight()/2 - (int)childNumberBtn.getTextSize()/2;
		childNumberBtn.setPadding(0, toppadding,0 , 0);

		childNumberBtn.layout(1 * childWidth, 3 * childHeight, (1 + 1) * childWidth, (3 + 1) * childHeight);
		childNumberBtn.setOnClickListener(this);
		
		//删除键特殊处理
		mDeleteBtn = (Button) mNumberBtns.get(getChildCount() - 2);
	    toppadding = childNumberBtn.getHeight()/2-(int)childNumberBtn.getTextSize()/2;
	    mDeleteBtn.setPadding(0, toppadding,0 , 0);

	    mDeleteBtn.layout(2 * childWidth, 3 * childHeight, (2 + 1) * childWidth, (3 + 1) * childHeight);
	    mDeleteBtn.setOnClickListener(this);
	    
		//空白键特殊处理
	    mNullBtn = (Button) mNumberBtns.get(getChildCount() - 1);
	    toppadding = childNumberBtn.getHeight()/2-(int)childNumberBtn.getTextSize()/2;
	    mNullBtn.setPadding(0, toppadding,0 , 0);
	    mNullBtn.layout(0, 3 * childHeight, 1 * childWidth, (3 + 1) * childHeight);
	    mNullBtn.setOnClickListener(this);
		
	}

	private boolean mberPadClickable = true;
	
	public void setNumberPadClickable(boolean able) {
		mberPadClickable = able;
	}
	
	@Override
	public void onClick(View v) {
		if (mberPadClickable && mListener != null) {
			mListener.clickNumer((Integer)((Button)v).getTag());			
		}
	}
	
	OnNumberClickListener mListener = null;
	
	public interface OnNumberClickListener{
		public void clickNumer(int number);
	}
	public void setOnNumberClickListener(OnNumberClickListener listener) {
		mListener = listener;
	}

}
