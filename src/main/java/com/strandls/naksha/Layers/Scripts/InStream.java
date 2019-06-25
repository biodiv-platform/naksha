/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.Layers.Scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author humeil
 */
public class InStream extends Thread {

    private final InputStream in;
    private final PrintWriter pw;

    public InStream(InputStream in, PrintWriter pw, String threadName) {
        super(threadName);
        this.in = in;
        this.pw = pw;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in));) {
            String line;
            while ((line = br.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }
                pw.println(line);
                pw.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
