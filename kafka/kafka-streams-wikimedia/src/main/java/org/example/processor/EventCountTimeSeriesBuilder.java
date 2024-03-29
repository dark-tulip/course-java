package org.example.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;


/**
 * Его топик показывает статистику пришедших ивентов за последние 10 сек
 */
public class EventCountTimeSeriesBuilder {
  private static final String       TIMESERIES_TOPIC = "wikimedia.stats.timeseries";

  private static final String       TIMESERIES_STORE = "event.count.store";
  private static final ObjectMapper OBJECT_MAPPER    = new ObjectMapper();
  private final KStream<String, String> inputStream;

  public EventCountTimeSeriesBuilder(KStream<String, String> inputStream) {
    this.inputStream = inputStream;
  }

  public void setUp() {
    final TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10));

    this.inputStream
        .selectKey((key, value) -> "key-to-group")
        .groupByKey()
        .windowedBy(timeWindows)
        .count(Materialized.as(TIMESERIES_STORE))
        .toStream()
        .mapValues((readOnlyKey, value) -> {
          final Map<String, Object> kvMap = Map.of(
              "start_time", readOnlyKey.window().startTime().toString(),
              "end_time", readOnlyKey.window().endTime().toString(),
              "window_size", timeWindows.size(),
              "event_count", value
          );
          try {
            return OBJECT_MAPPER.writeValueAsString(kvMap);
          } catch (IOException e) {
            return null;
          }
        })
        .to(TIMESERIES_TOPIC, Produced.with(
            WindowedSerdes.timeWindowedSerdeFrom(String.class, timeWindows.size()),
            Serdes.String()
        ));
  }
}
