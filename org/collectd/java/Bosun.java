
import org.collectd.api.Collectd;
import org.collectd.api.ValueList;
import org.collectd.api.DataSet;
import org.collectd.api.DataSource;
import org.collectd.api.CollectdConfigInterface;
import org.collectd.api.CollectdInitInterface;
import org.collectd.api.CollectdWriteInterface;
import org.collectd.api.OConfigValue;
import org.collectd.api.OConfigItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class Bosun implements CollectdWriteInterface,
    CollectdInitInterface,
    CollectdConfigInterface
{
    private String    server = "localhost";
    private String    port   = "8070";
    private String    plugin_name = "Bosun";


    public Bosun ()
    {
        Collectd.registerInit   (plugin_name, this);
        Collectd.registerWrite  (plugin_name, this);
        Collectd.registerConfig (plugin_name, this);
    }

    public int init ()
    {
        try {
          Collectd.logInfo ("Bosun plugin: server: " + server + ", port: " + port);
          
        } catch (UnknownHostException e) {
          Collectd.logError ("Couldn't establish connection: " + e.getMessage());
          System.exit(1);
        } 

        return(0);
    }

  public int write (ValueList vl)
  {
    List<DataSource> ds = vl.getDataSet().getDataSources();
    List<Number> values = vl.getValues();
    int size            = values.size();

    for (int i=0; i<size; i++) {
        
        // Metric name
        String    name, 
                  pointName,
                  plugin, 
                  pluginInstance,
                  type, 
                  typeInstance;
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
          }
          if ( type != null && !type.isEmpty()) {
              tags.add(plugin + "_type=" + type);
          }
          if ( typeInstance != null && !typeInstance.isEmpty() ) {
              tags.add(plugin + "_type_instance=" + typeInstance);
          }

          pointName = ds.get(i).getName();
          if (!pointName.equals("value")) {
            // Collectd.logInfo("pointName: " + pointName);
            tags.add(plugin + "_point=" + pointName);
          }
        }

        

        sb.append("\"name\":" + join(parts, ".") + "\",");

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

        String output = sb.toString();

        // Send to Bosun
        String json =this.sendJSON()
        Collectd.logInfo(output);
    }

    return(0);
  }

  /*
  *  JSON format:
  *
  *    "{\"metric\":\"custom.my.metic\",\"timestamp\":" + unixTime + ",\"value\":14,\"tags\":{\"host\":\"ssotoTest16\"}}"
  *
  */
  private int sendJSON(String json){
    
    HttpClient httpClient = HttpClientBuilder.create().build(); 
    int result = 0;
    try {
        HttpPost request = new HttpPost( "http://" + this.server + this.port+ "/api/post");
        StringEntity params =new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"} ");
        request.addHeader("content-type", "application/x-www-form-urlencoded");
        request.setEntity(params);
        HttpResponse response = httpClient.execute(request);

        // handle response here...
    }catch (Exception ex) {
        // handle exception here
        Collectd.logError("Error sending json request: " + ex.toString());
        result = 1;
    } finally {
        httpClient.getConnectionManager().shutdown();
        return result;
    }
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
      else
      {
        Collectd.logError ("Bosun plugin: Unknown config option: " + key);
      }
    } /* for (i = 0; i < children.size (); i++) */

    return (0);
  } /* }}} int config */
}
