package inc.flide.vim8.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.UserDictionary;
import android.util.Log;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import inc.flide.vim8.R;

public class PredictiveTextHelper {
    private final List<String> englishDictionary;
    private final List<String> personalDictionary;
    private final List<String> suggestedWords;
    private CharSequence textBeforeCursor;
    private String currentWord;

    public PredictiveTextHelper(Context context) {
        englishDictionary = new ArrayList<>();
        personalDictionary = new ArrayList<>();
        suggestedWords = new ArrayList<>();
        textBeforeCursor = "";
        currentWord = "";

        loadDictionaries(context);
    }

    public List<String> getSuggestedWords() {
        return suggestedWords;
    }

    public void setTextBeforeCursor(CharSequence text) {
        textBeforeCursor = text;
        currentWord = extractCurrentWord(textBeforeCursor.toString());
        generateWordSuggestions();
        Log.d("PredictiveText", suggestedWords.toString());
    }

    private void loadDictionaries(Context context) {
        loadUserDictionaryWords(context.getContentResolver());
        loadEnglishDictionaryWords(context);
    }

    private void loadEnglishDictionaryWords(Context context) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.english_dictionary)))) {
            String word;
            while ((word = reader.readLine()) != null) {
                englishDictionary.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadUserDictionaryWords(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(UserDictionary.Words.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            int index = cursor.getColumnIndex(UserDictionary.Words.WORD);
            while (cursor.moveToNext()) {
                String word = cursor.getString(index);
                personalDictionary.add(word);
            }
            cursor.close();
        }
    }

    private String extractCurrentWord(String textBeforeCursor) {
        String[] words = textBeforeCursor.split("\\s+");
        return words.length > 0 ? words[words.length - 1] : "";
    }
    private void generateWordSuggestions() {
        List<String> suggestions = new ArrayList<>();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        for (String word : englishDictionary) {
            if (levenshteinDistance.apply(currentWord, word) <= 2) {
                suggestions.add(word);
            }
        }

        for (String word : personalDictionary) {
            if (levenshteinDistance.apply(currentWord, word) <= 2) {
                suggestions.add(word);
            }
        }

        suggestions.sort(Comparator.comparingInt(word -> levenshteinDistance.apply(currentWord, word)));
        suggestedWords.clear();
        suggestedWords.addAll(suggestions.subList(0, 5));
    }

}
