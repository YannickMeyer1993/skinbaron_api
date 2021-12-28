package com.company.service;

import org.postgresql.util.PSQLException;

import java.sql.Connection;

import static com.company.SkinbaronAPI.buyItem;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

public class CheckLiveSkinbaron {
    public static void main(String[] args) throws Exception {

        int wait_counter = 3;

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        while (true) {
            try (Connection conn = getConnection()){

                System.out.println("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));
                buyItem(conn, secret, "a52eca5d-6beb-4cf8-8173-a1eae90cbb14", 0.09);
                break;
            } catch (PSQLException e) {
                System.out.println("Postgres is down.");
                break;
            } catch (InterruptedException e) {
                System.out.println("Program got interrupted.");
                break;
            }
            catch (Exception e) {
                System.out.println("Skibaron APIs are still down.");
                wait_counter++;

            }

        }
    }
}
