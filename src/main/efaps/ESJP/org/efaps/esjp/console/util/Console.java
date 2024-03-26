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
package org.efaps.esjp.console.util;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.annotation.EFapsSystemConfiguration;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("46c8410d-005a-4548-9163-a9b409f385f1")
@EFapsApplication("eFapsApp-Console")
@EFapsSystemConfiguration("54b18a80-8222-4231-b222-6555b2c675e2")
public final class Console
{
    /** The base. */
    public static final String BASE = "org.efaps.consoles.";

    /** Console-Configuration. */
    public static final UUID SYSCONFUUID = UUID.fromString("54b18a80-8222-4231-b222-6555b2c675e2");

    /**
     * Singelton.
     */
    private Console()
    {
    }

    /**
     * @return the SystemConfigruation for console
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration getSysConfig()
        throws CacheReloadException
    {
        // console-Configuration
        return SystemConfiguration.get(SYSCONFUUID);
    }
}
