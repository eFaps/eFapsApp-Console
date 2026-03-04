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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.stmt.CIPrintStmt;
import org.efaps.db.stmt.CountStmt;
import org.efaps.db.stmt.DeleteStmt;
import org.efaps.db.stmt.ExecStmt;
import org.efaps.db.stmt.InsertStmt;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.UpdateStmt;
import org.efaps.eql.EQL;
import org.efaps.eql.JSONCI;
import org.efaps.esjp.ci.CICommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@EFapsUUID("8da2f447-e6c5-4897-a8ab-8a60ed04f625")
@EFapsApplication("eFapsApp-Console")
@Path("/ui/modules/console-eql2")
public class EQL2Controller
{

    private static final Logger LOG = LoggerFactory.getLogger(EQL2Controller.class);

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public Response execute(final String stmt)
        throws EFapsException
    {
        LOG.info("Got EQL2 to execute: {}", stmt);
        Object result = null;
        final var errors = EQL.checkSyntax(stmt);
        if (!errors.isEmpty()) {
            final List<EQL2MsgDto> msgs = new ArrayList<>();
            for (final var error : errors) {
                msgs.add(EQL2MsgDto.builder()
                                .withMsg(error.getSyntaxErrorMessage().getMessage())
                                .build());
            }
            result = msgs;
        } else {
            final var eqlStmt = EQL.getStatement(stmt);

            if (eqlStmt instanceof final PrintStmt printStmt) {
                final var values = printStmt.evaluate().getData();
                final List<String> keys = new ArrayList<>();
                if (!values.isEmpty()) {
                    keys.addAll(values.iterator().next().keySet());
                }
                result = EQL2DataDto.builder()
                                .withKeys(keys)
                                .withValues(values)
                                .build();
            } else if (eqlStmt instanceof final CountStmt countStmt) {
                final var count  = countStmt.evaluate().count();
                result = EQL2MsgDto.builder().withMsg("" + count).build();
            } else if (eqlStmt instanceof final DeleteStmt deleteStmt) {
                deleteStmt.execute();
                result = EQL2MsgDto.builder().withMsg("Sucess").build();
            } else if (eqlStmt instanceof final InsertStmt insertStmt) {
                final var inst = insertStmt.execute();
                result = EQL2MsgDto.builder().withMsg("Sucess " + inst.getOid()).build();
            } else if (eqlStmt instanceof final UpdateStmt updateStmt) {
                updateStmt.execute();
                result = EQL2MsgDto.builder().withMsg("Sucess").build();
            } else if (eqlStmt instanceof final ExecStmt execStmt) {
                result = execStmt.getData();
            } else if (eqlStmt instanceof CIPrintStmt) {
                result = JSONCI.getCI((CIPrintStmt) eqlStmt);
            }

            // if no error store the eql in history
            final Insert insert = new Insert(CICommon.HistoryEQL);
            insert.add(CICommon.HistoryEQL.Origin, "eFapsApp-Console");
            insert.add(CICommon.HistoryEQL.EQLStatement, stmt);
            insert.execute();
        }
        return Response.ok(result)
                        .build();
    }

    @GET
    @Path("/history")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response history(@QueryParam("term") String term,
                            @QueryParam("own") Boolean own,
                            @QueryParam("limit") Integer limit)
        throws EFapsException
    {
        final List<EQL2HistoryDto> dtos = new ArrayList<>();

        final var bldr = EQL.builder().print()
                        .query(CICommon.HistoryEQL);

        if (own) {
            bldr.where().attribute(CICommon.HistoryEQL.Creator).eq(Context.getThreadContext().getPersonId());
        }
        if (StringUtils.isNotEmpty(term)) {
            bldr.where().attribute(CICommon.HistoryEQL.EQLStatement).ilike(term);
        }

        final var eval = bldr
                        .select()
                        .attribute(CICommon.HistoryEQL.EQLStatement, CICommon.HistoryEQL.Creator, CICommon.HistoryEQL.Created)
                        .limit(limit)
                        .orderBy(CICommon.HistoryEQL.Created, true)
                        .evaluate();
        while (eval.next()) {
            final String stmt = eval.get(CICommon.HistoryEQL.EQLStatement);
            final Person creator = eval.get(CICommon.HistoryEQL.Creator);
            final OffsetDateTime created = eval.get(CICommon.HistoryEQL.Created);
            dtos.add(
            EQL2HistoryDto.builder()
                .withStmt(stmt)
                .withCreator(creator.getName())
                .withCreated(created)
                .build());
        }
        return Response.ok(dtos)
                        .build();
    }
}
