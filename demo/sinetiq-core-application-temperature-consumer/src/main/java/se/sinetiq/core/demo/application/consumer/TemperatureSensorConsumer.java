package se.sinetiq.core.demo.application.consumer;

import se.sinetiq.core.demo.api.client.ApiClient;
import se.sinetiq.core.demo.api.client.ApiException;
import se.sinetiq.core.demo.api.client.Configuration;
import se.sinetiq.core.demo.api.client.api.DefaultApi;
import se.sinetiq.core.demo.api.client.model.HistoricalTemperatureData;
import se.sinetiq.core.demo.api.client.model.TemperatureData;
import se.sinetiq.core.sr.consul.api.ConsulAPI;
import se.sinetiq.core.sr.consul.api.ServiceData;
import se.sinetiq.core.sr.consul.api.ServiceName;
import se.sinetiq.core.sr.consul.api.ServiceType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;

public class TemperatureSensorConsumer {

    public static void main(String[] args) {

        String host = System.getenv("CFG_SR_HOST");
        String port = System.getenv("CFG_SR_PORT");
        ConsulAPI consulAPI = new ConsulAPI();
        try {
            consulAPI.configure(host, port);
        } catch (Throwable t) {
            System.out.println("Service registry configuration failure!");
            t.printStackTrace();
            System.exit(1);
        }

        while (true) {

            ServiceData sd = lookupTempService(consulAPI);

            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setBasePath("http://" + sd.getHost() + ":" + sd.getPort() /*+ pathStr*/);

            DefaultApi apiInstance = new DefaultApi(defaultClient);
            try {
                TemperatureData result = apiInstance.temperatureGet();
                HistoricalTemperatureData history = apiInstance.temperatureHistoryGet(
                        OffsetDateTime.now().minusSeconds(10),
                        OffsetDateTime.now());
                System.out.printf("Response from %s%n:", sd.getName());
                System.out.printf("  Current temperature: %s (Read by %s at %s at %s)%n",
                        result.getTemperature(),
                        result.getMachineID(),
                        result.getLocation(),
                        result.getTimestamp().format(ofPattern("HH:mm:ss")));
                System.out.printf("  History (Last 10s): %s%n",
                        history.getReadings().stream()
                                .map(data -> String.format("%s: %s°%s",
                                        data.getTimestamp().format(ofPattern("HH:mm:ss")),
                                        data.getTemperature(),
                                        data.getUnit()))
                                .collect(Collectors.toList()));

            } catch (ApiException e) {
                System.err.println("Exception when calling DefaultApi#temperatureGet");
                System.err.println("Status code: " + e.getCode());
                System.err.println("Reason: " + e.getResponseBody());
                System.err.println("Response headers: " + e.getResponseHeaders());
                /*e.printStackTrace();*/
            }

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static ServiceData lookupTempService(ConsulAPI consulAPI) {
        String service = System.getenv("CFG_SERVICE_NAME");
        ServiceType serviceType = new ServiceType(service);

        int retry_limit = 10;
        for (int i = 1; i <= retry_limit; i++) {
            System.out.printf("Attempting to discover service and fetch temperature data (%d/%d)%n", i, retry_limit);
            //
            // Part 1: Discover (lookup) services (api instances) that fulfill the api specification 'temperature-sensor'
            //
            List<ServiceName> apiInstances = consulAPI.getServiceInstances(serviceType);

            if (apiInstances.size() > 0) {
                System.out.printf("Discovered %d service instances:%n", apiInstances.size());
                for (ServiceName sn : apiInstances) {
                    System.out.println("+ " + sn.getName());
                }
                //
                // Part 2: Use discovery endpoint details for establish connection
                //
                ServiceName sn = apiInstances.get(0); // Just pick the first service (api instance) found
                ServiceData sd = consulAPI.getServiceData(sn);
                if (sd == null) {
                    throw new RuntimeException("No services data found");
                }

                return sd;
            }

            // Else, wait a while and retry.
            try {
                Thread.sleep(5000);
                continue;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("No service found after " + retry_limit + " tries, giving up");
    }
}
