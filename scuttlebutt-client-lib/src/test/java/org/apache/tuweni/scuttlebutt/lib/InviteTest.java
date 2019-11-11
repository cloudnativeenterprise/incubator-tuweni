/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuweni.scuttlebutt.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.tuweni.concurrent.AsyncResult;
import org.apache.tuweni.scuttlebutt.Invite;
import org.apache.tuweni.scuttlebutt.MalformedInviteCodeException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class InviteTest {


  /**
   * Tests that it is possible to generate a test invite code.
   *
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  @Disabled("Requires a running ssb server")
  public void testGenerateInvite() throws IOException, InterruptedException {
    TestConfig config = TestConfig.fromEnvironment();

    AsyncResult<ScuttlebuttClient> client =
        ScuttlebuttClientFactory.fromNet(new ObjectMapper(), config.getHost(), config.getPort(), config.getKeyPair());

    ScuttlebuttClient scuttlebuttClient = client.get();

    AsyncResult<Invite> inviteAsyncResult = scuttlebuttClient.getNetworkService().generateInviteCode(1);

    Invite invite = inviteAsyncResult.get();

    assertEquals(invite.identity().publicKeyAsBase64String(), config.getKeyPair().publicKey().bytes().toBase64String());
  }

  /**
   * Tests it's possible to request the server uses an invite code generated by another node.
   *
   * @throws IOException
   * @throws InterruptedException
   * @throws TimeoutException
   */
  @Test
  @Disabled("Requires a running ssb server")
  public void testUseInvite() throws IOException, InterruptedException, TimeoutException {
    TestConfig config = TestConfig.fromEnvironment();

    String inviteCode = System.getenv("ssb_invite_code");

    if (inviteCode == null) {
      fail("Test requires an 'ssb_invite_code environment variable with a valid ssb invite code");
    } else {

      try {
        Invite invite = Invite.fromCanonicalForm(inviteCode);

        AsyncResult<ScuttlebuttClient> localhost = ScuttlebuttClientFactory
            .fromNet(new ObjectMapper(), config.getHost(), config.getPort(), config.getKeyPair());

        ScuttlebuttClient scuttlebuttClient = localhost.get();

        AsyncResult<Void> asyncResult = scuttlebuttClient.getNetworkService().redeemInviteCode(invite);

        asyncResult.get(1, TimeUnit.MINUTES);

      } catch (MalformedInviteCodeException e) {
        fail(e.getMessage());
      }



    }


  }

}