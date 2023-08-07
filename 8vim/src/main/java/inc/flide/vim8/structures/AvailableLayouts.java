package inc.flide.vim8.structures;

import static inc.flide.vim8.models.AppPrefsKt.appPreferenceModel;
import static inc.flide.vim8.models.LayoutKt.embeddedLayouts;

import android.content.Context;
import android.net.Uri;
import arrow.core.Option;
import inc.flide.vim8.keyboardactionlisteners.MainKeypadActionListener;
import inc.flide.vim8.models.AppPrefs;
import inc.flide.vim8.models.CustomLayout;
import inc.flide.vim8.models.EmbeddedLayout;
import inc.flide.vim8.models.KeyboardData;
import inc.flide.vim8.models.Layout;
import inc.flide.vim8.models.LayoutKt;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AvailableLayouts {
    private static AvailableLayouts singleton;
    private final AppPrefs prefs;
    private final Map<String, EmbeddedLayout> embeddedLayoutsWithName;
    private final int defaultIndex;
    private final List<EmbeddedLayout> embeddedLayouts;
    private List<Layout<?>> layouts;
    private LinkedHashMap<String, CustomLayout> customLayoutsWithName;
    private List<CustomLayout> customLayouts;
    private Set<String> displayNames;
    private int index = -1;

    private AvailableLayouts(Context context) {
        prefs = appPreferenceModel().java();

        embeddedLayoutsWithName = embeddedLayouts(context);
        embeddedLayouts = new ArrayList<>(embeddedLayoutsWithName.values());
        defaultIndex = embeddedLayouts.indexOf((EmbeddedLayout) prefs.getLayout().getCurrent().getDefault());

        reloadCustomLayouts(context);
        prefs.getLayout().getCustom().getHistory().observe(newValue -> reloadCustomLayouts(context));
    }

    public static void initialize(Context context) {
        if (singleton == null) {
            singleton = new AvailableLayouts(context);
        }
    }

    public static AvailableLayouts getInstance() {
        assert singleton != null;
        return singleton;
    }

    private void reloadCustomLayouts(Context context) {
        listCustomLayoutHistory(context);
        updateDisplayNames();
        findIndex();
    }

    public void selectLayout(Context context, int which) {
        int embeddedLayoutSize = embeddedLayoutsWithName.size();
        Option<Layout<?>> layoutOption;
        if (which < embeddedLayoutSize) {
            layoutOption = Option.fromNullable(embeddedLayouts.get(which));
        } else {
            layoutOption = Option.fromNullable(customLayouts.get(which - embeddedLayoutSize));
        }
        layoutOption.onSome(layout -> {
            prefs.getLayout().getCurrent().set(layout, true);
            MainKeypadActionListener.rebuildKeyboardData(LayoutKt.loadKeyboardData(layout, context).getOrNull());
            index = which;
            return null;
        });
    }

    public Set<String> getDisplayNames() {
        return displayNames;
    }

    public int getIndex() {
        return index;
    }

    private void listCustomLayoutHistory(Context context) {
        LinkedHashSet<String> uris = new LinkedHashSet<>(prefs.getLayout().getCustom().getHistory().get());

        customLayoutsWithName = new LinkedHashMap<>();
        LinkedHashSet<String> newUris = new LinkedHashSet<>();
        for (String customLayoutUriString : uris) {
            Uri customLayoutUri = Uri.parse(customLayoutUriString);
            CustomLayout layout = new CustomLayout(customLayoutUri);
            KeyboardData keyboardData = LayoutKt
                    .loadKeyboardData(layout, context)
                    .getOrNull();
            if (keyboardData == null || keyboardData.getTotalLayers() == 0) {
                continue;
            }
            customLayoutsWithName.put(keyboardData.toString(), layout);
            newUris.add(customLayoutUriString);
        }
        prefs.getLayout().getCustom().getHistory().set(newUris, true);
        customLayouts = new ArrayList<>(customLayoutsWithName.values());
    }

    private void findIndex() {
        index = layouts.indexOf(prefs.getLayout().getCurrent().get());
        if (index == -1) {
            index = defaultIndex;
        }
    }

    private void updateDisplayNames() {
        displayNames = new LinkedHashSet<>(embeddedLayoutsWithName.keySet());
        displayNames.addAll(customLayoutsWithName.keySet());
        layouts = new ArrayList<>(embeddedLayouts);
        layouts.addAll(customLayouts);
    }
}
