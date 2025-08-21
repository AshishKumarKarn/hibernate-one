package com.karn;

import com.karn.entities.Book_;
import org.hibernate.jpa.HibernatePersistenceConfiguration;


import com.karn.entities.Book;

import java.util.UUID;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {
        try (var sessionFactory =
                     new HibernatePersistenceConfiguration("Bookshelf")
                             .managedClass(Book.class)
                             .jdbcDriver("com.mysql.cj.jdbc.Driver")
                             // use mysql  database
                             .jdbcUrl("jdbc:mysql://localhost:3306/bookshelf?useSSL=false&serverTimezone=UTC")
                             .jdbcCredentials("root", "")
                             // set the Agroal connection pool size
                             .jdbcPoolSize(16)
                             // display SQL in console
                             .showSql(true, true, true)
                             .createEntityManagerFactory();) {

            // export the inferred database schema
//        sessionFactory.getSchemaManager().create(false);

            String isbn = UUID.randomUUID().toString();
            // persist an entity
            sessionFactory.inTransaction(session -> {
                session.persist(new Book(isbn, "Hibernate in Action"));
            });

            // query data using HQL
            sessionFactory.inSession(session -> {
                out.println("using HQL: " + session.createSelectionQuery("select isbn||': '||title from Book").getResultCount());
            });

            // query data using criteria API
            sessionFactory.inSession(session -> {
                var builder = sessionFactory.getCriteriaBuilder();
                var query = builder.createQuery(String.class);
                var book = query.from(Book.class);
                query.select(builder.concat(builder.concat(book.get(Book_.isbn), builder.literal(": ")),
                        book.get(Book_.title)));
                session.createSelectionQuery(query).getResultStream()
                        .forEach(res -> out.println("using Criteria: " + res));
            });
        }
    }
}