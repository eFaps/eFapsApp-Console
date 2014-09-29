/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev: 9669 $
 * Last Changed:    $Date: 2013-06-20 12:12:39 -0500 (jue, 20 jun 2013) $
 * Last Changed By: $Author: jorge.cueva@moxter.net $
 */

package org.efaps.esjp.console;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIFormConsole;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uitable.MultiPrint;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractDocument_Base.java 3674 2010-01-28 18:52:35Z jan.moxter
 *          $
 */
@EFapsUUID("8a1e1edf-e235-4089-8974-30464217facf")
@EFapsRevision("$Rev: 9669 $")
public class ExecuteEsjp_Base
{

    private static final Logger LOG = LoggerFactory.getLogger(ExecuteEsjp.class);

    public Return executeEsjp(final Parameter _parameter)
        throws EFapsException
    {
        final StringBuilder snipplet = new StringBuilder();
        snipplet.append("document.getElementsByName('").append(CIFormConsole.Console_ExecuteEsjpForm.returnValue.name)
                        .append("')[0].innerHTML=\"");
        final Return ret = new Return();
        final Instance instance = Instance.get(_parameter
                        .getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.instance.name));
        _parameter.put(ParameterValues.INSTANCE, instance);

        final String esjp = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.esjp.name);
        final String methodStr = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.method.name);
        if (ExecuteEsjp_Base.LOG.isDebugEnabled()) {
            ExecuteEsjp_Base.LOG.debug("Esjp: {}\n Method: {}\n ",
                            new Object[] { esjp, methodStr });
        }
        try {
            final Class<?> clazz = Class.forName(esjp);
            final Method method = clazz.getMethod(methodStr, new Class[] { Parameter.class });
            method.invoke(clazz.newInstance(), _parameter);
        } catch (final Exception e) {
            ExecuteEsjp_Base.LOG.error("Catched:", e);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);
            String content = baos.toString();
            content = content.replace("\n", "<br/>");
            content = content.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            snipplet.append(StringEscapeUtils.escapeEcmaScript(content.substring(0, content.length() > 5000 ? 5000
                            : content.length())));
        }
        snipplet.append("\";");
        ret.put(ReturnValues.SNIPLETT, snipplet.toString());
        return ret;
    }

    public Return autoComplete4Program(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Map<String, String>> tmpMap = new TreeMap<String, Map<String, String>>();
        final List<Instance> instances = new MultiPrint()
        {

            @Override
            protected void add2QueryBldr(final Parameter _parameter,
                                         final QueryBuilder _queryBldr)
                throws EFapsException
            {
                final String input = (String) _parameter.get(ParameterValues.OTHERS);
                _queryBldr.addWhereAttrMatchValue(CIAdminProgram.Abstract.Name, input + "*").setIgnoreCase(true);

            };
        }.getInstances(_parameter);

        final MultiPrintQuery multi = new MultiPrintQuery(instances);
        multi.addAttribute(CIAdminProgram.Abstract.Name);
        multi.execute();
        while (multi.next()) {
            final String name = multi.<String>getAttribute(CIAdminProgram.Abstract.Name);
            final Map<String, String> map = new HashMap<String, String>();
            map.put(EFapsKey.AUTOCOMPLETE_KEY.getKey(), name);
            map.put(EFapsKey.AUTOCOMPLETE_VALUE.getKey(), name);
            map.put(EFapsKey.AUTOCOMPLETE_CHOICE.getKey(), name);
            tmpMap.put(name, map);
        }
        final Return ret = new Return();
        final List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        list.addAll(tmpMap.values());
        ret.put(ReturnValues.VALUES, list);

        return ret;
    }

    public Return executeScript(final Parameter _parameter)
        throws EFapsException
    {
        final EFapsClassLoader efapsClassLoader = EFapsClassLoader.getInstance();
        final String option = _parameter.getParameterValue(CIFormConsole.Console_ExecuteScriptForm.types.name);
        final String code = _parameter.getParameterValue(CIFormConsole.Console_ExecuteScriptForm.script.name);
        if (code != null) {
            if ("Groovy".equals(option)) {
                final Binding binding = new Binding();
                binding.setVariable("EFAPS_LOGGER", LOG);
                binding.setVariable("EFAPS_USERNAME", Context.getThreadContext().getPerson().getName());
                final GroovyShell shell = new GroovyShell(efapsClassLoader, binding);
                final Script script = shell.parse(code);
                script.run();
            } else {
                // TODO
            }
        }
        return new Return();
    }

    public Return dropDown4Scripts(final Parameter _parameter)
        throws EFapsException
    {
        return new Field()
        {

            @Override
            public Return dropDownFieldValue(final Parameter _parameter)
                throws EFapsException
            {
                final Map<Integer, String> options = analyseProperty(_parameter, "Option");
                final List<DropDownPosition> values = new ArrayList<DropDownPosition>();
                for (final Entry<Integer, String> option : options.entrySet()) {
                    final DropDownPosition pos = new DropDownPosition(option.getValue(), option.getValue());
                    values.add(pos);
                }

                final String html = getDropDownField(_parameter, values).toString();
                final Return ret = new Return();
                ret.put(ReturnValues.SNIPLETT, html);
                return ret;
            }

        }.dropDownFieldValue(_parameter);
    }

}
