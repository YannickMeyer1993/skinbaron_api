package de.yannickm.steambot.common;

public class LoggingHelper {
    public static void setUpClass() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.apache.http");
        root.setLevel(ch.qos.logback.classic.Level.ERROR);
        ch.qos.logback.classic.Logger root2 = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.gargoylesoftware.htmlunit");
        root2.setLevel(ch.qos.logback.classic.Level.ERROR);
        ch.qos.logback.classic.Logger root3 = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.springframework.web");
        root3.setLevel(ch.qos.logback.classic.Level.INFO);
        ch.qos.logback.classic.Logger root4 = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("com.gargoylesoftware.htmlunit.javascript");
        root4.setLevel(ch.qos.logback.classic.Level.OFF);
        ch.qos.logback.classic.Logger root5 = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("de.yannickm.steambot.common");
        root5.setLevel(ch.qos.logback.classic.Level.WARN);
        //...
    }
}
