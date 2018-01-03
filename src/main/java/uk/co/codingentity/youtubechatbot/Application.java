package uk.co.codingentity.youtubechatbot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Application {

    static private YouTube yt;


    private final static String username = "Many a True Nerd";

    public static void main(String[] args) {
        Credential userCredentials = null;
        try {
            userCredentials = Auth.authorise();
        } catch (IOException e) {
            System.err.println(String.format("failed to get authorisation tokens reason:%s", e.getLocalizedMessage()));
            System.exit(1);
        }
        yt = new YouTube.Builder(userCredentials.getTransport(), userCredentials.getJsonFactory(), userCredentials)
                .setApplicationName("youtube-chatbot-dev")
                .build();

        SearchResult chosenResult;
        try {
            chosenResult = getChannel();
        } catch (IOException e) {
            System.err.println(String.format("failed to search for channel channel. reason:%s", e.getLocalizedMessage()));
            System.exit(2);
        }

    }

    private static SearchResult getChannel() throws IOException {
        YouTube.Search.List search = yt.search().list("id,snippet");
        search.setType("channel");
        search.setQ(username);
        SearchListResponse response = search.execute();
        List<SearchResult> results = response.getItems();
        SearchResult chosenResult = null;
        if (results.size() == 0) {
            System.out.println("No match for channel search found.");
        } else if (results.size() == 1) {
            System.out.println("Found match for channel search");
            chosenResult = results.get(1);
        } else {
            do {
                //not 0 not 1 therefore must be more than one result so we must get the user to choose.
                System.out.println("Select a channel result");
                for (int i = 1; i <= results.size(); i++) {
                    SearchResult res = results.get(i - 1);
                    System.out.println(String.format("%s: %s (id:%s) ", i, res.getSnippet().getChannelTitle(), res.getSnippet().getChannelId()));
                }
                Scanner s = new Scanner(System.in);

                while (!s.hasNextInt()) {
                    s.next();
                }

                int chosenIndex = s.nextInt();
                if (chosenIndex >= results.size()) {
                    System.out.println("Choose a channel number within the range.");
                } else {
                    chosenResult = results.get(chosenIndex - 1);
                }
            } while (chosenResult == null);

        }
        return chosenResult;
    }
}
