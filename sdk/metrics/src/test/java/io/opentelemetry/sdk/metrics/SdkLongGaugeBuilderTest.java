/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for SDK {@link ObservableLongGauge}. */
class SdkLongGaugeBuilderTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkLongGaugeBuilderTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(sdkMeterReader)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void removeCallback() {
    ObservableLongGauge gauge =
        sdkMeter
            .gaugeBuilder("testGauge")
            .ofLongs()
            .buildWithCallback(measurement -> measurement.record(10));

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric -> assertThat(metric).hasName("testGauge").hasLongGauge().points().hasSize(1));

    gauge.close();

    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter
        .gaugeBuilder("testObserver")
        .ofLongs()
        .setDescription("My own LongValueObserver")
        .setUnit("ms")
        .buildWithCallback(result -> {});
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithOneRecord() {
    sdkMeter
        .gaugeBuilder("testObserver")
        .ofLongs()
        .buildWithCallback(result -> result.record(12, Attributes.builder().put("k", "v").build()));
    testClock.advance(Duration.ofSeconds(1));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testObserver")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 1000000000L)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.builder().put("k", "v").build())
                                .hasValue(12)));
    testClock.advance(Duration.ofSeconds(1));
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testObserver")
                    .hasLongGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - 2000000000L)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.builder().put("k", "v").build())
                                .hasValue(12)));
  }
}
