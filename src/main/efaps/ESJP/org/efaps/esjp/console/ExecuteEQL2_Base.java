/*
 * Copyright 2003 - 2018 The eFaps Team
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

import org.apache.commons.text.StringEscapeUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.stmt.AbstractStmt;
import org.efaps.db.stmt.CIPrintStmt;
import org.efaps.db.stmt.DeleteStmt;
import org.efaps.db.stmt.InsertStmt;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.UpdateStmt;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql.EQL;
import org.efaps.eql.JSONCI;
import org.efaps.esjp.ci.CICommon;
import org.efaps.esjp.ci.CIFormConsole;
import org.efaps.esjp.common.AbstractCommon;
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
 * The Class ExecuteEQL2_Base.
 */
@EFapsUUID("bc24f748-2f9a-431b-8792-f4429b1b73fe")
@EFapsApplication("eFapsApp-Console")
public abstract class ExecuteEQL2_Base
    extends AbstractCommon
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteEQL2.class);

    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final StringBuilder html = new StringBuilder();
        html.append("document.getElementsByName('")
            .append(CIFormConsole.Console_ExecuteEQL2Form.result.name)
            .append("')[0].innerHTML=\"")
            .append("<style> .eFapsForm .unlabeled .field { display: inline;} ")
            .append(" #result{ max-height: 400px; overflow: auto; width: 100%; background-color: lightgray; padding: 5px 10px;}")
            .append("</style><div id='result'>");
        Object restResult = null;
        try {
            final String eqlStmt = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEQL2Form.eql.name);
            final AbstractStmt stmt = EQL.getStatement(eqlStmt);

            if (stmt instanceof PrintStmt) {
                final PrintStmt printStmt = (PrintStmt) stmt;
                final Evaluator eval = printStmt.evaluate();
                final DataList datalist = eval.getDataList();
                restResult = datalist;
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
            } else if (stmt instanceof DeleteStmt) {
                final DeleteStmt deleteStmt = (DeleteStmt) stmt;
                deleteStmt.execute();
                html.append("Success");
                restResult = "Sucess";
            } else if (stmt instanceof InsertStmt) {
                final InsertStmt insertStmt = (InsertStmt) stmt;
                final Instance inst = insertStmt.execute();
                html.append("Success: ").append(inst.getOid());
                restResult = "Sucess " + inst.getOid();
            } else if (stmt instanceof UpdateStmt) {
                final UpdateStmt updateStmt = (UpdateStmt) stmt;
                updateStmt.execute();
                html.append("Success");
                restResult = "Sucess";
            } else if (stmt instanceof CIPrintStmt) {
              final AbstractCI<?> ci = JSONCI.getCI((CIPrintStmt) stmt);
              final Table table = new Table();
              table.addColumn(StringEscapeUtils.escapeHtml4(ci.getName()))
                              .addColumn(StringEscapeUtils.escapeHtml4(ci.getUUID().toString()));
              if (ci instanceof org.efaps.json.ci.Type) {
                  final org.efaps.json.ci.Type ciType = (Type) ci;
                  for (final Attribute attr: ciType.getAttributes()) {
                      table.addRow()
                          .addColumn(StringEscapeUtils.escapeHtml4(attr.getName()))
                          .addColumn(StringEscapeUtils.escapeHtml4(attr.getType().getName()))
                          .addColumn(StringEscapeUtils.escapeHtml4(attr.getType().getInfo()));
                  }
              }
              html.append(StringEscapeUtils.escapeEcmaScript(table.toHtml().toString()));
              restResult = ci;
          }
            // if no error store the eql in history
            final Insert insert = new Insert(CICommon.HistoryEQL);
            insert.add(CICommon.HistoryEQL.Origin, "eFapsApp-Console");
            insert.add(CICommon.HistoryEQL.EQLStatement, eqlStmt);
            insert.execute();
        } catch (final Exception e) {
            LOG.error("Catched error:", e);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);
            restResult = baos.toString();
            html.append(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(baos.toString())));
        } finally {
            html.append("</div>\";");
            if (_parameter.getParameterValue("eFaps-REST") == null) {
                ret.put(ReturnValues.SNIPLETT, html.toString());
            } else {
                ret.put(ReturnValues.VALUES, restResult);
            }
        }
        return ret;
    }
}
