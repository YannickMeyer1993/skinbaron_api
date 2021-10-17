package com.company;

import org.postgresql.util.PSQLException;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;

import static com.company.SkinbaronAPI.getBalance;
import static com.company.common.getConnection;
import static com.company.common.readPasswordFromFile;

@SuppressWarnings("BusyWait")
public class SkinbaronLiveCheck {

    public static void main(String[] args) throws Exception {

        int wait_counter = 3;

        String secret = readPasswordFromFile("C:/passwords/api_secret.txt");

        while (true) {
            try (Connection conn = getConnection()){

                System.out.println("Waiting for " + Math.pow(2, wait_counter) + " seconds");
                Thread.sleep((long) (Math.pow(2, wait_counter) * 1000));
                getBalance(secret,false,conn);
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
