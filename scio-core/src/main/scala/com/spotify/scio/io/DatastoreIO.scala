/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.io

import com.google.datastore.v1.{Entity, Query}
import com.spotify.scio.ScioContext
import com.spotify.scio.values.SCollection
import org.apache.beam.sdk.io.gcp.{datastore => beam}

import scala.concurrent.Future

final case class DatastoreIO(projectId: String) extends ScioIO[Entity] {

  override type ReadP = DatastoreIO.ReadParam
  override type WriteP = Unit

  override def read(sc: ScioContext, params: ReadP): SCollection[Entity] =
    sc.wrap(
      sc.applyInternal(
        beam.DatastoreIO
          .v1()
          .read()
          .withProjectId(projectId)
          .withNamespace(params.namespace)
          .withQuery(params.query)))

  override def write(data: SCollection[Entity], params: WriteP): Future[Tap[Entity]] = {
    data
      .asInstanceOf[SCollection[Entity]]
      .applyInternal(beam.DatastoreIO.v1.write.withProjectId(projectId))
    Future.failed(new NotImplementedError("Datastore future not implemented"))
  }

  override def tap(params: ReadP): Tap[Entity] =
    throw new NotImplementedError("Datastore tap not implemented")

}

object DatastoreIO {
  final case class ReadParam(query: Query, namespace: String = null)
}
