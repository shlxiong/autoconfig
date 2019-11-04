package com.openxsl.config.dal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;

public final class TelnetOperator {
	private TelnetClient telnet;
	private String host;
	private int port;
	private String username;
	private String password;
	
    private InputStream in;  
    private PrintStream out;
    private String prompt = ">";
    private char promptChar = '>';
	
    public TelnetOperator(){
    	telnet = new TelnetClient();
		telnet.setConnectTimeout(5000);
    }
    public TelnetOperator(String hostAndport){
    	this();
    	String[] endpoint = hostAndport.split(":");
    	this.host = endpoint[0];
		this.port = endpoint.length<2 ? 22 : Integer.parseInt(endpoint[1]);
    }
	public TelnetOperator(String host, String username, String password){
		this(host, 22, username, password);
	}
	
	public TelnetOperator(String host, int port, String username, String password){
		this();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public void setPrompt(String prompt) {
        if (prompt!=null){
            this.prompt = prompt;
            this.promptChar = prompt.charAt(prompt.length()-1);
        }  
    }
	
	public boolean ping(){
		try {  
            telnet.connect(host, port);
    		telnet.setSoTimeout(4000);
    		return telnet.getInputStream() != null;
        } catch (IOException e) {
            return false;
        } finally {
        	this.disconnect();
        }
	}
	
	public boolean connect(){
		try {  
            telnet.connect(host, port);
    		telnet.setSoTimeout(10000);
        } catch (IOException e) {
            return false;
        }
		
		in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
        /** Log the user on* */  
        readUntil("login: ");  //读取 login：提示符
        write(username);
        readUntil("password: ");
        write(password);
        
		String response = readUntil(null);
		if (response!=null && response.contains("Login Failed")){  
			return false; 
		}else{
			return true;
		}
	}
	
	public String sendCommand(String command) {  
        try {  
            write(command);  
            return readUntil(prompt);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }
	
	public void disconnect(){
		try {
			telnet.disconnect();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	private String readUntil(String pattern) {
		StringBuilder sb = new StringBuilder();
		System.out.println("readUtil -> "+pattern);
        try {
        	final boolean flag = (pattern==null || pattern.length()<1);
        	final char lastChar = flag ? promptChar
        				: pattern.charAt(pattern.length() - 1);
            char ch = (char) in.read();
            while (true) {
                sb.append(ch);
                if (ch == lastChar) {
                    if (flag || sb.toString().endsWith(pattern)) {
                    	break;
                    }
                }
                ch = (char) in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("response -> "+sb.toString());
        
        return sb.toString();
    } 
	private void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPrompt() {
		return prompt;
	}
	public static void main(String[] args) {
		TelnetOperator test = new TelnetOperator("127.0.0.1:7080");
		System.out.println(test.ping());
		test.disconnect();
		
		System.out.println("telnet 10.50.8.10:22....");
		test = new TelnetOperator("10.50.8.10", "admin", "hOm0G.G9OYlVw");
		System.out.println(test.connect());
		test.disconnect();
	}

}
