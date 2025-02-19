/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.Set;
import javax.annotation.concurrent.Immutable;

/**
 * Information about a {@link MetricReader} used when collecting metrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class CollectionInfo {

  /** Construct a new collection info object storing information for collection against a reader. */
  public static CollectionInfo create(
      CollectionHandle handle, Set<CollectionHandle> allCollectors, MetricReader reader) {
    return new AutoValue_CollectionInfo(handle, allCollectors, reader);
  }

  CollectionInfo() {}

  /** The current collection. */
  public abstract CollectionHandle getCollector();
  /** The set of all possible collectors. */
  public abstract Set<CollectionHandle> getAllCollectors();

  public abstract MetricReader getReader();

  /** The default aggregation temporality for the current metric collection. */
  public final AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return getReader().getAggregationTemporality(instrumentType);
  }
}
