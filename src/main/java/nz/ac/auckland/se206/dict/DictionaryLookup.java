package nz.ac.auckland.se206.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class DictionaryLookup {

  private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

  /**
   * This makes an API request to the dictionary API and returns the WordInfo object related to the
   * word we want to query the dictionary API with.
   *
   * @param query the string containing the word we want to query.
   * @return the WordInfo for this particular word.
   * @throws IOException If an I/O error occurs during the method execution.
   * @throws WordNotFoundException If we cannot find an appropriate WordInfo.
   */
  public static WordInfo searchWordInfo(String query) throws IOException, WordNotFoundException {

    // Initialising the client, request and response for the query we wish to make.
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(API_URL + query).build();
    Response response = client.newCall(request).execute();
    ResponseBody responseBody = response.body();

    // Initialising the JSON string that will contain the contents of the request response.
    String jsonString = responseBody.string();

    try {
      // Attempting to find the corresponding word and if we cannot find appropriate WordInfo, throw
      // an exception.
      JSONObject jsonObj = (JSONObject) new JSONTokener(jsonString).nextValue();
      String title = jsonObj.getString("title");
      String subMessage = jsonObj.getString("message");
      throw new WordNotFoundException(query, title, subMessage);
    } catch (ClassCastException e) {
      System.out.println(" ");
    }

    //  Initialise arrays to store appropriate word information.
    JSONArray vter = (JSONArray) new JSONTokener(jsonString).nextValue();
    List<WordEntry> entries = new ArrayList<>();

    for (int e = 0; e < vter.length(); e++) {
      JSONObject jsonEntryObj = vter.getJSONObject(e);
      JSONArray jsonMeanings = jsonEntryObj.getJSONArray("meanings");

      // If no part of speech found, incase words do not have one.
      String partOfSpeech = "[not specified]";
      List<String> definitions = new ArrayList<>();

      // Iterate through and obtain definitions for each word, storing them in an array.
      for (int m = 0; m < jsonMeanings.length(); m++) {
        JSONObject jsonMeaningObj = jsonMeanings.getJSONObject(m);
        String pos = jsonMeaningObj.getString("partOfSpeech");

        // if there is a part of speech, assign it.
        if (!pos.isEmpty()) {
          partOfSpeech = pos;
        }

        // Adding the definitions into the correct object format, so we can obtain a string,
        JSONArray jsonDefinitions = jsonMeaningObj.getJSONArray("definitions");
        for (int d = 0; d < jsonDefinitions.length(); d++) {
          JSONObject jsonDefinitionObj = jsonDefinitions.getJSONObject(d);

          // Adding the definitions to a list so we can access them in our HiddenCanvasController.
          String definition = jsonDefinitionObj.getString("definition");
          if (!definition.isEmpty()) {
            definitions.add(definition);
          }
        }
      }
      // Add this word entry to the list of word entries
      WordEntry wordEntry = new WordEntry(partOfSpeech, definitions);
      entries.add(wordEntry);
    }
    return new WordInfo(query, entries);
  }
}
