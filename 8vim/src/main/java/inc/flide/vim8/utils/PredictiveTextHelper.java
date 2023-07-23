package inc.flide.vim8.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.UserDictionary;
import android.util.Log;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PredictiveTextHelper {
    private final List<String> englishDictionary;
    private final List<String> personalDictionary;
    private final List<String> suggestedWords;
    private CharSequence textBeforeCursor;
    private String currentWord;

    public PredictiveTextHelper(ContentResolver contentResolver) {
        englishDictionary = new ArrayList<>();
        personalDictionary = new ArrayList<>();
        suggestedWords = new ArrayList<>();
        textBeforeCursor = "";
        currentWord = "";

        loadDictionaries(contentResolver);
    }

    public void setTextBeforeCursor(CharSequence text) {
        textBeforeCursor = text;
        currentWord = extractCurrentWord(textBeforeCursor.toString());
        generateWordSuggestions();
        Log.d("PredictiveText", suggestedWords.toString());
    }

    private void loadDictionaries(ContentResolver contentResolver) {
        loadUserDictionaryWords(contentResolver);
        loadEnglishDictionaryWords();
    }

    private void loadEnglishDictionaryWords() {

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
        suggestedWords.clear();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        for (String word : englishDictionary) {
            if (levenshteinDistance.apply(currentWord, word) <= 2) {
                suggestedWords.add(word);
            }
        }

        for (String word : personalDictionary) {
            if (levenshteinDistance.apply(currentWord, word) <= 2) {
                suggestedWords.add(word);
            }
        }

        suggestedWords.sort(Comparator.comparingInt(word -> levenshteinDistance.apply(currentWord, word)));
    }

}
