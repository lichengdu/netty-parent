package org.netty.client;


import org.junit.Ignore;
import org.junit.Test;

public class HttpConnectionTest {
	private final String TEST_URL="127.0.0.1:8000";
	private final String TEST_JSON="{\"method\":\"lcd.global.login.get\",\"data\":{\"name\":\"lichengdu\"},"
			+ "\"session\":\"12312312321321\"}";
	private final String TEST_CHARSET="UTF-8";
	
	
	
	@Test
	public void doSslPostTest(){
		try {
			String reulst=HttpConnection.doSslPost("https://"+TEST_URL, TEST_JSON, TEST_CHARSET);
		
			System.out.println(reulst);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void doSslGetTest(){
		try {
			String reulst=HttpConnection.doSslGet("https://"+TEST_URL+"?name=lichengdu", TEST_CHARSET);
			System.out.println(reulst);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void doGetTest(){
		try {
			String reulst=HttpConnection.doGet("http://"+TEST_URL, "name=lichengdu");
			System.out.println(reulst);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void doPostTest(){
		try {
			String reulst=HttpConnection.doPost("http://"+TEST_URL, TEST_JSON);
			System.out.println(reulst);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
