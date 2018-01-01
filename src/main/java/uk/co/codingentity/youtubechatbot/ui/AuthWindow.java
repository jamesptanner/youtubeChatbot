package uk.co.codingentity.youtubechatbot.ui;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class AuthWindow {
    WebView browser;

    public AuthWindow() {
    }

    public void initAndShowUI() {
        JFrame frame = new JFrame("Authorise with Youtube.");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setVisible(true);
        frame.setSize(500, 700);
        Platform.runLater(() -> initFX(fxPanel));
    }

    private void initFX(JFXPanel fxPanel) {
        browser = new WebView();
        fxPanel.setOpaque(true);
        fxPanel.setScene(new Scene(browser));

    }

    public void navigateToUrl(final String url) {
        Platform.runLater(() -> {
            WebEngine engine = browser.getEngine();
            engine.load(url);
        });
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        AuthWindow win = new AuthWindow();
        SwingUtilities.invokeAndWait(() -> win.initAndShowUI());
        win.navigateToUrl("https://google.com");
    }
}
