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
package org.apache.solr.handler.dataimport.datasource;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.VariableResolver;
import org.apache.solr.handler.dataimport.processor.EntityProcessor;
import org.apache.solr.handler.dataimport.processor.EntityProcessorWrapper;
import org.apache.solr.handler.dataimport.processor.XPathEntityProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This can be useful for users who have a DB field containing xml and wish to use a nested {@link XPathEntityProcessor}
 * <p/>
 * The datasouce may be configured as follows
 * <p/>
 * <datasource name="f1" type="FieldReaderDataSource" />
 * <p/>
 * The enity which uses this datasource must keep the url value as the variable name url="field-name"
 * <p/>
 * The fieldname must be resolvable from {@link VariableResolver}
 * <p/>
 * This may be used with any {@link EntityProcessor} which uses a {@link DataSource}&lt;{@link Reader}&gt; eg: {@link XPathEntityProcessor}
 * <p/>
 * Supports String, BLOB, CLOB data types and there is an extra field (in the entity) 'encoding' for BLOB types
 *
 * @since 1.4
 */
public class FieldReaderDataSource extends DataSource<Reader> {
  private static final Logger LOG = LoggerFactory.getLogger(FieldReaderDataSource.class);
  protected VariableResolver vr;
  protected String dataField;
  private String encoding;
  private EntityProcessorWrapper entityProcessor;

  @Override
  public void init(Context context, Properties initProps) {
    dataField = context.getEntityAttribute("dataField");
    encoding = context.getEntityAttribute("encoding");
    entityProcessor = (EntityProcessorWrapper) context.getEntityProcessor();
    /*no op*/
  }

  @Override
  public Reader getData(String query) {
    final Object o = entityProcessor.getVariableResolver().resolve(dataField);
    if (o == null) {
       throw new DataImportHandlerException (SEVERE, "No field available for name : " +dataField);
    }
    if (o instanceof String) {
      return new StringReader((String) o);
    } else if (o instanceof Clob) {
      final Clob clob = (Clob) o;
      try {
        //Most of the JDBC drivers have getCharacterStream defined as public
        // so let us just check it
        return readCharStream(clob);
      } catch (final Exception e) {
        LOG.info("Unable to get data from CLOB");
        return null;

      }

    } else if (o instanceof Blob) {
      final Blob blob = (Blob) o;
      try {
        return getReader(blob);
      } catch (final Exception e) {
        LOG.info("Unable to get data from BLOB");
        return null;

      }
    } else {
      return new StringReader(o.toString());
    }

  }

  static Reader readCharStream(Clob clob) {
    try {
      return clob.getCharacterStream();
    } catch (final Exception e) {
      wrapAndThrow(SEVERE, e,"Unable to get reader from clob");
      return null;//unreachable
    }
  }

  private Reader getReader(Blob blob)
          throws SQLException, UnsupportedEncodingException {
    if (encoding == null) {
      return (new InputStreamReader(blob.getBinaryStream()));
    } else {
      return (new InputStreamReader(blob.getBinaryStream(), encoding));
    }
  }

  @Override
  public void close() {

  }
}
