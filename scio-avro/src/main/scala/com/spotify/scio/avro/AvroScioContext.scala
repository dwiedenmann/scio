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

package com.spotify.scio.avro

import com.google.protobuf.Message
import org.apache.avro.Schema
import com.spotify.scio.ScioContext
import com.spotify.scio.coders.Coder
import com.spotify.scio.avro.types.AvroType.HasAvroAnnotation
import com.spotify.scio.io._
import com.spotify.scio.values._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** Enhanced version of [[ScioContext]] with Avro methods. */
final class AvroScioContext(@transient val self: ScioContext) extends Serializable {

  /**
   * Get an SCollection for an object file using default serialization.
   *
   * Serialized objects are stored in Avro files to leverage Avro's block file format. Note that
   * serialization is not guaranteed to be compatible across Scio releases.
   */
  def objectFile[T: Coder](path: String): SCollection[T] =
    self.read(ObjectFileIO[T](path))

  /**
   * Get an SCollection for an Avro file.
   * @param schema must be not null if `T` is of type
   *               [[org.apache.avro.generic.GenericRecord GenericRecord]].
   */
  def avroFile[T: ClassTag: Coder](path: String, schema: Schema = null): SCollection[T] =
    self.read(AvroIO[T](path, schema))

  /**
   * Get a typed SCollection from an Avro schema.
   *
   * Note that `T` must be annotated with
   * [[com.spotify.scio.avro.types.AvroType AvroType.fromSchema]],
   * [[com.spotify.scio.avro.types.AvroType AvroType.fromPath]], or
   * [[com.spotify.scio.avro.types.AvroType AvroType.toSchema]].
   */
  def typedAvroFile[T <: HasAvroAnnotation: ClassTag: TypeTag: Coder](
    path: String): SCollection[T] =
    self.read(com.spotify.scio.io.AvroTyped.AvroIO[T](path))

  /**
   * Get an SCollection for a Protobuf file.
   *
   * Protobuf messages are serialized into `Array[Byte]` and stored in Avro files to leverage
   * Avro's block file format.
   */
  def protobufFile[T: ClassTag: Coder](path: String)(implicit ev: T <:< Message): SCollection[T] =
    self.read(ProtobufIO[T](path))

}
