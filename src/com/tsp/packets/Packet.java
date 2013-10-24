package com.tsp.packets;

import com.tsp.game.actors.Actor;
import com.tsp.game.map.Point3D;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Tim
 * Date: 10/16/13
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Packet implements JSONAware
{
	public enum PacketType
	{
		NOTAPACKET,
		MOVEMENTPACKET,
		ACTORPACKET,
		UPDATEPACKET,
		ATTACKPACKET,
		QUITPACKET
	}

	protected static Integer packetCount = 0;
	protected Integer packetID;

	public PacketType getPacketType()
	{
		return packetType;
	}

	protected PacketType packetType;

	public Packet(Integer _packetID)
	{
		packetID = _packetID;
	}

	public Packet()
	{
		packetID = incrementPacketCount();
	}

	protected Integer incrementPacketCount()
	{
		return packetCount++;
	}

	public Integer getPacketID()
	{
		return packetID;
	}

	public static Packet parseJSONObject(JSONObject obj) throws IllegalArgumentException
	{
		if (obj.containsKey("packetType") && obj.containsKey("packetID"))
		{
			PacketType type;
			try
			{
				type = PacketType.valueOf((String) obj.get("packetType"));
			}
			catch (Exception e)
			{
				type = PacketType.NOTAPACKET;
			}
			switch (type)
			{
				case MOVEMENTPACKET:
				{
					if (!(obj.containsKey("playerID") && obj.containsKey("X") && obj.containsKey("Y") &&
					      obj.containsKey("Z")))
						throw new IllegalArgumentException("Not a valid Movement packet");

					return new MovementPacket(((Long) obj.get("packetID")).intValue(),
					                          ((Long) obj.get("playerID")).intValue(),
					                          ((Long) obj.get("X")).intValue(),
					                          ((Long) obj.get("Y")).intValue(),
					                          ((Long) obj.get("Z")).intValue());
				}
				case ACTORPACKET:
				{
					if (!(obj.containsKey("actor") && ((JSONObject) obj.get("actor")).containsKey("id") &&
					      ((JSONObject) obj.get("actor")).containsKey("name") &&
					      ((JSONObject) obj.get("actor")).containsKey("X") &&
					      ((JSONObject) obj.get("actor")).containsKey("Y") &&
					      ((JSONObject) obj.get("actor")).containsKey("Z") &&
					      ((JSONObject) obj.get("actor")).containsKey("health") &&
					      ((JSONObject) obj.get("actor")).containsKey("type") &&
					      ((JSONObject) obj.get("actor")).containsKey("symbol")))
						throw new IllegalArgumentException("Not a valid actor packet");


					int id = ((Long) ((JSONObject) obj.get("actor")).get("id")).intValue();
					String name = (String) ((JSONObject) obj.get("actor")).get("name");
					int x = ((Long) ((JSONObject) obj.get("actor")).get("X")).intValue();
					int y = ((Long) ((JSONObject) obj.get("actor")).get("Y")).intValue();
					int z = ((Long) ((JSONObject) obj.get("actor")).get("Z")).intValue();
					int health = ((Long) ((JSONObject) obj.get("actor")).get("health")).intValue();
					Actor.ActorType actorType = Actor.ActorType
							.valueOf((String) ((JSONObject) obj.get("actor")).get("type"));
					String symbol = (String) ((JSONObject) obj.get("actor")).get("symbol");
					int color = ((Long) ((JSONObject) obj.get("actor")).get("color")).intValue();

					Actor actor = new Actor(id, health, new Point3D(x, y, z), name, actorType, symbol, color);

					return new ActorPacket(((Long) obj.get("packetID")).intValue(), actor);
				}
				case QUITPACKET:
					return new QuitPacket();
				case ATTACKPACKET:
				{
					if (!(obj.containsKey("playerID") && obj.containsKey("X") && obj.containsKey("Y")))
						throw new IllegalArgumentException("Not a valid Attack packet");

					return new AttackPacket(((Long) obj.get("packetID")).intValue(),
					                          ((Long) obj.get("playerID")).intValue(),
					                          ((Long) obj.get("X")).intValue(),
					                          ((Long) obj.get("Y")).intValue());
				}
				default:
					throw new IllegalArgumentException("Not a valid packet");
			}
		}
		else
			throw new IllegalArgumentException("JSON is not packet");

	}
}
