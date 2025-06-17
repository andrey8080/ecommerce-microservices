package com.example.cartservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption

@Configuration
class CassandraConfig : AbstractCassandraConfiguration() {

    @Value("\${spring.cassandra.keyspace-name}")
    private lateinit var keyspaceName: String

    @Value("\${spring.cassandra.contact-points}")
    private lateinit var contactPoints: String

    @Value("\${spring.cassandra.port}")
    private var port: Int = 9042

    @Value("\${spring.cassandra.local-datacenter}")
    private lateinit var localDatacenter: String

    override fun getKeyspaceName(): String {
        return keyspaceName
    }

    override fun getContactPoints(): String {
        return contactPoints
    }

    override fun getPort(): Int {
        return port
    }

    override fun getLocalDataCenter(): String {
        return localDatacenter
    }

    override fun getSchemaAction(): SchemaAction {
        return SchemaAction.CREATE_IF_NOT_EXISTS
    }

    override fun getKeyspaceCreations(): List<CreateKeyspaceSpecification> {
        return listOf(
            CreateKeyspaceSpecification.createKeyspace(keyspaceName)
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication(1)
        )
    }

    override fun getKeyspaceDrops(): List<DropKeyspaceSpecification> {
        return emptyList()
    }
}
