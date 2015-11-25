package com.tamil.wakeonlan;

/**
 *	Convenience class for passing history entries around the application
 */

public class HistoryItem {
	public int id;
	public String title;
	public String mac;
	public String ip;
	public int port;

	public HistoryItem(int id, String title, String mac, String ip, int port) {
		this.id = id;
		this.title = title;
		this.mac = mac;
		this.ip = ip;
		this.port = port;
	}
}
