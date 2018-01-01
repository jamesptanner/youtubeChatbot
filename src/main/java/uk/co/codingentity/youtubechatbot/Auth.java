package uk.co.codingentity.youtubechatbot;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Arrays;

public class Auth {
final static NetHttpTransport NET_TRANSPORT = new NetHttpTransport();
final static JsonFactory JSON_FACTORY = new JacksonFactory();
final static Iterable<String> SCOPE = Arrays.asList(";".split(";"));

    final GoogleAuthorizationCodeFlow flow;
    public Auth(){
        flow = new GoogleAuthorizationCodeFlow(NET_TRANSPORT, JSON_FACTORY, Secrets.ClientID, Secrets.ClientSecret,SCOPE);
    }
}
