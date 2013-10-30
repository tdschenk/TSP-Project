package com.tsp.game.actors;

import java.awt.Point;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.tsp.game.map.Point3D;

/**
 * The Actor Superclass that all {@link Player} and {@link AI} are based on
 * This class is also JSONAware so for the JSONEncoder
 *
 * @author Tim Bradt
 * @since v1.0
 * @version v1.0
 * @see JSONAware
 */
public class Actor implements JSONAware
{

	/**
	 * The number of actors generated by the server
	 */
	protected static int count = 0;
	Point3D pos;
	String name;
	int id;
	int health;
	int color = 255;
	ActorType type;
	String symbol;

	/**
	 * Sets Actor ID to actor count and then increments actor count
	 */
	public Actor()
	{
		id = count;
		count++;
	}

	/**
	 * Allows for the creation of the actor by the packet without having to know AI type
	 *
	 * @see Packet.parseJSONObject
	 *
	 * @param id the id of the actor
	 * @param health the health of the actor
	 * @param pos the position of the actor
	 * @param name the name of the actor
	 * @param type the type of actor
	 * @param symbol the symbol of the actor
	 * @param color the color of the actor
	 */
	public Actor(int id, int health, Point3D pos, String name, ActorType type, String symbol, int color)
	{
		this.id = id;
		this.name = name;
		this.pos = pos;
		this.health = health;
		this.type = type;
		this.symbol = symbol;
		this.color = color;
	}

	/**
	 * Gets the ActorType
	 * @see ActorType
	 * @return the actors type
	 */
	public ActorType getType()
	{
		return type;
	}

	/**
	 * Gets the actors symbol
	 * @return the symbol string
	 */
	public String getSymbol()
	{
		return symbol;
	}

	/**
	 * Updates the symbol of the actor
	 * @param symbol the new symbol
	 */
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	/**
	 * Gets the floor the actor is on
	 * @return the floor number
	 */
	public int getZ()
	{
		return pos.getZ();
	}

	/**
	 * Sets the Z position
	 * @param z the new Z position
	 */
	public void setZ(Integer z)
	{
		pos.setZ(z);
	}

	/**
	 * Gets the column the actor is in
	 * @return the column number
	 */
	public int getX()
	{
		return (int) pos.getX();
	}

	/**
	 * Sets the X position
	 * @param x the new X position
	 */
	public void setX(Integer x)
	{
		pos.x = x;
	}

	/**
	 * Gets the row the actor is in
	 * @return the row number
	 */
	public int getY()
	{
		return (int) pos.getY();
	}

	/**
	 * Sets the Y position
	 * @param y the new Y position
	 */
	public void setY(Integer y)
	{
		pos.y = y;
	}

	/**
	 * Gets the actors color
	 * @return the int value of the color of the player
	 */
	public int getColor()
	{
		return color;
	}

	/**
	 * Checks if actor was hit
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return whether or not the actor was hit
	 */
	boolean checkHit(int x, int y, int z)
	{
		return checkHit(new Point3D(x, y, z));
	}

	/**
	 * Checks if actor was hit
	 * @param check the point to check against
	 * @return whether or not the actor was hit
	 */
	public boolean checkHit(Point3D check)
	{
		return pos.equals(check);
	}

	/**
	 * Gets the Position of the actor
	 * @return the {@link Point3D} Position of the actor
	 */
	public Point3D getPos()
	{
		return pos;
	}

	/**
	 * Sets the Position of the actor
	 * @param pos the new position
	 */
	public void setPos(Point3D pos)
	{
		this.pos = pos;
	}

	/**
	 * Move Actor by X columns and Y rows
	 * @param x columns to move by
	 * @param y rows to move by
	 */
	public void move(int x, int y)
	{
		move(new Point3D(x, y, 0));
	}

	/**
	 * Move Actor by a movement vector
	 * @param movement the vector for movement
	 */
	public void move(Point3D movement)
	{
		pos.add(movement);
	}

	/**
	 * Move Actor by X columns, Y rows, and Z floors
	 * @param x columns to move by
	 * @param y rows to move by
	 * @param z floors to move by
	 */
	public void move(int x, int y, int z)
	{
		move(new Point3D(x, y, z));
	}

	/**
	 * Gets the actor name
	 * @return the actors name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the actor health
	 * @return the actors health
	 */
	public int getHealth()
	{
		return health;
	}

	/**
	 * Updates the health of the actor
	 * @param health the new health
	 */
	public void setHealth(Integer health)
	{
		this.health = health;
	}

	/**
	 * Gets the actor id
	 * @return the id of the actor
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Actor attackee damages this actor
	 * @param attackee the attackee actor
	 */
	public void hit(Actor attackee)
	{
		attackee.setHealth(attackee.getHealth() - getDamage());
	}

	/**
	 * Gets the damage that the actors deals
	 * @return the damage
	 */
	int getDamage()
	{
		return 1;
	}

	/**
	 * Gets a new random position in the bounds of cols, rows, and lvls
	 * @param cols the upper bound of the x pos
	 * @param rows the upper bound of the y pos
	 * @param lvls the upper bound of the z pos
	 */
	public void newPosition(int cols, int rows, int lvls)
	{
		java.util.Random r = new java.util.Random();
		pos = new Point3D(r.nextInt(cols), r.nextInt(rows), r.nextInt(lvls));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toJSONString()
	{
		JSONObject jb = new JSONObject();
		jb.put("name",this.name);
		jb.put("id",id);
		jb.put("X",((int)pos.getX()));
		jb.put("Y",((int)pos.getY()));
		jb.put("Z",pos.getZ());
		jb.put("health",health);
		jb.put("type", type.toString());
		jb.put("symbol", symbol);
		jb.put("color", color);
		return jb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "Actor{" +
		       "pos=" + pos +
		       ", name='" + name + '\'' +
		       ", id=" + id +
		       ", health=" + health +
		       ", color=" + color +
		       ", type=" + type +
		       ", symbol='" + symbol + '\'' +
		       '}';
	}

	public void setID(int ID)
	{
		this.id = ID;
	}


	/**
	 * These are the types an Actor can have
	 *
	 * @see Player
	 * @see AI
	 */
	public enum ActorType
	{
		ACTOR_PLAYER,
		ACTOR_AI
	}
}
