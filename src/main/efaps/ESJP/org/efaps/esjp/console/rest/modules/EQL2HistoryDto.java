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

import java.time.OffsetDateTime;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

@JsonDeserialize(builder = EQL2HistoryDto.Builder.class)
@EFapsUUID("414e54e5-ec78-4d18-b061-29cf9ba936e1")
@EFapsApplication("eFapsApp-Console")
public class EQL2HistoryDto
{

    private final String stmt;
    private final String creator;
    private final OffsetDateTime created;

    @Generated("SparkTools")
    private EQL2HistoryDto(Builder builder)
    {
        this.stmt = builder.stmt;
        this.creator = builder.creator;
        this.created = builder.created;
    }

    public String getStmt()
    {
        return stmt;
    }

    public String getCreator()
    {
        return creator;
    }

    public OffsetDateTime getCreated()
    {
        return created;
    }

    @Generated("SparkTools")
    public static Builder builder()
    {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder
    {

        private String stmt;
        private String creator;
        private OffsetDateTime created;

        private Builder()
        {
        }

        public Builder withStmt(String stmt)
        {
            this.stmt = stmt;
            return this;
        }

        public Builder withCreator(String creator)
        {
            this.creator = creator;
            return this;
        }

        public Builder withCreated(OffsetDateTime created)
        {
            this.created = created;
            return this;
        }

        public EQL2HistoryDto build()
        {
            return new EQL2HistoryDto(this);
        }
    }
}
