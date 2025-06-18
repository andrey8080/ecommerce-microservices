# Troubleshooting Camunda Tasklist

If tasks do not appear in the Camunda Tasklist UI, ensure the engine name is `default`.
In Spring Boot applications this means disabling unique engine name generation:

```yaml
camunda:
  bpm:
    generate-unique-process-engine-name: false
```

Restart the service after applying the change.

If Tasklist shows `Form failure: The context path is either empty or not defined`,
also specify the web application path:

```yaml
camunda:
  bpm:
    webapp:
      application-path: /
```

Then restart the affected service.
