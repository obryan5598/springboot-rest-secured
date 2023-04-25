# SPRINGBOOT APPLICATION WITH SECURED REST SERVICES

The following example shows how it is possible to secure a SpringBoot 2.x REST service on Wildfly/JBoss EAP.

The reproducer has been built using:

- OpenJDK 17 (openjdk version "17.0.5" 2022-10-18)
- Apache Maven 3.9.0
- SpringBoot 2.7.8
- Red Hat JBoss EAP 7.4.10


## EAP Requirements

Before proceeding, please be sure to have the foretold software properly installed and configured.

EAP needs two application users to be registered, *user* and *admin*.
To add them to the properties-file realm, please run:


```
sh /path/to/your/EAP_HOME/bin/add-user.sh -sc /path/to/your/EAP-instance/configuration -a -u admin -p password -g AdminRole -e
sh /path/to/your/EAP_HOME/bin/add-user.sh -sc /path/to/your/EAP-instance/configuration -a -u user -p password -g UserRole -e
```

Afterwards, please start EAP instance using JDK 11 (some edits need to be done before switching to JDK 17) and run the following CLI:

```
sh /path/to/your/EAP_HOME/bin/standalone.sh -Djboss.server.base.dir=/path/to/your/EAP-instance -c standalone-full-ha.xml

# IN A NEW TERMINAL
sh /path/to/your/EAP_HOME/bin/jboss-cli.sh -Djboss.server.config.dir=/path/to/your/EAP-instance/configuration -c --controller=localhost:9990

# EXECUTE THE FOLLOWING CLI BATCH TO ENABLE JDK17 RUNTIME (BATCH REMOVES/SUBSTITUTES LEGACY SECURITY REFERENCES):
batch
/subsystem=logging/console-handler=CONSOLE:add(enabled=true, level=INFO)
/subsystem=logging/logger=org.wildfly.security:add(level=TRACE, handlers=[FILE, CONSOLE])
/core-service=management/management-interface=http-interface:undefine-attribute(name=security-realm)
/core-service=management/management-interface=http-interface:write-attribute(name=http-authentication-factory, value=management-http-authentication)

/subsystem=undertow:undefine-attribute(name=default-security-domain)
/subsystem=remoting/http-connector=http-remoting-connector:undefine-attribute(name=security-realm)
/subsystem=remoting/http-connector=http-remoting-connector:write-attribute(name=sasl-authentication-factory, value=application-sasl-authentication)

/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context, value=applicationSSC)
/subsystem=undertow/server=default-server/https-listener=https:undefine-attribute(name=security-realm)

/subsystem=undertow/server=default-server/host=default-host/setting=http-invoker:undefine-attribute(name=security-realm)
/subsystem=undertow/server=default-server/host=default-host/setting=http-invoker:write-attribute(name=http-authentication-factory, value=management-http-authentication)

/subsystem=messaging-activemq/server=default:undefine-attribute(name=security-domain)
/subsystem=messaging-activemq/server=default:write-attribute(name=elytron-domain, value=ApplicationDomain)

/core-service=management/security-realm=ApplicationRealm:remove()
/core-service=management/security-realm=ManagementRealm:remove()
/subsystem=security:remove()
run-batch
shutdown

# THEN SWITCH TO JDK 17 AND RERUN EAP.
# ONCE STARTED, TYPE AGAIN IN THE EAP CLI:

/subsystem=elytron/security-domain=springboot-sd:add(realms=[{realm=ApplicationRealm, role-decoder=groups-to-roles}], permission-mapper=default-permission-mapper, default-realm=ApplicationRealm)
/subsystem=elytron/http-authentication-factory=springboot-http-auth:add(http-server-mechanism-factory=global,security-domain=springboot-sd,mechanism-configurations=[{mechanism-name=BASIC, mechanism-realm-configurations=[{realm-name=springboot-application-sd}]}])
/subsystem=undertow/application-security-domain=springboot-application-sd:add(http-authentication-factory=springboot-http-auth)
```


## Application Build

To build the application please run:

```
cd /path/to/your/local-repo/springboot-rest-secured
mvn clean package
```


## Application Deployment

It is possible to deploy (and redeploy) the application via EAP CLI:

```
sh /path/to/your/EAP_HOME/bin/jboss-cli.sh -Djboss.server.config.dir=/path/to/your/EAP-instance/configuration -c --controller=localhost:9990 --command="deploy /path/to/your/local-repo/springboot-rest-secured/springboot-rest-secured-web/target/springboot-rest-secured-web-1.0.0.war --force"
```


## Invoking REST APIs

Here is an invocation to the unsecured *monitor* API:

```
→ http :8080/api/rs/monitor
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 18
Content-Type: text/plain;charset=ISO-8859-1
Date: Mon, 24 Apr 2023 17:52:59 GMT
Expires: 0
Pragma: no-cache
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

Application is UP
```

Here follows an invocation to the secured *secured* API, which is allowed to both *UserRole* and *AdminRole* roles:

```
→ http --auth-type basic --auth 'user:password' :8080/api/rs/masterData/secured?exampleParameter=testParameter
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, must-revalidate
Connection: keep-alive
Content-Length: 72
Content-Type: text/plain;charset=ISO-8859-1
Date: Mon, 24 Apr 2023 17:57:15 GMT
Expires: 0
Pragma: no-cache
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

SECURED SERVICE OUTPUT
PRINCIPAL: user
EXAMPLE PARAMETER: testParameter
```

and for *admin* user as well:

```
→ http --auth-type basic --auth 'admin:password' :8080/api/rs/masterData/secured?exampleParameter=testParameter
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, must-revalidate
Connection: keep-alive
Content-Length: 73
Content-Type: text/plain;charset=ISO-8859-1
Date: Mon, 24 Apr 2023 17:57:43 GMT
Expires: 0
Pragma: no-cache
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

SECURED SERVICE OUTPUT
PRINCIPAL: admin
EXAMPLE PARAMETER: testParameter
```

When invoking with wrong credentials, the following output is given by Elytron:

```
→ http --auth-type basic --auth 'user:wrong_password' :8080/api/rs/masterData/secured?exampleParameter=testParameter
HTTP/1.1 401 Unauthorized
Cache-Control: no-cache, no-store, must-revalidate
Connection: keep-alive
Content-Length: 71
Content-Type: text/html;charset=UTF-8
Date: Mon, 24 Apr 2023 17:58:23 GMT
Expires: 0
Pragma: no-cache
WWW-Authenticate: Basic realm="springboot-application-sd"

<html><head><title>Error</title></head><body>Unauthorized</body></html>
```

Here is an invocation to the secured *securedAdmin* API, which is allowed to *AdminRole* only:

```
→ http --auth-type basic --auth 'admin:password' :8080/api/rs/masterData/securedAdmin?exampleParameter=testParameter
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, must-revalidate
Connection: keep-alive
Content-Length: 79
Content-Type: text/plain;charset=ISO-8859-1
Date: Mon, 24 Apr 2023 17:54:33 GMT
Expires: 0
Pragma: no-cache
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

ADMIN SECURED SERVICE OUTPUT
PRINCIPAL: admin
EXAMPLE PARAMETER: testParameter
```

Whereas, when using an user with a different role (for example a *UserRole*), the following output from Spring Boot appears:

```
→ http --auth-type basic --auth 'user:password' :8080/api/rs/masterData/securedAdmin?exampleParameter=testParameter
HTTP/1.1 403 Forbidden
Cache-Control: no-cache, no-store, must-revalidate
Connection: keep-alive
Content-Type: application/json
Date: Mon, 24 Apr 2023 17:55:14 GMT
Expires: 0
Pragma: no-cache
Transfer-Encoding: chunked
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

{
    "error": "Forbidden",
    "path": "/api/rs/masterData/securedAdmin",
    "status": 403,
    "timestamp": 1682358914887
}
```


## TODOs

Following enhancements can be made:
- Use a LDAP user-base 
