1. Через программу KeyStore explorer можно создать свой CA
2. В нем можно создать связку ключей которую можно использовать в приложении
3. У некоторых браузеров свое хранилище доверенных сертификатов (for example, firefox)


## Setup spring application profile

Run/Debug configurations -> (Modify options) enable checkbox active profiles - set name of your profile in `application.properties` file

<img width="50%" alt="image" src="https://github.com/dark-tulip/course-java/assets/89765480/cbfc91f2-311d-431d-840e-3942d306d055" >

<hr>

### Enable SSL on Spring application

```properties
server.ssl.enabled=true
```

```logs
# before
2024-02-18T18:53:17.755+06:00  INFO 69923 --- : Tomcat started on port 8080 (http) with context path ''
# after 
2024-02-18T18:52:55.406+06:00  INFO 69910 --- : Tomcat started on port 8080 (https) with context path ''
```

## 1. Application properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres

# Включить на сервере SSL шифрование помогает настройка
server.ssl.enabled=true

# Пара ключей была сэкспортирована в этом формате,
server.ssl.key-store-type=PKCS12

# Путь к паре ключей из ресурсов проекта, им будет шифровать или дешифровать данные, которые приходят из зашифрованного канала
# Если пара ключей и пароль введены правильно приложение успешно запустится, иначе `keystore password was incorrect`
server.ssl.key-store=classpath:ssl/test_tansh_ssl_rootca.pfx
server.ssl.key-store-password=root

logging.level.root=info
```

## 2. Create test controller
```java
@RestController
@RequestMapping("/test")
public class TestController {

  @GetMapping
  public ResponseEntity<String> testSsl() {
    return ResponseEntity.ok("Hello");
  }
}
```

<hr>


#### Using https `https://localhost:8080/test`

<img width="50%" alt="image" src="https://github.com/dark-tulip/course-java/assets/89765480/e17f556d-617a-4047-9961-dcbab64ef44d">



#### Без установки защищенного соединения `http://localhost:8080/test`

<img width="50%" alt="image" src="https://github.com/dark-tulip/course-java/assets/89765480/f3be2d26-21cd-4e6f-9530-71354850057c">
