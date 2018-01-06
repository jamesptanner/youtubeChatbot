package uk.co.codingentity.youtubechatbot;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveChatMessage;
import com.google.api.services.youtube.model.LiveChatMessageListResponse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ChatStream {
    private YouTube m_yt;
    private String m_liveChatId;
    private Thread m_chatReadThread;
    LinkedList<LiveChatMessage> m_messageStore;
    private boolean endStream = false;

    public ChatStream(YouTube yt, String liveChatId) {
        m_yt = yt;
        m_liveChatId = liveChatId;
        m_messageStore = new LinkedList<>();
        m_messageProcessor.start();
    }

    public void startReadingChat() {
        m_chatReadThread = new Thread(() -> {
            String pageToken = null;
            do {
                try {
                    YouTube.LiveChatMessages.List chatMessagesList = m_yt.liveChatMessages().list(m_liveChatId, "id,snippet,authorDetails");
                    if (pageToken != null) {
                        chatMessagesList.setPageToken(pageToken);
                    }
                    LiveChatMessageListResponse chatResponse = chatMessagesList.execute();
                    pageToken = chatResponse.getNextPageToken();
                    List<LiveChatMessage> chatMessages = chatResponse.getItems();
                    for (LiveChatMessage message : chatMessages) {
                        m_messageStore.add(message);
                    }
                    Thread.sleep(chatResponse.getPollingIntervalMillis());
                } catch (IOException e) {

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!endStream);
        });
        m_chatReadThread.start();

    }

    public void waitForCompletion() {
        try {
            m_chatReadThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Thread m_messageProcessor = new Thread(new Runnable() {
        @Override
        public void run() {
            do {
                if (m_messageStore.peek() != null) {
                    LiveChatMessage msg = m_messageStore.pop();
                    System.out.println(String.format("%s: %s", msg.getAuthorDetails().getDisplayName(), msg.getSnippet().getDisplayMessage()));
                }
            } while (!endStream);
        }
    });

}
