package cn.bobasyu.databeses

import io.smallrye.mutiny.Uni
import jakarta.persistence.Persistence
import org.hibernate.reactive.mutiny.Mutiny.Session
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory

object PostgresqlClient {
    val factory: SessionFactory by lazy {
        Persistence.createEntityManagerFactory("postgresql-example")
            .unwrap(SessionFactory::class.java)
    }

    fun <T> withSession(var1: (Session) -> Uni<T>): Uni<T> = factory.withSession(var1)
}