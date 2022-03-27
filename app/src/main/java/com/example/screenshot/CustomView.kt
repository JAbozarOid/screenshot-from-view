package com.example.screenshot

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet

class CustomView(context: Context, attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatImageView(
        context, attrs
    ) {

    private var mAttributes: TypedArray =
        context.obtainStyledAttributes(attrs, R.styleable.AlertIconCustomView)

    // *** alert Icon
    private var isClicked = false

    private var alarmIconViewState: AlarmIconViewState = AlarmIconViewState.DEFAULT

    private var mDefaultAlertIcon: Drawable? =
        mAttributes.getDrawable(R.styleable.AlertIconCustomView_alarmDefault)

    private var mClickedAlertIcon: Drawable? =
        mAttributes.getDrawable(R.styleable.AlertIconCustomView_alarmClicked)

    private var mDefaultAlarmBackground: Drawable? =
        mAttributes.getDrawable(R.styleable.AlertIconCustomView_alarmBGDefault)

    private var mClickedAlarmBackground: Drawable? =
        mAttributes.getDrawable(R.styleable.AlertIconCustomView_alarmBGClicked)
    // *** alert icon


    init {
        setImageDrawable(mDefaultAlertIcon)
    }

    fun onClick(): Boolean {
        return if (alarmIconViewState == AlarmIconViewState.DEFAULT)
            setAlarmIcon(true)
        else
            setAlarmIcon(false)
    }


    fun setAlarmIcon(isAlarmClicked: Boolean): Boolean {
        if (isAlarmClicked) {
            alarmIconViewState = AlarmIconViewState.CLICKED
            setImageDrawable(mClickedAlertIcon)
            background = mClickedAlarmBackground
        } else {
            alarmIconViewState = AlarmIconViewState.DEFAULT
            setImageDrawable(mDefaultAlertIcon)
            background = mDefaultAlarmBackground
        }
        isClicked = isAlarmClicked
        return isClicked
    }

}

enum class AlarmIconViewState {
    DEFAULT, CLICKED
}

