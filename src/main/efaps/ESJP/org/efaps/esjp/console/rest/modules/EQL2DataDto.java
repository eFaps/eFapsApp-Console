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
package org.efaps.esjp.console.rest.modules;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

@JsonDeserialize(builder = EQL2DataDto.Builder.class)
@EFapsUUID("484a497c-fd7a-41c2-9b6d-304a05ae936b")
@EFapsApplication("eFapsApp-Console")
public class EQL2DataDto
{

    private final List<String> keys;
    private final Collection<Map<String, ?>> values;

    @Generated("SparkTools")
    private EQL2DataDto(Builder builder)
    {
        this.keys = builder.keys;
        this.values = builder.values;
    }

    public List<String> getKeys()
    {
        return keys;
    }

    public Collection<Map<String, ?>> getValues()
    {
        return values;
    }

    @Generated("SparkTools")
    public static Builder builder()
    {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder
    {

        private List<String> keys = Collections.emptyList();
        private Collection<Map<String, ?>> values = Collections.emptyList();

        private Builder()
        {
        }

        public Builder withKeys(List<String> keys)
        {
            this.keys = keys;
            return this;
        }

        public Builder withValues(Collection<Map<String, ?>> values)
        {
            this.values = values;
            return this;
        }

        public EQL2DataDto build()
        {
            return new EQL2DataDto(this);
        }
    }
}
