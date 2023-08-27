package inc.flide.vim8.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.card.MaterialCardView;
import inc.flide.vim8.R;

public class MaterialCard extends FrameLayout {
    private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CardView;
    private final String text;
    private ColorStateList backgroundColor;
    private ColorStateList foregroundColor;
    private Drawable icon;

    public MaterialCard(Context context) {
        this(context, null);
    }

    public MaterialCard(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.materialCardViewStyle);
    }

    @SuppressLint("RestrictedApi")
    public MaterialCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.MaterialCard, defStyleAttr, DEF_STYLE_RES);

        if (a.hasValue(R.styleable.MaterialCard_icon)) {
            icon = a.getDrawable(R.styleable.MaterialCard_icon);
        }

        if (a.hasValue(R.styleable.MaterialCard_backgroundColor)) {
            backgroundColor = a.getColorStateList(R.styleable.MaterialCard_backgroundColor);
        }
        if (a.hasValue(R.styleable.MaterialCard_foregroundColor)) {
            foregroundColor = a.getColorStateList(R.styleable.MaterialCard_foregroundColor);
        }
        text = TypedArrayUtils.getString(a, R.styleable.MaterialCard_text, R.styleable.MaterialCard_android_text);
        inflate(context, R.layout.material_card, this);
        setupMaterialCard();
        setupIcon();
        setupText();
        a.recycle();
    }

    private void setupMaterialCard() {
        MaterialCardView materialCardView = findViewById(R.id.card);
        materialCardView.setCardBackgroundColor(backgroundColor);
    }

    private void setupText() {
        TextView view = findViewById(R.id.text);
        view.setText(text);
        view.setTextColor(foregroundColor);
        if (icon == null) {
            view.setPadding(0, 0, 0, 0);
        }
    }

    private void setupIcon() {
        ImageView view = findViewById(R.id.icon);
        if (icon != null) {
            icon = DrawableCompat.wrap(icon).mutate();
            DrawableCompat.setTintList(icon, foregroundColor);
            view.setImageDrawable(icon);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}