/**
 * License: GPL v3 or later. For details, see LICENSE file.
 */
package org.windu2b.osm.check_transport_relations.check;

import org.windu2b.osm.check_transport_relations.data.osm.Node;
import org.windu2b.osm.check_transport_relations.data.osm.Relation;
import org.windu2b.osm.check_transport_relations.data.osm.Way;

/**
 * @author windu
 * 
 */
public final class LastElements
{

	protected static Way	  lastWay	       = null;


	protected static Node	  lastStopPosition	= null;


	protected static Relation	lastStopArea	= null;




	/**
	 * @return the lastWay
	 */
	public static final Way getLastWay()
	{
		return lastWay;
	}




	/**
	 * @param lastWay
	 *            the lastWay to set
	 */
	public static final Way setLastWay( Way lastWay )
	{
		LastElements.lastWay = lastWay;

		return lastWay;
	}




	/**
	 * @return the lastStopPosition
	 */
	public static final Node getLastStopPosition()
	{
		return lastStopPosition;
	}




	/**
	 * @param lastStopPosition
	 *            the lastStopPosition to set
	 */
	public static final Node setLastStopPosition( Node lastStopPosition )
	{
		LastElements.lastStopPosition = lastStopPosition;

		return lastStopPosition;
	}




	/**
	 * @return the stopArea
	 */
	public static final Relation getLastStopArea()
	{
		return lastStopArea;
	}




	/**
	 * @param lastStopArea
	 *            the stopArea to set
	 */
	public static final Relation setLastStopArea( Relation lastStopArea )
	{
		LastElements.lastStopArea = lastStopArea;

		return lastStopArea;
	}




	public static void reset()
	{
		LastElements.lastStopPosition = null;
		LastElements.lastWay = null;
		LastElements.lastStopArea = null;
	}

}
