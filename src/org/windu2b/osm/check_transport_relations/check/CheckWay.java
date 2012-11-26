/**
 * License: GPL v3 or later. For details, see LICENSE file.
 */
package org.windu2b.osm.check_transport_relations.check;

import static org.windu2b.osm.check_transport_relations.tools.I18n.tr;

import org.windu2b.osm.check_transport_relations.data.osm.OsmPrimitiveType;
import org.windu2b.osm.check_transport_relations.data.osm.RelationMember;
import org.windu2b.osm.check_transport_relations.data.osm.Way;
import org.windu2b.osm.check_transport_relations.io.Log;
import org.windu2b.osm.check_transport_relations.io.OsmTransferException;

/**
 * @author windu
 * 
 */
public class CheckWay extends AbstractCheck
{

	public CheckWay( Check check )
	{
		super( check );
	}




	/*
	 * (non-Javadoc)
	 * 
	 * @see org.windu2b.osm.check_transport_relations.check.ICheck#check()
	 */
	@Override
	public boolean check( RelationMember rm ) throws OsmTransferException
	{
		if( rm.getDisplayType() == OsmPrimitiveType.WAY )
		{
			LastElements.setLastStopPosition( null );
			Way way = ( Way ) rm.getMember();

			Way lastWay = LastElements.getLastWay();
			if( lastWay != null )
			{
				/**
				 * Si le précédent way et l'actuel n'ont ni leur premier ni leur
				 * dernier point en commun => ils ne sont pas adjacents On lève
				 * alors une exception
				 * 
				 * @TODO : n'envoyer qu'un message d'erreur et continuer le
				 *       traitement
				 */
				if( !areContiguousWays( way, lastWay ) )
				{
					// On pointe tout de même sur le 'way' en cours, pour éviter de provoquer des erreurs en cascade
					LastElements.setLastWay( way );
				
					Log.log( tr( "Ways {0} and {1} are not adjacent",
					        lastWay.getId(), way.getId() ) );

					return false;
				}
			}

			// On pointe sur le 'way' en cours
			LastElements.setLastWay( way );
		}
		else
		{
			return this.check.setState( this.check.stop_position ).check( rm );
		}

		return true;
	}




	public static boolean areContiguousWays( Way w1, Way w2 )
	{
		return w1.getFirstNode().equals( w2.getFirstNode() )
		        || w1.getLastNode().equals( w2.getLastNode() )
		        || w1.getLastNode().equals( w2.getFirstNode() )
		        || w1.getFirstNode().equals( w2.getLastNode() );
	}

}