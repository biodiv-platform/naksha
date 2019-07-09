/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.Layers.Scripts;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author humeil
 */
public class DBexec {
	public static int main_func_generation(String sql_fl, String dbname, String dbpassword, String dbuser) {
		try {
			String password = "hum123";
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "PGPASSWORD=" + dbpassword
					+ " psql -h localhost -d " + dbname + " -a -U " + dbuser + " -f " + sql_fl);
			Process process = builder.start();
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(process);
			int i = process.getErrorStream().available();
			StringWriter errors = new StringWriter();
			StringWriter input = new StringWriter();
			new OutStream(process.getOutputStream(), "output-stream-thread", dbpassword).start();
			new InStream(process.getErrorStream(), new PrintWriter(errors, true), "error-stream-thread").start();
			System.out.println(process.getErrorStream().available());
			new InStream(process.getInputStream(), new PrintWriter(input, true), "input-stream-thread").start();
			process.waitFor();
			return i;
		} catch (Error | Exception ex) {
			return 1;
		}

	}
}
