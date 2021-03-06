package com.tsp.client.model;

import java.awt.Point;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.tsp.game.actors.Actor;
import com.tsp.game.actors.Player;
import com.tsp.game.map.Dungeon;
import com.tsp.game.map.Point3D;
import com.tsp.packets.ActorUpdate;
import com.tsp.packets.MessagePacket;
import com.tsp.packets.Packet;
import com.tsp.packets.ScorePacket;
import com.tsp.util.KDTuple;

public class GameModel
{

	private int MESSAGE_TIMEOUT = 50;

	Queue<Packet> packets = new LinkedList<Packet>();
	List<SimpleEntry<Integer, String>> messages = new ArrayList<SimpleEntry<Integer, String>>();

	boolean quit = false;

	// Actor and dungeon
	Point3D attackLocation;
	String attackAnimation = "-";

	// Game tile types
	private HashMap<Integer, Actor> otherActors;
	private HashMap<String, KDTuple> scores;
	private boolean ready = false;

	private Player me;
	private Dungeon dungeon;
	private String playerName;

	public GameModel()
	{
		otherActors = new HashMap<Integer, Actor>();
		scores = new HashMap<String, KDTuple>();
	}

	public GameModel(String playerName)
	{
		this();
		this.playerName = playerName;
	}

	/* Convinience methods for getting dungeon and actor */
	public Dungeon getDungeon()
	{
		return this.dungeon;
	}

	public Player getMe()
	{
		if (me == null)
		{
			me = new Player(this.playerName, getDungeon().getRows(),
					getDungeon().getColumns(), getDungeon().getFloors());
		}
		return me;
	}

	/* Methods used to get symbols and colors for drawing in the map */
	public String getSymbol(int y, int x, int z)
	{
		Point3D point = new Point3D(x, y, z);
		if (me.getPos().equals(point))
		{
			return me.getSymbol();
		} else if (me.isAttacking() && me.getAttackPos().equals(point))
		{
			return me.getAttackSymbol();
		}

		if (!dungeon.isUnrevealed(x, y, z))
			for (Actor a : otherActors.values())
			{
				if (a.getPos().equals(point))
				{
					return a.getSymbol();
				}
				if (a instanceof Player)
				{
					if (((Player) a).isAttacking()
							&& ((Player) a).getAttackPos().equals(point))
					{
						return ((Player) a).getAttackSymbol();
					}
				}
			}

		return this.dungeon.getTile(new Point3D(x, y, z));
	}

	public int getColor(int x, int y, int z)
	{
		if (me.getPos().equals(new Point3D(x, y, z))
				|| (attackLocation != null && x == attackLocation.getX()
				&& y == attackLocation.getY() && z == attackLocation
				.getZ()))
		{
			return me.getColor();
		}
		if (dungeon.isUnrevealed(x, y, z))
			return 232 + 9;

		for (Actor a : otherActors.values())
		{
			if (a.getPos().equals(new Point3D(x, y, z)))
			{
				return a.getColor();
			}
		}

		if (dungeon.isStairUp(x, y, z) || dungeon.isStairDown(x, y, z))
			return (int) (255.0 / 2);
		return 255;
	}

	/* Convinience setters and getters, also game state information */
	public void setID(int id)
	{
		this.getMe().setID(id);
	}

	public void setDungeon(String[][][] dungeon)
	{
		this.dungeon = new Dungeon(dungeon);
	}

	public void setMe(Player me)
	{
		this.me = me;
		dungeon.updateVisibleDungeon(this.me);
		if (!scores.containsKey(me.getName()))
			scores.put(me.getName(), new KDTuple());
	}

	public void setQuit(boolean quit)
	{
		this.quit = quit;
	}

	public boolean getQuit()
	{
		return quit;
	}

	public boolean getReady()
	{
		return ready;
	}

	public void setReady(boolean ready)
	{
		this.ready = ready;
	}

	/* Client-Server Communication functions */
	public boolean packetsAviable()
	{
		boolean ret;
		synchronized (this)
		{
			ret = !packets.isEmpty();
		}
		return ret;
	}

	public Packet getPacket()
	{
		Packet ret;
		synchronized (this)
		{
			ret = packets.poll();
		}
		return ret;
	}

	public void insertPacket(Packet e)
	{
		if (e != null)
		{
			synchronized (this)
			{
				packets.add(e);
			}
		}
	}

	public boolean hasPackets()
	{
		return !packets.isEmpty();
	}

	public void update(ActorUpdate actorUpdate)
	{
		if (otherActors.containsKey(actorUpdate.getActorID())
				|| getMe().getId() == actorUpdate.getActorID())
		{
			if (actorUpdate.contains("remove"))
			{
				if (getMe().getId() == actorUpdate.getActorID())
				{
					me.setHealth(0);
					setQuit(true);
				} else {
					scores.remove(otherActors.get(actorUpdate.getActorID()).getName());
					otherActors.remove(actorUpdate.getActorID());
				}
			} else
			{
				boolean meMoved = false;
				Actor actor = (getMe().getId() == actorUpdate.getActorID() ? getMe()
						: otherActors.get(actorUpdate.getActorID()));

				if (actorUpdate.contains("X"))
				{
					actor.setX(((Long) actorUpdate.getValue("X")).intValue());
					if (actor.getId() == me.getId())
					{
						meMoved = true;
					}
				}

				if (actorUpdate.contains("Y"))
				{
					actor.setY(((Long) actorUpdate.getValue("Y")).intValue());
					if (actor.getId() == me.getId())
					{
						meMoved = true;
					}
				}

				if (actorUpdate.contains("Z"))
				{
					actor.setZ(((Long) actorUpdate.getValue("Z")).intValue());
					if (actor.getId() == me.getId())
					{
						meMoved = true;
					}
				}

				if (actorUpdate.contains("health"))
					actor.setHealth(((Long) actorUpdate.getValue("health"))
							.intValue());

				if (actorUpdate.contains("symbol"))
					actor.setSymbol((String) actorUpdate.getValue("symbol"));

				if (actorUpdate.contains("attacking")
						&& actorUpdate.contains("deltaX")
						&& actorUpdate.contains("deltaY"))
				{
					((Player) actor).setAttacking((Boolean) actorUpdate
							.getValue("attacking"), new Point3D(
									((Long) actorUpdate.getValue("deltaX")).intValue(),
									((Long) actorUpdate.getValue("deltaY")).intValue(),
									0));
				}
				if (meMoved)
				{
					dungeon.updateVisibleDungeon(me);
				}
			}
		}
	}

	public void addActor(Actor actor)
	{
		if (actor.getId() != me.getId()
				&& !otherActors.containsKey(actor.getId())) {
			otherActors.put(actor.getId(), actor);
			scores.put(actor.getName(), new KDTuple());
		}
	}

	public void setScoreForActor(Actor actor, KDTuple score) {
		scores.put(actor.getName(), score);
	}

	private boolean occupied(Point3D point)
	{
		for (Actor actor : otherActors.values())
		{
			if (actor.getPos().equals(point))
				return true;
		}
		return false;
	}

	/* Attempting attack and move methods */
	public boolean attemptMove(Point delta)
	{
		Point3D newPosition = me.getPos().clone();
		newPosition.translate((int) delta.getX(), (int) delta.getY());

		if (getMe().isAttacking())
			return false;

		// Verify the new Point3D is inside the map
		if (getDungeon().validPoint(newPosition))
		{
			int x = (int) newPosition.getX();
			int y = (int) newPosition.getY();
			int z = newPosition.getZ();
			if (!occupied(newPosition))
			{
				if (dungeon.isEmptyFloor(x, y, z))
				{
					return true;
				} else if (dungeon.isStairUp(x, y, z))
				{
					return true;
				} else if (dungeon.isStairDown(x, y, z))
				{
					return true;
				} else
				{
					return false;
				}
			}
		}
		return false;
	}

	public boolean attemptAttack(Point3D delta)
	{
		Point3D newPosition = me.getPos().clone();
		newPosition.translate(delta.x, delta.y);

		int x = newPosition.x;
		int y = newPosition.y;
		int z = newPosition.getZ();

		if (getMe().isAttacking() && getMe().attemptAttackReset())
			return false;

		if (getDungeon().validPoint(newPosition)
				&& (dungeon.isEmptyFloor(new Point3D(x, y, z)) || occupied(newPosition)))
		{
			me.setAttacking(true, delta);
			return true;
		}

		return false;
	}

	// Messaging functionality
	public void addMessage(MessagePacket packet) {
		String message = packet.getMessage();
		SimpleEntry<Integer, String> newEntry = new SimpleEntry<Integer, String>(MESSAGE_TIMEOUT, message);

		// Make sure that this message doesn't already exist
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i).getValue().equals(message))
				return;
		}
		messages.add(newEntry);
	}

	public List<String> getMessages() {
		// Decrement the scores of all the messages by one
		List<String> ret = new ArrayList<String>();

		for (int i = 0; i < messages.size(); i++) {
			SimpleEntry<Integer, String> current = messages.get(i);
			Integer newKey = current.getKey().intValue() - 1;
			if (newKey > 0) {
				messages.set(i, new SimpleEntry<Integer, String>(newKey, current.getValue()));
				ret.add(current.getValue());
			} else {
				messages.remove(i);
			}
		}

		return ret;
	}

	public void updateScore(ScorePacket packet) {
		this.scores.put(packet.getPlayerID(), packet.getScore());
	}

	public List<String> getScores() {
		List<String> ret = new ArrayList<String>();
		List<String> used = new ArrayList<String>();

		for (int i = 0; i < scores.size(); i++) {
			String greatestKillsName = "";
			KDTuple greatestScore = null;
			
			// Locate the greatest scorer so far
			for (String name: scores.keySet()) {
				if (used.contains(name))
					continue;
				
				KDTuple score = scores.get(name);
				if (greatestScore == null || score.kills() > greatestScore.kills()) {
					greatestKillsName = name;
					greatestScore = score;
				}
			}
			
			StringBuilder b = new StringBuilder().append(greatestScore.kills()/3)
					.append("/")
					.append(greatestScore.deaths()/3)
					.append("-")
					.append(greatestKillsName);

			ret.add(b.toString());
			used.add(greatestKillsName);
		}
		return ret;
	}
}
