package uk.co.codingentity.youtubechatbot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Application {

    static private YouTube yt;

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
        System.out.println("Enter username to search for.");
        SearchResult chosenResult = null;
        try {
            chosenResult = getChannel(new Scanner(System.in).next());
        } catch (IOException e) {
            System.err.println(String.format("failed to search for channel . reason:%s", e.getLocalizedMessage()));
            System.exit(2);
        }


        SearchResult nextStream = null;
        try {
            nextStream = findNextStream(chosenResult.getSnippet().getChannelId());
        } catch (IOException e) {
            System.err.println(String.format("failed to search for livestream. reason:%s", e.getLocalizedMessage()));
            System.exit(3);
        }

        String livestreamId = nextStream.getId().getVideoId();  //so we can make the calls based on the live stream.
        String livestreamTitle = nextStream.getSnippet().getTitle(); //just nice to have around.
        String livestreamState = nextStream.getSnippet().getLiveBroadcastContent(); //so we know if we are live or not.

        try {
            ChatStream cs = new ChatStream(yt, getLiveChatId(livestreamId));
            cs.startReadingChat();
            cs.waitForCompletion();
        } catch (IOException e) {
        }
    }

    static String getLiveChatId(String videoId) throws IOException {
        // Get liveChatId from the video
        YouTube.Videos.List videoList = yt.videos()
                .list("liveStreamingDetails")
                .setFields("items/liveStreamingDetails/activeLiveChatId")
                .setId(videoId);
        VideoListResponse response = videoList.execute();
        for (Video v : response.getItems()) {
            String liveChatId = v.getLiveStreamingDetails().getActiveLiveChatId();
            if (liveChatId != null && !liveChatId.isEmpty()) {
                return liveChatId;
            }
        }

        return null;
    }

    private static SearchResult findNextStream(String channelId) throws IOException {
        YouTube.Search.List search = yt.search().list("id,snippet");
        search.setChannelId(channelId);
        search.setType("video");
        search.setEventType("live");
        SearchListResponse liveStreams = search.execute();
        if (liveStreams.getItems().size() == 0) {
            search.setEventType("upcoming");
            search.setOrder("date");
            liveStreams = search.execute();
        }
        if (liveStreams.getItems().size() != 0) {
            return liveStreams.getItems().get(0);
        }
        return null;
    }

    private static SearchResult getChannel(String username) throws IOException {
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
            chosenResult = results.get(0);
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
