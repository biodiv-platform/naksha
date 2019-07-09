/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.Layers.Scripts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author humeil
 */
public class OutStream extends Thread {

	private final String message;
	private final OutputStream out;

	public OutStream(OutputStream out, String threadName, String message) {
		super(threadName);
		this.out = out;
		this.message = message;
	}

	@Override
	public void run() {
		try (PrintWriter pw = new PrintWriter(out);) {
			pw.println(message);
			pw.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
