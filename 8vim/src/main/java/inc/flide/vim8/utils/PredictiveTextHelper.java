package inc.flide.vim8.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.UserDictionary;
import android.util.Log;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import inc.flide.vim8.R;

public class PredictiveTextHelper {
    private final Map<String, Long> wordFrequencyMap;
    private final List<String> personalDictionary;
    private final List<String> suggestedWords;

    public PredictiveTextHelper(Context context) {
        wordFrequencyMap = new HashMap<>();
        personalDictionary = new ArrayList<>();
        suggestedWords = new ArrayList<>();

        loadDictionaries(context);
    }

    public List<String> getSuggestedWords() {
        return suggestedWords;
    }

    public void setTextBeforeCursor(CharSequence textBeforeCursor) {
        String currentWord = extractCurrentWord(textBeforeCursor.toString());
        generateWordSuggestions(currentWord);
    }

    private void loadDictionaries(Context context) {
        loadUserDictionaryWords(context.getContentResolver());
        loadEnglishDictionaryWords(context);
    }

    private void loadEnglishDictionaryWords(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.english_word_frequency);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String word = parts[0];
                    Long frequency = Long.parseLong(parts[1].trim());
                    wordFrequencyMap.put(word, frequency);
                }
            }
            reader.close();
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

    private List<String> getClosestWords(String input) {
        final int ACCEPTABLE_LEVENSHTEIN_DISTANCE = 3;
        List<String> candidateWords = new ArrayList<>();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        for (String word : wordFrequencyMap.keySet()) {
            if (levenshteinDistance.apply(input, word) <= ACCEPTABLE_LEVENSHTEIN_DISTANCE) {
                candidateWords.add(word);
            }
        }

        for (String word : personalDictionary) {
            if (levenshteinDistance.apply(input, word) <= ACCEPTABLE_LEVENSHTEIN_DISTANCE) {
                candidateWords.add(word);
            }
        }

        candidateWords.sort(Comparator.comparingInt(word -> levenshteinDistance.apply(input, word)));
        return candidateWords;
    }

    private void generateWordSuggestions(String currentWord) {
        suggestedWords.clear();
        if (currentWord == null || currentWord.isEmpty()) {
            return;
        }

        // Get a list of candidate words based on Levenshtein distance
        List<String> candidateWords = getClosestWords(currentWord);

        // Create a map to store the candidates and their frequencies
        Map<String, Long> candidateFrequencies = new HashMap<>();
        for (String word : candidateWords) {
            Long frequency = wordFrequencyMap.get(word);
            if (frequency != null) {
                candidateFrequencies.put(word, frequency);
            } else {
                // If the word is not in the frequency map, assign it a low frequency
                candidateFrequencies.put(word, 1L);
            }
        }

        // Sort the candidates based on their frequencies
        suggestedWords.addAll(candidateFrequencies.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(12)
                .collect(Collectors.toList()));
    }

}
