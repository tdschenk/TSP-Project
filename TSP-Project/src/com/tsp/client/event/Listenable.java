package com.tsp.client.event;

import java.util.HashMap;
import java.util.Properties;



public interface Listenable {
	public void addListener(GameListener gl);
	public void removeListener(GameListener gl);
	public void fireEvent(EventType e, HashMap<String, Object> payload);
}
