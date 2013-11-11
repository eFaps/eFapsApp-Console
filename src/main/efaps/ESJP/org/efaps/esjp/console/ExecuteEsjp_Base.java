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

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIFormConsole;
import org.efaps.esjp.common.uiform.Field;
import org.efaps.esjp.common.uitable.MultiPrint;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.update.util.InstallationException;
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
    protected static final Logger LOG = LoggerFactory.getLogger(ExecuteEsjp.class);

    public Return executeEsjp(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = Instance.get(_parameter
                        .getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.instance.name));
        _parameter.put(ParameterValues.INSTANCE, instance);

        final String esjp = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.esjp.name);
        final String methodStr = _parameter.getParameterValue(CIFormConsole.Console_ExecuteEsjpForm.method.name);
        if (ExecuteEsjp.LOG.isDebugEnabled()) {
            ExecuteEsjp.LOG.debug("Esjp: {}\n Method: {}\n ",
                            new Object[] { esjp, methodStr });
        }
        try {
            final Class<?> clazz = Class.forName(esjp);
            final Method method = clazz.getMethod(methodStr, new Class[] { Parameter.class });
            method.invoke(clazz.newInstance(), _parameter);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.ClassNotFoundException", e);
        } catch (final NoSuchMethodException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.NoSuchMethodException", e);
        } catch (final SecurityException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.SecurityException", e);
        } catch (final InstantiationException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.IllegalAccessException", e);
        } catch (final IllegalArgumentException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.IllegalArgumentException", e);
        } catch (final InvocationTargetException e) {
            throw new EFapsException(ExecuteEsjp_Base.class, "execute.InvocationTargetException", e);
        }
        return new Return();
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
        throws InstallationException
    {
        final EFapsClassLoader efapsClassLoader = EFapsClassLoader.getInstance();
        final CompilerConfiguration config = new CompilerConfiguration();
        final GroovyClassLoader loader = new GroovyClassLoader(efapsClassLoader, config);
        final Script script = getScript(_parameter);
        if (script != null && script.getCode() != null) {
            final Class<?> clazz = loader.parseClass(script.getCode());
            groovy.lang.Script go;
            try {
                go = (groovy.lang.Script) clazz.newInstance();

                final Object[] args = {};
                go.invokeMethod("run", args);

            } catch (final InstantiationException e) {
                throw new InstallationException("InstantiationException in Groovy", e);
            } catch (final IllegalAccessException e) {
                throw new InstallationException("IllegalAccessException in Groovy", e);
            }
        }

        return new Return();
    }

    protected Script getScript(final Parameter _parameter)
    {
        final String option = _parameter.getParameterValue(CIFormConsole.Console_ExecuteScriptForm.types.name);
        final String code = _parameter.getParameterValue(CIFormConsole.Console_ExecuteScriptForm.script.name);
        Script script = null;
        if ("Groovy".equals(option)) {
            script = new Script(code, null, null);
        } else if ("Rhino".equals(option)) {

        }
        return script;
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

    private class Script
    {

        /**
         * Script code to execute.
         */
        private final String code;

        /**
         * File name of the script (within the class path).
         */
        private final String fileName;

        /**
         * Name of called function.
         */
        private final String function;

        /**
         * Constructor to initialize a script.
         *
         * @param _code         script code
         * @param _fileName     script file name
         * @param _function     called function name
         */
        private Script(final String _code,
                               final String _fileName,
                               final String _function)
        {
            this.code = (_code == null) || ("".equals(_code.trim())) ? null : _code.trim();
            this.fileName = _fileName;
            this.function = _function;
        }

        /**
         * Getter method for instance variable {@link #code}.
         *
         * @return value of instance variable {@link #code}
         */
        public String getCode()
        {
            return this.code;
        }

        /**
         * Getter method for instance variable {@link #fileName}.
         *
         * @return value of instance variable {@link #fileName}
         */
        public String getFileName()
        {
            return this.fileName;
        }

        /**
         * Getter method for instance variable {@link #function}.
         *
         * @return value of instance variable {@link #function}
         */
        public String getFunction()
        {
            return this.function;
        }
    }
}
