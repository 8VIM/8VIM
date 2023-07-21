package inc.flide.vim8.utils;

import android.content.Context;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;
import inc.flide.vim8.R;
import java.util.ArrayList;
import java.util.Collection;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

public final class DialogsHelper {
    private DialogsHelper() {

    }

    public static MaterialDialog createMaterialDialog(Context context, int titleRes, int messageRes,
                                                      int positiveButtonRes,
                                                      Function1<MaterialDialog, Unit> callback) {
        return new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR()).title(titleRes, null)
                .message(messageRes, null, null).cancelable(false)
                .cancelOnTouchOutside(false)
                .positiveButton(positiveButtonRes, null, callback)
                .negativeButton(R.string.activate_ime_dialog_negative_button_text, null, null);
    }

    public static MaterialDialog createItemsChoice(Context context, int titleRes,
                                                   Collection<String> items, int selectedIndex,
                                                   Function3<MaterialDialog, Integer, CharSequence, Unit> callback) {
        return DialogSingleChoiceExtKt.listItemsSingleChoice(
                new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR()).title(titleRes, null)
                        .positiveButton(R.string.generic_okay_text, null, null)
                        .negativeButton(R.string.generic_cancel_text, null, null), null,
                new ArrayList<>(items), null, selectedIndex, true, -1, -1, callback);
    }
}
