
// download from https://code.google.com/p/org-json-java/downloads/detail?name=org.json-20120521.jar
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;


public class test {
	
	public static String getJson(String metric_name, int value, long timestamp, String hostname ){
		
		// compone the JSON
		JSONObject json = new JSONObject();
		StringEntity json_string = null;
		
		try {
			json.put("metric", metric_name);
			json.put("timestamp", timestamp);
			json.put("value", value);
			
			JSONObject tagsJson = new JSONObject();
			tagsJson.put("host", hostname);
			
			json.put("tags", tagsJson);
			
			json_string =new StringEntity(json.toString());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json_string.toString();			
	}
	
	public static void httpPost(String json, String server, int port){
		HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
	    HttpResponse response = null;
	    try {
				
			HttpPost request = new HttpPost( "http://" + server + ":" + port+ "/api/put");
				
	        //StringEntity json_string =new StringEntity("{\"metric\":\"custom.my.metic\",\"timestamp\":" + unixTime + ",\"value\":14,\"tags\":{\"host\":\"ssotoTest16\"}}");
	        request.addHeader("content-type", "application/x-www-form-urlencoded");
	        request.setEntity(new StringEntity(json));
	        response = httpClient.execute(request);

	        // handle response here...
	        int status_code = response.getStatusLine().getStatusCode();
	        if (status_code > 207){
	        	System.err.println("Request sent failed. Status " + status_code);
	        }
	    }catch (Exception ex) {
	        // handle exception here
	        System.err.println("Error sending json request: " + ex.toString());
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String server = "localhost";
		int port = 8070;
		String metric_name = "custom.my.metric";
		int value = 10;
		long unixTime = System.currentTimeMillis() / 1000L;
		String hostname = "ssotoTest16";
		
		String json = getJson(metric_name, value, unixTime, server);
		httpPost(json, server, port);
		
	}
}
