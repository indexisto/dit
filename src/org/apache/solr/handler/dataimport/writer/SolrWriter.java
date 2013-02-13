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
package org.apache.solr.handler.dataimport.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DocBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changed to be stub. See sources in Apache Solr
 */
public class SolrWriter extends DIHWriterBase implements DIHWriter {

    private static final Logger log = LoggerFactory.getLogger(SolrWriter.class);

    public static final String LAST_INDEX_KEY = "last_index_time";

    @Override
    public void close() {
        // stub
    }

    @Override
    public boolean upload(SolrInputDocument d) {
        // stub
        return true;
    }

    @Override
    public void deleteDoc(Object id) {
        // stub
    }

    @Override
    public void deleteByQuery(String query) {
        // stub
    }

    @Override
    public void commit(boolean optimize) {
        // stub
    }

    @Override
    public void rollback() {
        // stub
    }

    @Override
    public void doDeleteAll() {
        // stub
    }

    public static String getResourceAsString(InputStream in) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final byte[] buf = new byte[1024];
        int sz = 0;
        try {
            while ((sz = in.read(buf)) != -1) {
                baos.write(buf, 0, sz);
            }
        } finally {
            try {
                in.close();
            } catch (final Exception e) {

            }
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    public static String getDocCount() {
        if (DocBuilder.INSTANCE.get() != null) {
            return ""
                    + (DocBuilder.INSTANCE.get().importStatistics.docCount
                            .get() + 1);
        } else {
            return null;
        }
    }

    @Override
    public void init(Context context) {
        /* NO-OP */
    }
}
