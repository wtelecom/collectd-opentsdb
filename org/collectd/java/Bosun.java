package org.collectd.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.collectd.api.Collectd;
import org.collectd.api.CollectdConfigInterface;
import org.collectd.api.CollectdInitInterface;
import org.collectd.api.CollectdWriteInterface;
import org.collectd.api.DataSource;
import org.collectd.api.OConfigItem;
import org.collectd.api.OConfigValue;
import org.collectd.api.ValueList;
import org.json.JSONException;
import org.json.JSONObject;

public class Bosun implements CollectdWriteInterface,
    CollectdInitInterface,
    CollectdConfigInterface
{
    private String      server = "localhost";
    private String      port   = "8070";
    private String 		custom_data = "";

    public Bosun ()
    {
        Collectd.registerInit   ("Bosun", this);
        Collectd.registerWrite  ("Bosun", this);
        Collectd.registerConfig ("Bosun", this);
    }

    public int init ()
    {
        
      Collectd.logInfo ("Bosun plugin: server: " + server + ", port: " + port);

        return(0);
    }

  public int write (ValueList vl)
  {
    List<DataSource> ds = vl.getDataSet().getDataSources();
    List<Number> values = vl.getValues();
    int size            = values.size();
    StringBuffer sb 	= new StringBuffer();
    Map<String, String> tagMap = new LinkedHashMap<String, String>();

    for (int i=0; i<size; i++) {
    	
    	tagMap.clear();
    	
    	sb.setLength(0);
        sb.append("put ");
    	
        // Metric name
        String    name, pointName,
                  plugin, pluginInstance,
                  type, typeInstance,
                  json;
        
        ArrayList<String> parts = new ArrayList<String>();
        ArrayList<String> tags = new ArrayList<String>();
        
        

        plugin         = vl.getPlugin();
        pluginInstance = vl.getPluginInstance();
        type           = vl.getType();
        typeInstance   = vl.getTypeInstance();

        Collectd.logInfo("plugin: " + plugin + " pluginInstance: " + pluginInstance + " type: " + type + " typeInstance: " + typeInstance);

        if ( plugin != null && !plugin.isEmpty() ) {
            parts.add(plugin);
            if ( pluginInstance != null && !pluginInstance.isEmpty() ) {
                tags.add(plugin + "_instance=" + pluginInstance);
                tagMap.put(plugin+"_instance", pluginInstance);
            }
            if ( type != null && !type.isEmpty()) {
                tags.add(plugin + "_type=" + type);
                tagMap.put(plugin+"_type", type);
            }
            if ( typeInstance != null && !typeInstance.isEmpty() ) {
                tags.add(plugin + "_type_instance=" + typeInstance);
                tagMap.put(plugin+"_type_instance", typeInstance);
            }

            pointName = ds.get(i).getName();
            if (!pointName.equals("value")) {
              // Collectd.logInfo("pointName: " + pointName);
              tags.add(plugin + "_point=" + pointName);
              tagMap.put(plugin + "_point", pointName);
            }
        }
        
        if (custom_data.length()>0){
        	tagMap.put("custom_data", custom_data);
        }

        name = join(parts, ".");

        sb.append(name).append(' ');

        // Time
        long time = vl.getTime() / 1000;
        sb.append(time).append(' ');

        // Value
        Number val = values.get(i);
        sb.append(val).append(' ');

        // Host
        String host = vl.getHost();
        sb.append("host=").append(host).append(" ");

        // Meta
        sb.append("source=collectd");

        sb.append(" ").append(join(tags, " "));
        
        Collectd.logInfo("StringBuffer: " + sb.toString());
        
        json = getJson(name, val, time, host, tagMap);
        Collectd.logInfo("JSON: " + json);
        // Send to Bosun
        httpPost(json, server, port);
    }

    return(0);
  }

  public static String join(Collection<String> s, String delimiter) {
      StringBuffer buffer = new StringBuffer();
      Iterator<String> iter = s.iterator();
      while (iter.hasNext()) {
          buffer.append(iter.next());
          if (iter.hasNext()) {
              buffer.append(delimiter);
          }
      }
      return buffer.toString();
  }

  public int config (OConfigItem ci) /* {{{ */
  {
    List<OConfigItem> children;
    int i;

    Collectd.logDebug ("Bosun plugin: config: ci = " + ci + ";");

    children = ci.getChildren ();
    for (i = 0; i < children.size (); i++)
    {
      List<OConfigValue> values;
      OConfigItem child;
      String key;

      child = children.get (i);
      key   = child.getKey ();
      if (key.equalsIgnoreCase ("Server"))
      {
        values = child.getValues();
        if (values.size () != 2)
        {
            Collectd.logError ("Bosun plugin: " + key +
                "configuration option needs exactly two arguments: server + port");
            return (1);
        } else {
            server = values.get(0).toString();
            port   = values.get(1).toString();
        }
      }
      else if (key.equalsIgnoreCase("custom_data"))
      {
    	  values = child.getValues();
    	  if (values.size() > 0)
    	  {
    		  custom_data = values.get(0).toString();
    	  }
    	  
      } else
      {
        Collectd.logError ("Bosun plugin: Unknown config option: " + key);
      }
    } /* for (i = 0; i < children.size (); i++) */

    return (0);
  } /* }}} int config */
  
	/*
	* Returns JSON well formed ready to send
	*/
  	public static String getJson(String metric_name, Number value, long timestamp, String hostname, Map<String, String> tagMap){
		
		// compone the JSON
		JSONObject json = new JSONObject();
		
		try {
			json.put("metric", metric_name);
			json.put("timestamp", timestamp);
			json.put("value", value);
			
			JSONObject tagsJson = new JSONObject();
			tagsJson.put("host", hostname);
			
			for (Map.Entry<String, String> entry : tagMap.entrySet())
			{
			    tagsJson.put(entry.getKey(), entry.getValue());
			}
			
			json.put("tags", tagsJson);
			
		} catch (JSONException e) {
			Collectd.logError("Error creating JSON string." + e.getStackTrace());
			e.printStackTrace();
		}
		return json.toString();			
	}
	
	@SuppressWarnings("deprecation")
	public static void httpPost(String json, String server, String port){
		HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
	    HttpResponse response = null;
	    try {
				
			HttpPost request = new HttpPost( "http://" + server + ":" + port+ "/api/put");
				
	        //StringEntity json_string =new StringEntity("{\"metric\":\"custom.my.metic\",\"timestamp\":" + unixTime + ",\"value\":14,\"tags\":{\"host\":\"hostname\"}}");
	        request.addHeader("content-type", "application/x-www-form-urlencoded");
	        request.setEntity(new StringEntity(json));
	        response = httpClient.execute(request);
	
	        // handle response here...
	        int status_code = response.getStatusLine().getStatusCode();
	        if ( status_code > 207){
	        	Collectd.logError( "Request sent failed. Status " + status_code);
	        } 
//	        else {
//	        	Collectd.logInfo("Request succes. Status code: " + status_code);
//	        }
	    }catch (Exception ex) {
	        // handle exception here
	    	Collectd.logError("Error sending json request: " + ex.toString());
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
	}
	
  
}
