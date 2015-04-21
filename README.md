# collectd-opentsdb

collectd writer plugin for OpenTSDB.

Install
-------
    # Install collectd.
    apt-get install collectd
    # Clone the plugin repo.
    git clone git://github.com/auxesis/collectd-opentsdb.git
    # Compile the plugin
    cd collectd-opentsdb
    javac -classpath /usr/share/collectd-core/java/collectd-api.jar org/collectd/java/OpenTSDB.java


Insert this into your `collectd.conf` (likely at `/etc/collectd/collectd.conf`):

    LoadPlugin java

    <Plugin java>
      JVMArg "-Djava.class.path=/usr/share/collectd-core/java/collectd-api.jar:/path/to/collectd-opentsdb/"
        LoadPlugin "org.collectd.java.OpenTSDB"
        <Plugin "OpenTSDB">
        Server "localhost" "4242"
      </Plugin>
    </Plugin>

Restart collectd.

# collectd-bosun

opentsdb writer java based plugin for Bosun

Dependencies
-------
Download [ org.json ](https://org-json-java.googlecode.com/files/org.json-20120521.jar) java library.

    [ -d foo ] || mkdir /opt/collectd-bosun-dependencies
    cd /opt/collectd-bosun-dependencies
    apt-get install wget
    wget -O  org.json.jar https://org-json-java.googlecode.com/files/org.json-20120521.jar

Get the [apache http client]( http://hc.apache.org/downloads.cgi) library

    [ -d foo ] || mkdir /opt/collectd-bosun-dependencies
    cd /opt/collectd-bosun-dependencies
    wget -O httpcomponents-client.tar.gz  http://apache.rediris.es//httpcomponents/httpclient/binary/httpcomponents-client-4.4.1-bin.tar.gz
     mkdir httpcomponents-client && tar xf httpcomponents-client.tar.gz -C httpcomponents-client --strip-components 1

Install and configure collectd
-------

    # Install collectd.
    apt-get install collectd
    # we need to use a specific directory
    cd /opt/
    # Clone the plugin repo.
    git clone git://github.com/auxesis/collectd-opentsdb.git collectd-opentsdb
    # Compile the plugin
    cd collectd-opentsdb
      CLASSPATH="/opt/collectd-bosun-dependencies/httpcomponents-client/lib/commons-codec-1.9.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/fluent-hc-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpclient-cache-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpcore-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/jna-4.1.0.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/commons-logging-1.2.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpclient-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpclient-win-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpmime-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/jna-platform-4.1.0.jar:/usr/share/collectd/java/collectd-api.jar:/opt/collectd-bosun-dependencies/org.json.jar:."
    javac -classpath $CLASSPATH org/collectd/java/Bosun.java

If no errores appears, `Bosun.class` should be created in `org/collectd/java/` folder.

Insert this into your `/etc/collectd/collectd.conf` (or in `/etc/collectd/collectd.conf.d/bosun.conf`):

    LoadPlugin java

    <Plugin java>
      JVMArg "-Djava.class.path=/opt/collectd-bosun-dependencies/httpcomponents-client/lib/commons-codec-1.9.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/fluent-hc-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpclient-cache-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpcore-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/jna-4.1.0.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/commons-logging-1.2.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpclient-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpclient-win-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/httpmime-4.4.1.jar:/opt/collectd-bosun-dependencies/httpcomponents-client/lib/jna-platform-4.1.0.jar:/usr/share/collectd/java/collectd-api.jar:/opt/collectd-bosun-dependencies/org.json.jar:/home/soto/src/collectd-opentsdb/"

      LoadPlugin "org.collectd.java.Bosun"
      <Plugin "Bosun">
        Server "localhost" "8070"
        custom_data "deviceId0"
      </Plugin>
    </Plugin>
You can test your configuration executing:

    collectd -T -f

`-T` test the config and `-f` disable the daemon forking. If no errores appears you can restart collectd daemon:

    service collectd restart
