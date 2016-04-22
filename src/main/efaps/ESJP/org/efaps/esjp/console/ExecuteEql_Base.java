/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.esjp.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.JSONCI;
import org.efaps.eql.JSONData;
import org.efaps.eql.stmt.ICIPrintStmt;
import org.efaps.eql.stmt.ICIStmt;
import org.efaps.eql.stmt.IEQLStmt;
import org.efaps.eql.stmt.IPrintStmt;
import org.efaps.eql.stmt.IUpdateStmt;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.ci.CIFormConsole;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.uitable.MultiPrint;
import org.efaps.esjp.ui.html.Table;
import org.efaps.json.ci.AbstractCI;
import org.efaps.json.ci.Attribute;
import org.efaps.json.ci.Type;
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
 */
@EFapsUUID("af6a4d60-5a43-40ff-917b-8e89ff9fe320")
@EFapsApplication("eFapsApp-Console")
public abstract class ExecuteEql_Base
    extends AbstractCommon
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteEql.class);

    /**
     * Execute eql.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    @SuppressWarnings("checkstyle:illegalcatch")
    public Return executeEql(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        html.append("document.getElementsByName('")
                    .append(CIFormConsole.Console_ExecuteEqlForm.result.name)
                    .append("')[0].innerHTML=\"")
                    .append("<style> .eFapsForm .unlabeled .field { display: inline;} ")
                    .append(" #result{ max-height: 400px; overflow: auto; width: 100%; background-color: lightgray;}")
                    .append("</style><div id='result'>");
        try {
            final String eql = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEqlForm.eql.name);

            LOG.debug("Executing eql: '{}'", eql);

            final IEQLStmt stmt = InvokerUtil.getInvoker().invoke(eql);
            if (stmt instanceof IPrintStmt) {

                final DataList datalist = JSONData.getDataList((IPrintStmt) stmt);

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
            } else if (stmt instanceof IUpdateStmt) {
                ((IUpdateStmt) stmt).execute();
            } else if (stmt instanceof ICIStmt) {
                final AbstractCI<?> ci = JSONCI.getCI((ICIPrintStmt) stmt);
                final Table table = new Table();
                table.addColumn(StringEscapeUtils.escapeHtml4(ci.getName()))
                                .addColumn(StringEscapeUtils.escapeHtml4(ci.getUUID().toString()));
                if (ci instanceof org.efaps.json.ci.Type) {
                    final org.efaps.json.ci.Type ciType = (Type) ci;
                    for (final Attribute attr: ciType.getAttributes()) {
                        table.addRow()
                            .addColumn(StringEscapeUtils.escapeHtml4(attr.getName()));
                    }
                }
                html.append(StringEscapeUtils.escapeEcmaScript(table.toHtml().toString()));
            }
            // if no error store the eql in history
            final Insert insert = new Insert(CICommon.HistoryEQL);
            insert.add(CICommon.HistoryEQL.Origin, "eFapsApp-Console");
            insert.add(CICommon.HistoryEQL.EQLStatement, eql);
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

    /**
     * History multi print.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return historyMultiPrint(final Parameter _parameter)
        throws EFapsException
    {
        final MultiPrint multi = new MultiPrint()
        {

            @Override
            protected void add2QueryBldr(final Parameter _parameter,
                                         final org.efaps.db.QueryBuilder _queryBldr)
                throws EFapsException
            {
                _queryBldr.addWhereAttrEqValue(CICommon.HistoryEQL.Origin, "eFapsApp-Console");
            };
        };
        return multi.execute(_parameter);
    }
}
