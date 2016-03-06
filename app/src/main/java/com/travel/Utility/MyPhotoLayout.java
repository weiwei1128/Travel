package com.travel.Utility;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.travel.R;

/**
 * Created by wei on 2016/2/4.
 */
public class MyPhotoLayout extends LinearLayout {
    private ImageView imageView;
    private TextView textView;

    public MyPhotoLayout(Context context) {
        super(context);
        imageView = new ImageView(context);
        textView = new TextView(context);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        imageView.setLayoutParams(layoutParams);
        layoutParams.setMargins(-100, 150, 0, 0);
//        imageView.setLayoutParams(layoutParams);
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setLayoutParams(layoutParams);

        this.setGravity(Gravity.CENTER);
        this.setClickable(true);
        this.addView(imageView);
        this.addView(textView);
    }

    public void setText(int resid) {
        textView.setText(resid);
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setIcon(int resid) {
        imageView.setImageDrawable(getResources().getDrawable(resid));
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getTextView() {
        return textView;
    }
}
