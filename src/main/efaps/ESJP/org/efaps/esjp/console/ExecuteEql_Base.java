/*
 * Copyright 2003 - 2014 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.eql.JSONData;
import org.efaps.eql.Statement;
import org.efaps.esjp.ci.CIConsole;
import org.efaps.esjp.ci.CIFormConsole;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.ui.html.Table;
import org.efaps.json.data.AbstractValue;
import org.efaps.json.data.DataList;
import org.efaps.json.data.ObjectData;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("af6a4d60-5a43-40ff-917b-8e89ff9fe320")
@EFapsRevision("$Rev$")
public abstract class ExecuteEql_Base
    extends AbstractCommon
{

    private static final Logger LOG = LoggerFactory.getLogger(ExecuteEql.class);

    public Return executeEql(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        html.append("document.getElementsByName('").append(CIFormConsole.Console_ExecuteEqlForm.result.name)
            .append("')[0].innerHTML=\"")
            .append("<style> .eFapsForm .unlabeled .field { display: inline;} ")
            .append(" #result{ max-height: 400px; overflow: auto; width: 100%; background-color: lightgray;}")
            .append("</style><div id='result'>");
        try {
            final String eql = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEqlForm.eql.name);

            LOG.debug("Executing eql: '{}'", eql);

            final Statement stmt = Statement.getStatement(eql);
            final DataList datalist = JSONData.getDataList(stmt);

            LOG.debug("Recieved: '{}'", datalist);

            final Table table = new Table();
            boolean first = true;
            for (final ObjectData data : datalist) {
                if (first) {
                    first = false;
                    table.addRow();
                    for (final AbstractValue<?> value : data.getValues()) {
                        table.addHeaderColumn(value.getKey());
                    }
                }
                table.addRow();
                for (final AbstractValue<?> value : data.getValues()) {
                    final String valueStr;
                    if (value == null) {
                        valueStr = "";
                    } else {
                        valueStr = String.valueOf(value.getValue());
                    }
                    table.addColumn(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(valueStr)));
                }
            }
            html.append(table.toHtml());

            // if no error store the eql in history
            final Insert insert = new Insert(CIConsole.EQLHistory);
            insert.add(CIConsole.EQLHistory.EQLStatement, eql);
            insert.execute();
        } catch (final Exception e) {
            LOG.error("Catched error:", e);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);
            html.append(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(baos.toString())));
        } finally {
            html.append("</div>\";");
            ret.put(ReturnValues.SNIPLETT, html.toString());
        }
        return ret;
    }
}