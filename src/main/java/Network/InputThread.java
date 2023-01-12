package Network;

import Util.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputThread extends Thread {
    Node node;
    String input;
    BufferedReader inFromUser;

    public InputThread(Node n) {
        node = n;
    }

    public void run() {
        try {
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            // if it fails to write quit, it asks for it again
            while (true) {
                System.out.println(Color.ANSI_RED + "\n\t*** Write 'q' to quit or 'r' to recharge ***\n" + Color.ANSI_RESET);
                input = inFromUser.readLine();
                // if you write quit
                if (input.equalsIgnoreCase("q")) {
                    if (node.getFlagClosure()) {
                        System.out.println("Closing procedure already in progress");
                    } else {
                        node.setFlagClosure(true);
                        ClosureThread exit = new ClosureThread(node);
                        exit.start();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                } else if (input.equalsIgnoreCase("r")) {
                    if (node.getDrone().getBattery() == 100) {
                        System.out.println("The battery is already 100%");
                    } else {
                        node.setWantRecharge(true);

                        // procedure to recharge the drone
                        RechargeThread recharge = new RechargeThread(node);
                        recharge.start();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
