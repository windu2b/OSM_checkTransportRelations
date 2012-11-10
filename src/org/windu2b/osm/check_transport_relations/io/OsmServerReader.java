// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.windu2b.osm.check_transport_relations.io;

import static org.windu2b.osm.check_transport_relations.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.windu2b.osm.check_transport_relations.data.DataSet;
import org.windu2b.osm.check_transport_relations.gui.progress.ProgressMonitor;

/**
 * This DataReader reads directly from the REST API of the osm server.
 * 
 * It supports plain text transfer as well as gzip or deflate encoded transfers;
 * if compressed transfers are unwanted, set property osm-server.use-compression
 * to false.
 * 
 * @author imi
 */
public abstract class OsmServerReader extends OsmConnection
{
	private OsmApi	  api	         = OsmApi.getOsmApi();


	private boolean	  doAuthenticate	= false;


	protected boolean	gpxParsedProperly;




	/**
	 * Open a connection to the given url and return a reader on the input
	 * stream from that connection. In case of user cancel, return
	 * <code>null</code>.
	 * 
	 * @param urlStr
	 *            The exact url to connect to.
	 * @param pleaseWaitDlg
	 * @return An reader reading the input stream (servers answer) or
	 *         <code>null</code>.
	 */
	protected InputStream getInputStream( String urlStr,
	        ProgressMonitor progressMonitor ) throws OsmTransferException
	{
		try
		{
			urlStr = urlStr.startsWith( "http" ) ? urlStr
			        : ( getBaseUrl() + urlStr );
			return getInputStreamRaw( urlStr, progressMonitor );
		}
		finally
		{
			progressMonitor.invalidate();
		}
	}




	protected String getBaseUrl()
	{
		return api.getBaseUrl();
	}




	protected InputStream getInputStreamRaw( String urlStr,
	        ProgressMonitor progressMonitor ) throws OsmTransferException
	{
		try
		{
			URL url = null;
			try
			{
				url = new URL( urlStr.replace( " ", "%20" ) );
			}
			catch ( MalformedURLException e )
			{
				throw new OsmTransferException( e );
			}
			try
			{
				activeConnection = ( HttpURLConnection ) url.openConnection();
				// fix #7640, see
				// http://www.tikalk.com/java/forums/httpurlconnection-disable-keep-alive
				activeConnection.setRequestProperty( "Connection", "close" );
			}
			catch ( Exception e )
			{
				throw new OsmTransferException( tr(
				        "Failed to open connection to API {0}.",
				        url.toExternalForm() ), e );
			}
			if ( cancel )
			{
				activeConnection.disconnect();
				return null;
			}

			if ( cancel ) throw new OsmTransferCanceledException();

			activeConnection.setConnectTimeout( 15 * 1000 );

			try
			{
				System.out.println( "GET " + url );
				activeConnection.connect();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
				throw new OsmTransferException(
				        tr( "Could not connect to the OSM server. Please check your internet connection." ),
				        e );
			}
			try
			{
				if ( activeConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED )
				    throw new OsmApiException(
				            HttpURLConnection.HTTP_UNAUTHORIZED, null, null );

				if ( activeConnection.getResponseCode() == HttpURLConnection.HTTP_PROXY_AUTH )
				    throw new OsmTransferCanceledException();

				String encoding = activeConnection.getContentEncoding();
				if ( activeConnection.getResponseCode() != HttpURLConnection.HTTP_OK )
				{
					String errorHeader = activeConnection
					        .getHeaderField( "Error" );
					StringBuilder errorBody = new StringBuilder();
					try
					{
						InputStream i = FixEncoding(
						        activeConnection.getErrorStream(), encoding );
						if ( i != null )
						{
							BufferedReader in = new BufferedReader(
							        new InputStreamReader( i ) );
							String s;
							while ( ( s = in.readLine() ) != null )
							{
								errorBody.append( s );
								errorBody.append( "\n" );
							}
						}
					}
					catch ( Exception e )
					{
						errorBody.append( tr( "Reading error text failed." ) );
					}

					throw new OsmApiException(
					        activeConnection.getResponseCode(), errorHeader,
					        errorBody.toString() );
				}

				return FixEncoding( new ProgressInputStream( activeConnection,
				        progressMonitor ), encoding );
			}
			catch ( Exception e )
			{
				if ( e instanceof OsmTransferException )
					throw ( OsmTransferException ) e;
				else throw new OsmTransferException( e );

			}
		}
		finally
		{
			progressMonitor.invalidate();
		}
	}




	private InputStream FixEncoding( InputStream stream, String encoding )
	        throws IOException
	{
		if ( encoding != null && encoding.equalsIgnoreCase( "gzip" ) )
		{
			stream = new GZIPInputStream( stream );
		}
		else if ( encoding != null && encoding.equalsIgnoreCase( "deflate" ) )
		{
			stream = new InflaterInputStream( stream, new Inflater( true ) );
		}
		return stream;
	}




	public abstract DataSet parseOsm( final ProgressMonitor progressMonitor )
	        throws OsmTransferException;




	/**
	 * Returns true if this reader is adding authentication credentials to the
	 * read request sent to the server.
	 * 
	 * @return true if this reader is adding authentication credentials to the
	 *         read request sent to the server
	 */
	public boolean isDoAuthenticate()
	{
		return doAuthenticate;
	}




	/**
	 * Sets whether this reader adds authentication credentials to the read
	 * request sent to the server.
	 * 
	 * @param doAuthenticate
	 *            true if this reader adds authentication credentials to the
	 *            read request sent to the server
	 */
	public void setDoAuthenticate( boolean doAuthenticate )
	{
		this.doAuthenticate = doAuthenticate;
	}




	/**
	 * Determines if the GPX data has been parsed properly.
	 * 
	 * @return true if the GPX data has been parsed properly, false otherwise
	 * @see GpxReader#parse
	 */
	public final boolean isGpxParsedProperly()
	{
		return gpxParsedProperly;
	}
}
