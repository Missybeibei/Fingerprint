package ma.library;

import android.os.Parcel;
import android.os.Parcelable;

public class FingerGestureDetector implements Parcelable{
    private  OnFingerGestureListener mFingerListener; 
	
	
	public OnFingerGestureListener getFingerListener() {
		return mFingerListener;
	}

    public FingerGestureDetector() {
		
	}

	
	private FingerGestureDetector(Parcel source) {  
        readFromParcel(source);  
    }
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
	public static final Parcelable.Creator<FingerGestureDetector> CREATOR = new Parcelable.Creator<FingerGestureDetector>() {  
		  
        @Override  
        public FingerGestureDetector createFromParcel(Parcel source) {  
            return new FingerGestureDetector(source);  
        }  
  
        @Override  
        public FingerGestureDetector[] newArray(int size) {  
            return new FingerGestureDetector[size];  
        }  
    }; 
    
    public void readFromParcel(Parcel source) {  
          
    }
    
    public void addFingerGestureListener(OnFingerGestureListener l){
    	this.mFingerListener = l;
    }

}
