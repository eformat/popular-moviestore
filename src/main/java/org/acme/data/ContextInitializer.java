package org.acme.data;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = { Movie.class, MovieCart.class, MovieItem.class }, schemaPackageName = "com.acme.data")
interface ContextInitializer extends SerializationContextInitializer {
}
