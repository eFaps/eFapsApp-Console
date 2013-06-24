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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIFormConsole;
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

    public Return executeScript(final Parameter _parameter) {
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

}
