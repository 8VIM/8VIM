package inc.flide.vim8.ui.views.mainkeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import inc.flide.vim8.MainInputMethodService;
import inc.flide.vim8.R;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.preferences.SharedPreferenceHelper;
import inc.flide.vim8.structures.AvailableLayouts;
import inc.flide.vim8.structures.CustomKeycode;
import inc.flide.vim8.utils.ColorsHelper;
import java.util.ArrayList;
import java.util.List;

public class LanguagesSelectionView extends ConstraintLayout {
    public LanguagesSelectionView(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public LanguagesSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public LanguagesSelectionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        MainKeypadActionListener actionListener = new MainKeypadActionListener((MainInputMethodService) context, this);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RecyclerView view =
                (RecyclerView) layoutInflater.inflate(R.layout.languages_selection_view, this, false);
        view.setLayoutManager(new LinearLayoutManager(context));
        view.setAdapter(new ListLanguageAdapter(actionListener, context, getResources()));
        addView(view);
    }

    public static class ListLanguageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_SEPARATOR = 1;
        private static final int VIEW_TYPE_NORMAL = 2;
        private final AvailableLayouts availableLayouts;
        private final MainKeypadActionListener actionListener;
        private final Context context;
        private final LayoutInflater layoutInflater;
        private final Resources resources;
        private List<String> displayNames;

        public ListLanguageAdapter(MainKeypadActionListener actionListener, Context context, Resources resources) {
            this.context = context;
            this.resources = resources;
            this.actionListener = actionListener;
            layoutInflater = LayoutInflater.from(context);
            this.availableLayouts = AvailableLayouts.getInstance(context, resources);
            displayNames = new ArrayList<>(availableLayouts.getDisplayNames());
            SharedPreferenceHelper
                    .getInstance(context)
                    .addListener(this::updateList, context.getString(R.string.pref_custom_keyboard_layout_history))
                    .addListener(() -> notifyItemRangeChanged(0, getItemCount()),
                            context.getString(R.string.pref_board_fg_color_key),
                            context.getString(R.string.pref_color_mode_key));
            availableLayouts.onChange((previous, index) -> {
                previous += previous >= availableLayouts.getEmbeddedLayoutSize() ? 4 : 2;
                index += index >= availableLayouts.getEmbeddedLayoutSize() ? 4 : 2;
                notifyItemChanged(previous);
                notifyItemChanged(index);
            });
        }

        private void updateList() {
            availableLayouts.reloadCustomLayouts();
            displayNames = new ArrayList<>(availableLayouts.getDisplayNames());
            notifyItemInserted(availableLayouts.getEmbeddedLayoutSize());
        }

        @Override
        public int getItemViewType(int position) {
            int embeddedLayoutSize = availableLayouts.getEmbeddedLayoutSize();
            if (position == 0 || position == embeddedLayoutSize + 2) {
                return VIEW_TYPE_HEADER;
            } else if (position == 1 || position == embeddedLayoutSize + 3) {
                return VIEW_TYPE_SEPARATOR;
            }
            return VIEW_TYPE_NORMAL;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    return new HeaderViewHolder(layoutInflater, parent);
                case VIEW_TYPE_SEPARATOR:
                    return new SeparatorViewHolder(layoutInflater, parent);
                default:
                    return LanguageItemViewHolder.create(layoutInflater, parent);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int tintColor = ColorsHelper.getThemeColor(context, R.attr.colorOnSurface, R.string.pref_board_fg_color_key,
                    R.color.defaultBoardFg);
            switch (getItemViewType(position)) {
                case VIEW_TYPE_HEADER:
                    TextView header = (TextView) holder.itemView;
                    header.setTextColor(tintColor);
                    header.setText(position == 0 ? R.string.embedded_layouts_label : R.string.custom_layouts_label);
                    break;
                case VIEW_TYPE_NORMAL:
                    LanguageItemViewHolder vh = (LanguageItemViewHolder) holder;
                    int index = getIndex(position);
                    vh.text.setText(displayNames.get(index));
                    vh.icon.setColorFilter(tintColor);
                    vh.text.setTextColor(tintColor);
                    holder.itemView.setOnClickListener(
                            view -> {
                                availableLayouts.selectLayout(context, resources, index);
                                actionListener.handleInputKey(CustomKeycode.SWITCH_TO_MAIN_KEYPAD.getKeyCode(), 0);
                            });
                    if (index == availableLayouts.getIndex()) {
                        vh.icon.setVisibility(View.VISIBLE);
                        holder.itemView.setClickable(false);
                    } else {
                        holder.itemView.setClickable(true);
                    }
                    break;
                default:
            }
        }

        private int getIndex(int position) {
            return (position > availableLayouts.getEmbeddedLayoutSize() + 3) ? position - 4 : position - 2;
        }

        @Override
        public int getItemCount() {
            return availableLayouts.getDisplayNames().size() + 2 + (availableLayouts.hasCustomLayouts() ? 2 : 0);
        }
    }

    private static class LanguageItemViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView text;

        public LanguageItemViewHolder(@NonNull View view) {
            super(view);
            this.icon = itemView.findViewById(R.id.mtrl_list_item_icon);
            this.text = itemView.findViewById(R.id.mtrl_list_item_text);
        }


        @NonNull
        public static LanguageItemViewHolder create(LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
            return new LanguageItemViewHolder(layoutInflater
                    .inflate(R.layout.material_list_item_single_line, parent, false));
        }
    }

    private static class SeparatorViewHolder extends RecyclerView.ViewHolder {

        public SeparatorViewHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.design_navigation_item_separator, parent, false));
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.design_language_header, parent, false));
        }
    }
}
