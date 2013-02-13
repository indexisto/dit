/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.handler.dataimport;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.XMLErrorLogger;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.dataimport.config.ConfigNameConstants;
import org.apache.solr.handler.dataimport.config.ConfigParseUtil;
import org.apache.solr.handler.dataimport.config.DIHConfiguration;
import org.apache.solr.handler.dataimport.config.Entity;
import org.apache.solr.handler.dataimport.config.Script;
import org.apache.solr.handler.dataimport.datasource.DataSource;
import org.apache.solr.handler.dataimport.datasource.JdbcDataSource;
import org.apache.solr.handler.dataimport.processor.SqlEntityProcessor;
import org.apache.solr.handler.dataimport.properties.DIHPropertiesWriter;
import org.apache.solr.handler.dataimport.properties.SimplePropertiesWriter;
import org.apache.solr.handler.dataimport.writer.SolrWriter;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.indexisto.dit.data.exception.MappingException;
//import org.apache.solr.core.SolrCore;

/**
 * <p> Stores all configuration information for pulling and indexing data. </p>
 * <p/>
 * <b>This API is experimental and subject to change</b>
 *
 * @since solr 1.3
 */
public class DataImporter {

  public enum Status {
    IDLE, RUNNING_FULL_DUMP, RUNNING_DELTA_DUMP, JOB_FAILED
  }

  private static final Logger LOG = LoggerFactory.getLogger(DataImporter.class);
  private static final XMLErrorLogger XMLLOG = new XMLErrorLogger(LOG);

  private Status status = Status.IDLE;
  private DIHConfiguration config;
  private Date indexStartTime;
  private final Properties store = new Properties();
  private Map<String, Map<String,String>> requestLevelDataSourceProps = new HashMap<String, Map<String,String>>();
  private IndexSchema schema;
  public DocBuilder docBuilder;
  public DocBuilder.Statistics cumulativeStatistics = new DocBuilder.Statistics();
  private SolrCore core;
  private final Map<String, Object> coreScopeSession = new ConcurrentHashMap<String,Object>();
  private DIHPropertiesWriter propWriter;
  private final ReentrantLock importLock = new ReentrantLock();
  private boolean isDeltaImportSupported = false;
  private final String handlerName;
  //Solr cut off
  //private final Map<String, SchemaField> lowerNameVsSchemaField = new HashMap<String, SchemaField>();

  // Vladimir Mikhel
  private String defaultDataSource;
  /**
   * Only for testing purposes
   */
  // Vladimir Mikhel: changed to public
  public DataImporter() {
    createPropertyWriter();
    propWriter.init(this);
    handlerName = "dataimport" ;
  }

  //Solr cut off
  /*DataImporter(SolrCore core, String handlerName) {
    this.handlerName = handlerName;
    this.core = core;
    schema = core.getSchema();
    loadSchemaFieldMap();
    createPropertyWriter();
  }*/

  private void createPropertyWriter() {
    // Solr cut off
    /*
    if (core == null
        || !core.getCoreDescriptor().getCoreContainer().isZooKeeperAware()) {
      propWriter = new SimplePropertiesWriter();
    } else {
      propWriter = new ZKPropertiesWriter();
    }
    */
    propWriter = new SimplePropertiesWriter();

    propWriter.init(this);
  }


  boolean maybeReloadConfiguration(RequestInfo params,
      NamedList<?> defaultParams) throws IOException {
  if (importLock.tryLock()) {
      boolean success = false;
      try {
        final String dataConfigText = params.getDataConfig();
        params.getConfigFile();
        InputSource is = null;

        // Solr cut off
        /*
        if(dataConfigText!=null && dataConfigText.length()>0) {
          is = new InputSource(new StringReader(dataConfigText));
        } else if(dataconfigFile!=null) {
          is = new InputSource(core.getResourceLoader().openResource(dataconfigFile));
          is.setSystemId(SystemIdResolver.createSystemIdFromResourceName(dataconfigFile));
          LOG.info("Loading DIH Configuration: " + dataconfigFile);
        }
        */
        if (dataConfigText == null && dataConfigText.length() > 0)
            throw new MappingException("There is no data config text");
        is = new InputSource(new StringReader(dataConfigText));

        if(is!=null) {
          loadDataConfig(is);
          success = true;
        }

        final Map<String,Map<String,String>> dsProps = new HashMap<String,Map<String,String>>();
        if(defaultParams!=null) {
          int position = 0;
          while (position < defaultParams.size()) {
            if (defaultParams.getName(position) == null) {
              break;
            }
            final String name = defaultParams.getName(position);
            if (name.equals("datasource")) {
              success = true;
              final NamedList dsConfig = (NamedList) defaultParams.getVal(position);
              LOG.info("Getting configuration for Global Datasource...");
              final Map<String,String> props = new HashMap<String,String>();
              for (int i = 0; i < dsConfig.size(); i++) {
                props.put(dsConfig.getName(i), dsConfig.getVal(i).toString());
              }
              LOG.info("Adding properties to datasource: " + props);
              dsProps.put((String) dsConfig.get("name"), props);
            }
            position++;
          }
        }
        requestLevelDataSourceProps = Collections.unmodifiableMap(dsProps);
      } catch(final Exception ioe) {
        throw ioe;
      } finally {
        importLock.unlock();
      }
      return success;
    } else {
      return false;
    }
  }


  //Solr cut off
  /*private void loadSchemaFieldMap() {
    final Map<String, SchemaField> modLnvsf = new HashMap<String, SchemaField>();
    for (final Map.Entry<String, SchemaField> entry : schema.getFields().entrySet()) {
      modLnvsf.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
    }
    lowerNameVsSchemaField = Collections.unmodifiableMap(modLnvsf);
  }*/

  // Solr cut off
  /*public SchemaField getSchemaField(String caseInsensitiveName) {
    SchemaField schemaField = null;

    if(schema!=null) {
      schemaField = schema.getFieldOrNull(caseInsensitiveName);
    }
    if (schemaField == null) {
      schemaField = lowerNameVsSchemaField.get(caseInsensitiveName.toLowerCase(Locale.ROOT));
    }
    return schemaField;
  }*/

   public String getHandlerName() {
        return handlerName;
    }



  /**
   * Used by tests
   */
  // Vladimir Mikhel: changed to public
  public void loadAndInit(String configStr) {
    loadDataConfig(new InputSource(new StringReader(configStr)));
  }

  private void loadDataConfig(InputSource configFile) {

    try {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

      // only enable xinclude, if a a SolrCore and SystemId is present (makes no sense otherwise)
      if (core != null && configFile.getSystemId() != null) {
        try {
          dbf.setXIncludeAware(true);
          dbf.setNamespaceAware(true);
        } catch( final UnsupportedOperationException e ) {
          LOG.warn( "XML parser doesn't support XInclude option" );
        }
      }

      final DocumentBuilder builder = dbf.newDocumentBuilder();
      // Solr cut off
      //if (core != null)
      //  builder.setEntityResolver(new SystemIdResolver(core.getResourceLoader()));
      builder.setErrorHandler(XMLLOG);
      Document document;
      try {
        document = builder.parse(configFile);
      } finally {
        // some XML parsers are broken and don't close the byte stream (but they should according to spec)
        IOUtils.closeQuietly(configFile.getByteStream());
      }

      config = readFromXml(document);
      LOG.info("Data Configuration loaded successfully");
    } catch (final Exception e) {
      throw new DataImportHandlerException(SEVERE,
              "Data Config problem: " + e.getMessage(), e);
    }
    for (final Entity e : config.getEntities()) {
      if (e.getAllAttributes().containsKey(SqlEntityProcessor.DELTA_QUERY)) {
        isDeltaImportSupported = true;
        break;
      }
    }
  }

  public DIHConfiguration readFromXml(Document xmlDocument) {
    final List<Map<String, String >> functions = new ArrayList<Map<String ,String>>();
    Script script = null;
    final Map<String, Map<String,String>> dataSources = new HashMap<String, Map<String,String>>();

    final NodeList dataConfigTags = xmlDocument.getElementsByTagName("dataConfig");
    if(dataConfigTags == null || dataConfigTags.getLength() == 0) {
      throw new DataImportHandlerException(SEVERE, "the root node '<dataConfig>' is missing");
    }
    final Element e = (Element) dataConfigTags.item(0);
    final List<Element> documentTags = ConfigParseUtil.getChildNodes(e, "document");
    if (documentTags.isEmpty()) {
      throw new DataImportHandlerException(SEVERE, "DataImportHandler " +
              "configuration file must have one <document> node.");
    }

    final List<Element> scriptTags = ConfigParseUtil.getChildNodes(e, ConfigNameConstants.SCRIPT);
    if (!scriptTags.isEmpty()) {
      script = new Script(scriptTags.get(0));
    }

    // Add the provided evaluators
    final List<Element> functionTags = ConfigParseUtil.getChildNodes(e, ConfigNameConstants.FUNCTION);
    if (!functionTags.isEmpty()) {
      for (final Element element : functionTags) {
        final String func = ConfigParseUtil.getStringAttribute(element, NAME, null);
        final String clz = ConfigParseUtil.getStringAttribute(element, ConfigNameConstants.CLASS, null);
        if (func == null || clz == null){
          throw new DataImportHandlerException(
                  SEVERE,
                  "<function> must have a 'name' and 'class' attributes");
        } else {
          functions.add(ConfigParseUtil.getAllAttributes(element));
        }
      }
    }
    final List<Element> dataSourceTags = ConfigParseUtil.getChildNodes(e, DATA_SRC);
    if (!dataSourceTags.isEmpty()) {
      for (final Element element : dataSourceTags) {
        final Map<String,String> p = new HashMap<String,String>();
        final HashMap<String, String> attrs = ConfigParseUtil.getAllAttributes(element);
        for (final Map.Entry<String, String> entry : attrs.entrySet()) {
          p.put(entry.getKey(), entry.getValue());
        }
        dataSources.put(p.get("name"), p);
      }
    }
    if(dataSources.get(null) == null){
      for (final Map<String,String> properties : dataSources.values()) {
        dataSources.put(null,properties);
        break;
      }
    }
    return new DIHConfiguration(documentTags.get(0), this, functions, script, dataSources);
  }

  DIHConfiguration getConfig() {
    return config;
  }

  Date getIndexStartTime() {
    return indexStartTime;
  }

  void setIndexStartTime(Date indextStartTime) {
    indexStartTime = indextStartTime;
  }

  void store(Object key, Object value) {
    store.put(key, value);
  }

  Object retrieve(Object key) {
    return store.get(key);
  }

  DataSource getDataSourceInstance(Entity key, String name, Context ctx) {
    Map<String,String> p = requestLevelDataSourceProps.get(name);
    if (p == null)
      p = config.getDataSources().get(name);
    if (p == null)
      p = requestLevelDataSourceProps.get(null);// for default data source
    if (p == null)
      p = config.getDataSources().get(null);
    // Vladimir Mikhel: changed to SnatcherDataSource
    if (p == null && datasource != null && datasource.length() > 0) {
        p = new HashMap<String, String>();
        p.put(TYPE, datasource);
    }
    if (p == null)
      throw new DataImportHandlerException(SEVERE,
              "No dataSource :" + name + " available for entity :" + key.getName());
    final String type = p.get(TYPE);

    DataSource dataSrc = null;
    if (type == null) {
      // Vladimir Mikhel: be aware of this, ad custom datasource
      dataSrc = new JdbcDataSource();
    } else {
      try {
        dataSrc = (DataSource) DocBuilder.loadClass(type, getCore()).newInstance();
      } catch (final Exception e) {
        wrapAndThrow(SEVERE, e, "Invalid type for data source: " + type);
      }
    }
    try {
      final Properties copyProps = new Properties();
      copyProps.putAll(p);
      final Map<String, Object> map = ctx.getRequestParameters();
      if (map.containsKey("rows")) {
        int rows = Integer.parseInt((String) map.get("rows"));
        if (map.containsKey("start")) {
          rows += Integer.parseInt((String) map.get("start"));
        }
        copyProps.setProperty("maxRows", String.valueOf(rows));
      }
      // Vladimir Mikhel: changed to SnatcherDataSource
      if (dataSourceParams != null && dataSourceParams.size() > 0) {
    	  final Set<String> paramKeys = dataSourceParams.keySet();
    	  for (final String paramKey : paramKeys) {
    		copyProps.setProperty(paramKey, String.valueOf(dataSourceParams.get(paramKey)));
    	  }
      }
      dataSrc.init(ctx, copyProps);
    } catch (final Exception e) {
      wrapAndThrow(SEVERE, e, "Failed to initialize DataSource: " + key.getDataSourceName());
    }
    return dataSrc;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public boolean isBusy() {
    return importLock.isLocked();
  }

  public void doFullImport(SolrWriter writer, RequestInfo requestParams) {
    LOG.info("Starting Full Import");
    setStatus(Status.RUNNING_FULL_DUMP);

    setIndexStartTime(new Date());

    try {
      docBuilder = new DocBuilder(this, writer, propWriter, requestParams);
      checkWritablePersistFile(writer);
      docBuilder.execute();
      if (!requestParams.isDebug())
        cumulativeStatistics.add(docBuilder.importStatistics);
    // Vladimir Mikhel
    } catch (final DataImportHandlerException t) {
    	LOG.error("Full Import failed");
    	throw t;
    ///
    } catch (final Throwable t) {
      SolrException.log(LOG, "Full Import failed", t);
      docBuilder.rollback();
    } finally {
      setStatus(Status.IDLE);
      DocBuilder.INSTANCE.set(null);
    }

  }

  private void checkWritablePersistFile(SolrWriter writer) {
//    File persistFile = propWriter.getPersistFile();
//    boolean isWritable = persistFile.exists() ? persistFile.canWrite() : persistFile.getParentFile().canWrite();
    if (isDeltaImportSupported && !propWriter.isWritable()) {
      throw new DataImportHandlerException(SEVERE,
          "Properties is not writable. Delta imports are supported by data config but will not work.");
    }
  }

  public void doDeltaImport(SolrWriter writer, RequestInfo requestParams) {
    LOG.info("Starting Delta Import");
    setStatus(Status.RUNNING_DELTA_DUMP);

    try {
      setIndexStartTime(new Date());
      docBuilder = new DocBuilder(this, writer, propWriter, requestParams);
      checkWritablePersistFile(writer);
      docBuilder.execute();
      if (!requestParams.isDebug())
        cumulativeStatistics.add(docBuilder.importStatistics);
    } catch (final Throwable t) {
      LOG.error("Delta Import Failed", t);
      docBuilder.rollback();
    } finally {
      setStatus(Status.IDLE);
      DocBuilder.INSTANCE.set(null);
    }

  }

  public void runAsync(final RequestInfo reqParams, final SolrWriter sw) {
    new Thread() {
      @Override
      public void run() {
        runCmd(reqParams, sw);
      }
    }.start();
  }

  //Vladimir Mikhel: added params
  Map<String, Object> dataSourceParams;
  String datasource;

  // Vladimir Mikhel: changed to public
  public void runCmd(RequestInfo reqParams, SolrWriter sw) {
	datasource = reqParams.getDatasource();
	dataSourceParams = reqParams.getDataSourceParams();

    final String command = reqParams.getCommand();
    if (command.equals(ABORT_CMD)) {
      if (docBuilder != null) {
        docBuilder.abort();
      }
      return;
    }
    if (!importLock.tryLock()){
      LOG.warn("Import command failed . another import is running");
      return;
    }
    try {
      if (FULL_IMPORT_CMD.equals(command) || IMPORT_CMD.equals(command)) {
        doFullImport(sw, reqParams);
      } else if (command.equals(DELTA_IMPORT_CMD)) {
        doDeltaImport(sw, reqParams);
      }
    } finally {
      importLock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  Map<String, String> getStatusMessages() {
    //this map object is a Collections.synchronizedMap(new LinkedHashMap()). if we
    // synchronize on the object it must be safe to iterate through the map
    final Map statusMessages = (Map) retrieve(STATUS_MSGS);
    final Map<String, String> result = new LinkedHashMap<String, String>();
    if (statusMessages != null) {
      synchronized (statusMessages) {
        for (final Object o : statusMessages.entrySet()) {
          final Map.Entry e = (Map.Entry) o;
          //the toString is taken because some of the Objects create the data lazily when toString() is called
          result.put((String) e.getKey(), e.getValue().toString());
        }
      }
    }
    return result;

  }

  DocBuilder getDocBuilder() {
    return docBuilder;
  }

  public static final ThreadLocal<AtomicLong> QUERY_COUNT = new ThreadLocal<AtomicLong>() {
    @Override
    protected AtomicLong initialValue() {
      return new AtomicLong();
    }
  };

  public static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
  };

  static final class MSG {
    public static final String NO_CONFIG_FOUND = "Configuration not found";

    public static final String NO_INIT = "DataImportHandler started. Not Initialized. No commands can be run";

    public static final String INVALID_CONFIG = "FATAL: Could not create importer. DataImporter config invalid";

    public static final String LOAD_EXP = "Exception while loading DataImporter";

    public static final String JMX_DESC = "Manage data import from databases to Solr";

    public static final String CMD_RUNNING = "A command is still running...";

    public static final String DEBUG_NOT_ENABLED = "Debug not enabled. Add a tag <str name=\"enableDebug\">true</str> in solrconfig.xml";

    public static final String CONFIG_RELOADED = "Configuration Re-loaded sucessfully";

    public static final String CONFIG_NOT_RELOADED = "Configuration NOT Re-loaded...Data Importer is busy.";

    public static final String TOTAL_DOC_PROCESSED = "Total Documents Processed";

    public static final String TOTAL_FAILED_DOCS = "Total Documents Failed";

    public static final String TOTAL_QUERIES_EXECUTED = "Total Requests made to DataSource";

    public static final String TOTAL_ROWS_EXECUTED = "Total Rows Fetched";

    public static final String TOTAL_DOCS_DELETED = "Total Documents Deleted";

    public static final String TOTAL_DOCS_SKIPPED = "Total Documents Skipped";
  }

  public IndexSchema getSchema() {
    return schema;
  }

  public SolrCore getCore() {
    return core;
  }

  void putToCoreScopeSession(String key, Object val) {
    coreScopeSession.put(key, val);
  }
  Object getFromCoreScopeSession(String key) {
    return coreScopeSession.get(key);
  }

  public static final String COLUMN = "column";

  public static final String TYPE = "type";

  public static final String DATA_SRC = "dataSource";

  public static final String MULTI_VALUED = "multiValued";

  public static final String NAME = "name";

  public static final String STATUS_MSGS = "status-messages";

  public static final String FULL_IMPORT_CMD = "full-import";

  public static final String IMPORT_CMD = "import";

  public static final String DELTA_IMPORT_CMD = "delta-import";

  public static final String ABORT_CMD = "abort";

  public static final String DEBUG_MODE = "debug";

  public static final String RELOAD_CONF_CMD = "reload-config";

  public static final String SHOW_CONF_CMD = "show-config";
}
