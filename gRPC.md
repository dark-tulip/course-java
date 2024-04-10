## JSON
- JSON - это текстовый формат данных (больший размер сообщения: медленней пересылка)
- Избыточный - повторение ключей в массиве каждый раз
- нет строгой типизации

## Protobuf
- Protobuf - бинарный формат данных (ускорение до 7-10 раз)
- строная типизация
- нечитаемый
- необходимо кодировать и декодировать эти данные

## HTTP 2.0
- меньший размер - выше скорость
- потоки данных
- мультиплексирование, приоритизация потоков

## Генерация кода
- компилятор protoc
- `.proto` file describes типы данных, формат сообщений и RPC операции

## Когда нужен gRPC?
- если монолит: куда нужен доступ из браузера REST API
- between microservices
- different langs
- data streaming
- огромное кол-во запросов или узкий канал

# GRPC server

```proto
syntax = "proto3";
package kz.inn.grpc;

message HelloRequest {
  string name = 1;  // key=1
  repeated string hobbies = 2; // key tag should be unique
}

message HelloResponse {
  string greeting = 1;
}

service GreetingService {
  rpc greeting(HelloRequest) returns (HelloResponse);
}
```

```java
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        Server server = ServerBuilder.forPort(8080).addService(new GreetingServiceImpl()).build();

        server.start();

        System.out.println("Server started");

        server.awaitTermination();

    }
}

public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
    public void greeting(kz.inn.grpc.GreetingServiceOuterClass.HelloRequest request,
                         StreamObserver<kz.inn.grpc.GreetingServiceOuterClass.HelloResponse> responseStreamObserver) {
        System.out.println(request);

        GreetingServiceOuterClass.HelloResponse response = kz.inn.grpc.GreetingServiceOuterClass
                .HelloResponse.newBuilder()
                .setGreeting("Hello from server, " + request.getName())
                .build();

        responseStreamObserver.onNext(response);

        responseStreamObserver.onCompleted();
    }
}
```
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>kz.inn</groupId>
    <artifactId>GRPC_SERVER</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
            <version>1.24.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>1.24.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>1.24.0</version>
        </dependency>
        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
            <version>1.3.1</version>
        </dependency>
    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.6.2</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:3.9.0:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.24.0:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

# GRPC client + same `proto file` and `pom.xml` as in server
```java

public class Client {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8080")
                .usePlaintext().build();

        // stub это тот объект на котором можно делать удаленные вызовы
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingServiceOuterClass.HelloRequest request = GreetingServiceOuterClass.HelloRequest
                .newBuilder().setName("Neil").build();

        GreetingServiceOuterClass.HelloResponse response = stub.greeting(request);

        System.out.println(response);

        channel.shutdownNow();
    }
}
```