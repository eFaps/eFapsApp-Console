/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.text.StringEscapeUtils;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIFormConsole;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uitable.MultiPrint;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("8a1e1edf-e235-4089-8974-30464217facf")
@EFapsApplication("eFapsApp-Console")
public abstract class ExecuteEsjp_Base
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteEsjp.class);

    /**
     * Execute esjp.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
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
            method.invoke(clazz.getConstructor().newInstance(), _parameter);
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

    /**
     * Auto complete4 program.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return autoComplete4Program(final Parameter _parameter)
        throws EFapsException
    {
        final Map<String, Map<String, String>> tmpMap = new TreeMap<>();
        final List<Instance> instances = new MultiPrint()
        {

            @Override
            protected void add2QueryBldr(final Parameter _parameter,
                                         final QueryBuilder _queryBldr)
                throws EFapsException
            {
                final String input = (String) _parameter.get(ParameterValues.OTHERS);
                _queryBldr.addWhereAttrMatchValue(CIAdminProgram.Abstract.Name, input + "*").setIgnoreCase(true);

            }
        }.getInstances(_parameter);

        final MultiPrintQuery multi = new MultiPrintQuery(instances);
        multi.addAttribute(CIAdminProgram.Abstract.Name);
        multi.execute();
        while (multi.next()) {
            final String name = multi.<String>getAttribute(CIAdminProgram.Abstract.Name);
            final Map<String, String> map = new HashMap<>();
            map.put("eFapsAutoCompleteKEY", name);
            map.put("eFapsAutoCompleteVALUE", name);
            map.put("eFapsAutoCompleteCHOICE", name);
            tmpMap.put(name, map);
        }
        final Return ret = new Return();
        final List<Map<String, String>> list = new ArrayList<>();
        list.addAll(tmpMap.values());
        ret.put(ReturnValues.VALUES, list);

        return ret;
    }

    /**
     * Update field4 esjp.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
    public Return updateField4ESJP(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final String esjp = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.esjp.name);
        try {
            final Class<?> clazz = Class.forName(esjp);
            final List<String> methods = new ArrayList<>();
            for (final Method method : clazz.getMethods()) {
                if (method.getParameterTypes().length == 1
                                && method.getParameterTypes()[0].equals(Parameter.class)) {
                    methods.add(method.getName());
                }
            }
            Collections.sort(methods);
            final StringBuilder arrayStr = new StringBuilder()
                    .append("new Array('").append(methods.isEmpty()
                                    ? "" : StringEscapeUtils.escapeEcmaScript(methods.get(0))).append("'");
            for (final String method : methods) {
                arrayStr.append(",'").append(StringEscapeUtils.escapeEcmaScript(method)).append("','")
                    .append(StringEscapeUtils.escapeEcmaScript(method)).append("'");
            }
            arrayStr.append(")");
            final List<Map<String, Object>> values = new ArrayList<>();
            final Map<String, Object> map = new HashMap<>();
            map.put(CIFormConsole.Console_ExecuteEsjpForm.method.name, arrayStr);
            values.add(map);
            ret.put(ReturnValues.VALUES, values);

        } catch (final ClassNotFoundException e) {
            LOG.error("ClassNotFoundException", e);
        }
        return ret;
    }

    /**
     * Execute script.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
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

    /**
     * Drop down4 scripts.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the return
     * @throws EFapsException on error
     */
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
                final List<DropDownPosition> values = new ArrayList<>();
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
