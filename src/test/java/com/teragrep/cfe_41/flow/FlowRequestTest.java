/*
 * Integration Command-line tool for Teragrep
 * Copyright (C) 2025  Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.cfe_41.flow;

import com.teragrep.cfe_41.ApiConfig;
import com.teragrep.cnf_01.ArgsConfiguration;
import com.teragrep.cnf_01.Configuration;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class FlowRequestTest {

    private static ClientAndServer mockServer;
    private static MockServerClient mockClient;

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FlowRequestImpl.class).verify();
    }

    @BeforeAll
    public static void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(1080);
        mockClient = new MockServerClient("localhost", 1080);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }

    @Test
    public void flowRequestTest() {
        // Build data for expected response
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder
                .add(Json.createObjectBuilder().add("id", 1).add("name", "flow1"))
                .add(Json.createObjectBuilder().add("id", 2).add("name", "flow2"));
        JsonArray flowsArray = arrayBuilder.build();

        // Simulate CFE_18 endpoint. FlowsArray is returned from CFE_18 as built above.
        mockClient
                .when(request().withMethod("GET").withPath("/flow"))
                .respond(response().withStatusCode(200).withBody(flowsArray.toString()));

        String[] args = new String[] {
                "url=http://localhost:1080", "test=test"
        };
        Configuration cfg = new ArgsConfiguration(args);

        // Create ApiConfig so that request can be made
        ApiConfig apiConfig = Assertions.assertDoesNotThrow(() -> new ApiConfig(cfg.asMap()));

        // Create request
        FlowRequest fakeFlow = new FlowRequestImpl(apiConfig);

        // Store the results from FlowRequestImpl.flowResponse()
        FlowResponse fakeFlowResponse = Assertions.assertDoesNotThrow(() -> fakeFlow.flowResponse());

        // Create expected FlowResponse that should match the requested FlowResponse
        FlowResponse realFlowResponse = new FlowResponse(flowsArray);

        // Assertions
        Assertions.assertFalse(flowsArray.isEmpty());
        // Asserting that contents equal to each other
        Assertions.assertEquals(realFlowResponse.hashCode(), fakeFlowResponse.hashCode());

    }

}
