/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tuweni.devp2p.eth

import kotlinx.coroutines.Dispatchers
import org.apache.tuweni.eth.repository.BlockchainRepository
import org.apache.tuweni.rlpx.RLPxService
import org.apache.tuweni.rlpx.wire.SubProtocol
import org.apache.tuweni.rlpx.wire.SubProtocolClient
import org.apache.tuweni.rlpx.wire.SubProtocolHandler
import org.apache.tuweni.rlpx.wire.SubProtocolIdentifier
import kotlin.coroutines.CoroutineContext

class EthSubprotocol(
  private val coroutineContext: CoroutineContext = Dispatchers.Default,
  private val blockchainInfo: BlockchainInformation,
  private val repository: BlockchainRepository
) : SubProtocol {

  private var client: EthClient? = null

  companion object {
    val ETH62 = SubProtocolIdentifier.of("eth", 62)
    val ETH63 = SubProtocolIdentifier.of("eth", 63)
    val ETH64 = SubProtocolIdentifier.of("eth", 64)
  }
  override fun id(): SubProtocolIdentifier = ETH64

  override fun supports(subProtocolIdentifier: SubProtocolIdentifier): Boolean {
    return "eth".equals(subProtocolIdentifier.name()) && (subProtocolIdentifier.version() == ETH62.version() ||
      subProtocolIdentifier.version() == ETH63.version() || subProtocolIdentifier.version() == ETH64.version())
  }

  override fun versionRange(version: Int): Int {
    if (version == ETH62.version()) {
      return 8
    } else {
      return 17
    }
  }

  override fun createHandler(service: RLPxService): SubProtocolHandler {
    val controller = EthController(repository, createClient(service) as EthRequestsManager)
    return EthHandler(coroutineContext, blockchainInfo, service, controller)
  }

  override fun getCapabilities() = mutableListOf(ETH62, ETH63, ETH64)

  override fun createClient(service: RLPxService): SubProtocolClient {
    if (client == null) {
      val c = EthClient(service)
      client = c
      return c
    }
    return client!!
  }
}
