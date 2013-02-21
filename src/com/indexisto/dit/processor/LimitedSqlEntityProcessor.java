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
package com.indexisto.dit.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.VariableResolver;
import org.apache.solr.handler.dataimport.datasource.DataSource;
import org.apache.solr.handler.dataimport.processor.EntityProcessorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.DataImportHandler;
import com.indexisto.dit.data.exception.MappingException;
import com.indexisto.dit.data.exception.NotEnoughParamsException;
import com.indexisto.dit.data.object.DihCommandEnum;

public class LimitedSqlEntityProcessor extends EntityProcessorBase {
    private static final Logger LOG = LoggerFactory
            .getLogger(LimitedSqlEntityProcessor.class);

    protected DataSource<Iterator<Map<String, Object>>> dataSource;

    public static final String QUERY = "query";
    public static final String DELTA_QUERY = "deltaQuery";
    public static final String OFFSET = "offset";
    public static final String OFFSET_PLACEHOLDER_NAME = "offset";
    public static final String LIMIT = "limit";
    public static final String LIMIT_PLACEHOLDER_NAME = "limit";
    public static final String LAST_INDEX_TIME = "last_index_time";
    public static final String LAST_INDEX_TIME_PLACEHOLDER_NAME = "last_index_time";
    public static final String DATE_FORMAT_PLACEHOLDER_NAME = "dateFormat";

    @Override
    @SuppressWarnings("unchecked")
    public void init(Context context) {
        super.init(context);
        dataSource = context.getDataSource();
    }

    protected void initQuery(String q) {
        try {
            DataImporter.QUERY_COUNT.get().incrementAndGet();
            rowIterator = dataSource.getData(q);
            query = q;
        } catch (final DataImportHandlerException e) {
            throw e;
        } catch (final Exception e) {
            LOG.error("The query failed '" + q + "'", e);
            throw new DataImportHandlerException(
                    DataImportHandlerException.SEVERE, e);
        }
    }

    @Override
    public Map<String, Object> nextRow() {
        if (rowIterator == null) {
            final String command = (String) context.getRequestParameters().get(
                    DataImportHandler.COMMAND);

            final String query = command.equals(DihCommandEnum.DELTA_IMPORT
                    .toString()) && context.isRootEntity() ? context
                    .getEntityAttribute(DELTA_QUERY) : context
                    .getEntityAttribute(QUERY);
            if (query == null || query.equals("")) {
                throw new MappingException(QUERY + " or " + DELTA_QUERY
                        + " not specified");
            }

            final Map<String, Object> queryMap = new HashMap<String, Object>();
            if (context.isRootEntity()) {
                final long offset = (long) context.getRequestParameters().get(OFFSET);
                final long limit = (long) context.getRequestParameters().get(LIMIT);
                queryMap.put(OFFSET_PLACEHOLDER_NAME, offset);
                queryMap.put(LIMIT_PLACEHOLDER_NAME, limit);
            }

            if (command.equals(DihCommandEnum.DELTA_IMPORT.toString()) && context.isRootEntity()) {
                final String dateFormat = context
                        .getEntityAttribute(DATE_FORMAT_PLACEHOLDER_NAME);
                if (dateFormat == null || dateFormat.equals(""))
                    throw new MappingException("No date format for delta query specified");

                final Date lastIndexTime = (Date) context.getRequestParameters().get(LAST_INDEX_TIME);
                if (lastIndexTime == null) {
                    throw new NotEnoughParamsException("Last index time is not specified");
                }
                try {
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
                    final String lastIndexTimeStr = simpleDateFormat.format(lastIndexTime);
                    queryMap.put(LAST_INDEX_TIME_PLACEHOLDER_NAME, lastIndexTimeStr);
                } catch (final Exception e) {
                    throw new MappingException("Seems like dateFormat is wrong", e);
                }
            }

            final VariableResolver vr = context.getVariableResolver();
            vr.addNamespace(null, queryMap);
            initQuery(vr.replaceTokens(query));
        }
        return getNext();
    }
}
